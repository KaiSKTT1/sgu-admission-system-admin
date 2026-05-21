package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Import bảng điểm cộng xét tuyển từ file Excel (.xlsx) vào bảng xt_diemcongxetuyen.
 *
 * Cấu trúc file Excel:
 * - Col 0: STT (tự động, không dùng)
 * - Col 1: CCCD → ts_cccd
 * - Col 2: Tên ngành → tra bảng xt_nganh để lấy maNganh
 * - Col 3: Môn đạt giải → convert sang mã môn hoặc dùng trực tiếp nếu đã có mã
 * - Col 4: Điểm cộng cho môn đạt giải
 * - Col 5: Điểm cộng cho THXT ko có môn đạt giải
 *
 * Logic xử lý:
 * 1. Lấy CCCD từ col 1
 * 2. Tra cứu tên ngành (col 2) trong bảng xt_nganh → lấy maNganh
 * 3. Lấy tất cả mã tổ hợp của maNganh từ xt_nganh_tohop
 * 4. Xử lý mã môn từ col 3:
 *    - Nếu Excel có cột "Mã môn" sẵn: dùng trực tiếp
 *    - Nếu không: convert từ "Môn đạt giải"
 * 5. So khớp mã môn với mon1/mon2/mon3 của tổ hợp:
 *    - Nếu khớp → diemUtxt = điểm cộng cho môn đạt giải (col 4)
 *    - Nếu không khớp → diemUtxt = điểm cộng cho THXT ko có môn đạt giải (col 5)
 * 6. Tính diemTong = diemCc + diemUtxt
 * 7. Lưu vào xt_diemcongxetuyen
 */
public class DiemCongImporter {

    private static final int BATCH_SIZE = 50;
    private static final int HEADER_ROW_IDX = 0; // 0-based
    private static final int DATA_START_IDX = 1; // 0-based

    // Lưu trữ vị trí cột được phát hiện động từ header
    private static class ColumnMapping {
        int colCccd = -1; // CCCD
        int colTenNganh = -1; // Tên ngành
        int colMaMon = -1; // Mã môn (nếu có)
        int colMonDatGiai = -1; // Môn đạt giải (nếu có)

        int colDiemMonDatGiai = -1; // Điểm cộng cho môn đạt giải
        int colDiemThxtKhongMon = -1; // Điểm cộng cho THXT ko có môn đạt giải

        // FILE TIẾNG ANH
        int colDiemCongTiengAnh = -1;

        // đánh dấu file tiếng anh
        boolean isEnglishCertificateFile = false;

        boolean isValid() {

            // file điểm cộng tiếng anh
            if (isEnglishCertificateFile) {
                return colCccd >= 0
                        && colDiemCongTiengAnh >= 0;
            }

            // file cũ
            return colCccd >= 0
                    && (colTenNganh >= 0 || colMaMon >= 0)
                    && colDiemMonDatGiai >= 0
                    && colDiemThxtKhongMon >= 0;
        }

        @Override
        public String toString() {
            return String.format(
                    "ColumnMapping[CCCD=%d, TenNganh=%d, MaMon=%d, MonDatGiai=%d, DiemMon=%d, DiemThxt=%d, DiemAnh=%d, EnglishFile=%s]",
                    colCccd,
                    colTenNganh,
                    colMaMon,
                    colMonDatGiai,
                    colDiemMonDatGiai,
                    colDiemThxtKhongMon,
                    colDiemCongTiengAnh,
                    isEnglishCertificateFile
            );
        }
    }

    // Kết quả trả về
    public static class ImportResult {
        public final int totalSuccess;
        public final int totalSkipDuplicate;
        public final int totalSkipError;
        public final String logPath;
        public final List<String> processedSheets; // Danh sách các sheet được xử lý
        public final List<String> errorMessages; // Danh sách lỗi chi tiết

        public ImportResult(int totalSuccess, int totalSkipDuplicate, int totalSkipError, String logPath,
                List<String> processedSheets, List<String> errorMessages) {
            this.totalSuccess = totalSuccess;
            this.totalSkipDuplicate = totalSkipDuplicate;
            this.totalSkipError = totalSkipError;
            this.logPath = logPath;
            this.processedSheets = processedSheets;
            this.errorMessages = errorMessages;
        }
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    public static ImportResult importFromExcel(String filePath) {
        String logPath = filePath.replace(".xlsx", "_diem_cong_import.log");

        int totalSuccess = 0;
        int totalSkipDuplicate = 0;
        int totalSkipError = 0;
        List<String> processedSheets = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        // Tải trước các key đã tồn tại để kiểm tra trùng
        Set<String> existingKeys = loadExistingKeys();

        // Cache để lấy dữ liệu từ DB
        Map<String, XtNganh> nganhMap = loadNganhMap(); // tenNganh → XtNganh
        Map<String, List<XtNganhToHop>> nganhToHopMap = loadNganhToHopMap(); // maNganh → List<XtNganhToHop>
        Map<String, List<String>> toHopMonMap = loadToHopMonMap(); // maToHop → List[mon1, mon2, mon3]

        List<XtDiemCongXetTuyen> batch = new ArrayList<>(BATCH_SIZE);

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook wb = new XSSFWorkbook(fis);
                PrintWriter logWriter = new PrintWriter(new FileWriter(logPath, true))) {

            logWriter.println("=== Import bảng điểm cộng bắt đầu: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    + " | File: " + filePath + " ===");
            logWriter.println("Số bản ghi đã có trong DB: " + existingKeys.size());
            logWriter.println("Số ngành trong DB: " + nganhMap.size());
            if (!nganhMap.isEmpty()) {
                String sampleNganh = nganhMap.keySet().stream().limit(5).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
                logWriter.println("Mẫu ngành: " + sampleNganh + (nganhMap.size() > 5 ? ", ..." : ""));
            }
            logWriter.println("Số sheet tìm thấy: " + wb.getNumberOfSheets());

            // Lặp qua tất cả các sheet trong workbook
            for (int sheetIdx = 0; sheetIdx < wb.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = wb.getSheetAt(sheetIdx);
                String sheetName = sheet.getSheetName();
                processedSheets.add(sheetName);

                logWriter.println("\n--- Xử lý sheet: " + sheetName + " ---");

                // Phát hiện vị trí cột dựa trên header row
                ColumnMapping colMap = detectColumns(sheet, logWriter, sheetName);
                if (!colMap.isValid()) {
                    logWriter.println("[" + sheetName + "] LỖI: Không thể tìm thấy các cột bắt buộc. "
                            + "Cần có: CCCD, (Tên ngành hoặc Mã môn), Điểm CC món, Điểm CC THXT");
                    logWriter.println("Detected: " + colMap);
                    continue;
                }
                logWriter.println("[" + sheetName + "] Detected columns: " + colMap);
                // ================= FILE ĐIỂM CỘNG TIẾNG ANH =================
                if (colMap.isEnglishCertificateFile) {

                    for (int rowIdx = DATA_START_IDX; rowIdx <= sheet.getLastRowNum(); rowIdx++) {

                        Row row = sheet.getRow(rowIdx);

                        if (isRowEmpty(row)) {
                            continue;
                        }

                        String cccd = getString(row, colMap.colCccd);

                        if (cccd == null || cccd.isBlank()) {
                            continue;
                        }

                        cccd = cccd.trim();

                        BigDecimal diemCongTA =
                                getBigDecimal(row, colMap.colDiemCongTiengAnh);

                        if (diemCongTA == null) {
                            diemCongTA = BigDecimal.ZERO;
                        }

                        try (Session session = HibernateUtil
                                .getSessionFactory()
                                .openSession()) {

                            Transaction tx = session.beginTransaction();

                            List<XtDiemCongXetTuyen> list =
                                    session.createQuery(
                                            "from XtDiemCongXetTuyen "
                                            + "where tsCccd = :cccd",
                                            XtDiemCongXetTuyen.class)
                                    .setParameter("cccd", cccd)
                                    .list();
                                    if (list.isEmpty()) {

                                        // =====================================================
                                        // CCCD chưa có trong bảng điểm cộng
                                        // -> tự tạo mới theo ngành + tổ hợp của thí sinh
                                        // =====================================================

                                        List<String> dsMaNganh =
                                                session.createQuery(
                                                        "select distinct nv.maNganh "
                                                        + "from XtNguyenVong nv "
                                                        + "where nv.tsCccd = :cccd",
                                                        String.class)
                                                .setParameter("cccd", cccd)
                                                .list();

                                        if (dsMaNganh.isEmpty()) {

                                            logWriter.printf(
                                                    "[ENGLISH] CCCD=%s không tìm thấy ngành/nguyện vọng%n",
                                                    cccd);

                                            totalSkipError++;

                                            tx.commit();

                                            continue;
                                        }

                                        for (String maNganh : dsMaNganh) {

                                            List<XtNganhToHop> toHops =
                                                    nganhToHopMap.get(normalize(maNganh));

                                            if (toHops == null || toHops.isEmpty()) {

                                                logWriter.printf(
                                                        "[ENGLISH] CCCD=%s | maNganh=%s không có tổ hợp%n",
                                                        cccd,
                                                        maNganh);

                                                continue;
                                            }

                                            for (XtNganhToHop toHop : toHops) {

                                                String maToHop = toHop.getMaToHop();

                                                XtDiemCongXetTuyen entity =
                                                        new XtDiemCongXetTuyen();

                                                entity.setTsCccd(cccd);

                                                entity.setMaNganh(maNganh);

                                                entity.setMaToHop(maToHop);

                                                entity.setPhuongThuc("THPT");

                                                entity.setDiemCc(diemCongTA);

                                                entity.setDiemUtxt(BigDecimal.ZERO);

                                                entity.setDiemTong(diemCongTA);

                                                entity.setGhiChu(
                                                        "AUTO CREATED FROM ENGLISH FILE");

                                                entity.setDcKeys("");

                                                session.persist(entity);

                                                logWriter.printf(
                                                        "[ENGLISH] INSERT CCCD=%s | maNganh=%s | maToHop=%s | diemCC=%s%n",
                                                        cccd,
                                                        maNganh,
                                                        maToHop,
                                                        diemCongTA);
                                            }
                                        }

                                        tx.commit();

                                        totalSuccess++;

                                        continue;
                                    }

                            for (XtDiemCongXetTuyen entity : list) {

                                entity.setDiemCc(diemCongTA);

                                BigDecimal tong =
                                        sum(entity.getDiemUtxt(), diemCongTA);

                                entity.setDiemTong(tong);

                                session.merge(entity);
                            }

                            tx.commit();

                            totalSuccess++;

                            logWriter.printf(
                                    "[ENGLISH] UPDATE CCCD=%s | diemCC=%s%n",
                                    cccd,
                                    diemCongTA);

                        } catch (Exception ex) {

                            logWriter.printf(
                                    "[ENGLISH] ERROR CCCD=%s | %s%n",
                                    cccd,
                                    ex.getMessage());

                            totalSkipError++;
                        }
                    }

                    // xử lý xong sheet tiếng Anh thì skip logic cũ
                    continue;
                }
                for (int rowIdx = DATA_START_IDX; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (isRowEmpty(row))
                        continue;

                    // ── Đọc các cột ──────────────────────────────────────────────
                    String cccd = getString(row, colMap.colCccd);
                    if (cccd == null || cccd.isBlank()) {
                        String errorMsg = String.format("[%s] Dòng %d: CCCD trống", sheetName, rowIdx + 1);
                        logWriter.println(errorMsg);
                        if (errorMessages.size() < 100) errorMessages.add(errorMsg);
                        totalSkipError++;
                        continue;
                    }
                    cccd = cccd.trim();

                    // Lấy tên ngành (ưu tiên tên ngành, nếu không thì từ mã môn)
                    String tenNganh = null;
                    String tenNganhOriginal = null; // Lưu giá trị gốc để debug
                    String maMon = null;

                    if (colMap.colTenNganh >= 0) {
                        tenNganh = getString(row, colMap.colTenNganh);
                        tenNganhOriginal = tenNganh; // Lưu gốc
                    }
                    if (colMap.colMaMon >= 0) {
                        maMon = getString(row, colMap.colMaMon);
                    }

                    if ((tenNganh == null || tenNganh.isBlank()) && (maMon == null || maMon.isBlank())) {
                        String errorMsg = String.format("[%s] Dòng %d | CCCD=%s: Tên ngành/mã môn trống", sheetName,
                                rowIdx + 1, cccd);
                        logWriter.println(errorMsg);
                        if (errorMessages.size() < 100) errorMessages.add(errorMsg);
                        totalSkipError++;
                        continue;
                    }

                    if (tenNganh != null && !tenNganh.isBlank()) {
                        tenNganh = normalize(tenNganh); // Chuẩn hóa: loại bỏ dấu, khoảng cách thừa
                    }
                    if (maMon != null && !maMon.isBlank()) {
                        maMon = maMon.trim();
                    }

                    String monDatGiai = colMap.colMonDatGiai >= 0 ? getString(row, colMap.colMonDatGiai) : null;
                    BigDecimal diemMonDatGiai = getBigDecimal(row, colMap.colDiemMonDatGiai);
                    BigDecimal diemThxtKhongMon = getBigDecimal(row, colMap.colDiemThxtKhongMon);

                    // ── Tra cứu mã ngành từ tên ngành ────────────────────────────
                    // Nếu có tên ngành, tra cứu mã ngành từ bảng
                    // Nếu chỉ có mã môn, cần cách khác
                    XtNganh nganh = null;
                    if (tenNganh != null && !tenNganh.isBlank()) {
                        nganh = nganhMap.get(tenNganh);

                        if (nganh == null) {

                            String prefix = tenNganh.length() >= 5
                                    ? tenNganh.substring(0, 5)
                                    : tenNganh;

                            List<String> similarList = nganhMap.keySet().stream()
                                    .filter(k -> k.contains(prefix) || prefix.contains(k))
                                    .limit(5)
                                    .collect(Collectors.toList());

                            String errorMsg = String.format(
                                    "[%s] Dòng %d | CCCD=%s%n" +
                                    "Tên ngành gốc     = [%s]%n" +
                                    "Tên ngành chuẩn hóa = [%s]%n" +
                                    "=> KHÔNG TÌM THẤY NGÀNH TRONG DB%n" +
                                    "Gợi ý gần giống: %s%n",
                                    sheetName,
                                    rowIdx + 1,
                                    cccd,
                                    tenNganhOriginal,
                                    tenNganh,
                                    similarList.isEmpty() ? "(không có)" : similarList
                            );

                            logWriter.println(errorMsg);

                            if (errorMessages.size() < 100) {
                                errorMessages.add(errorMsg);
                            }

                            totalSkipError++;
                            continue;
                        }

                    } else if (maMon != null && !maMon.isBlank()) {
                        // Trường hợp chỉ có mã môn, không có tên ngành
                        // Bỏ qua hoặc xử lý đặc biệt
                        String errorMsg = String.format(
                                "[%s] Dòng %d | CCCD=%s: Chỉ có mã môn mà không có tên ngành (cấu trúc không hỗ trợ)",
                                sheetName, rowIdx + 1, cccd);
                        logWriter.println(errorMsg);
                        if (errorMessages.size() < 100) errorMessages.add(errorMsg);
                        totalSkipError++;
                        continue;
                    }

                    String maNganh = nganh.getMaNganh();
                    List<XtNganhToHop> toHops = nganhToHopMap.get(normalize(maNganh));
                    if (toHops == null || toHops.isEmpty()) {
                        String errorMsg = String.format(
                                "[%s] Dòng %d | CCCD=%s | Mã ngành=%s: Không có tổ hợp nào",
                                sheetName, rowIdx + 1, cccd, maNganh);
                        logWriter.println(errorMsg);
                        if (errorMessages.size() < 100) errorMessages.add(errorMsg);
                        totalSkipError++;
                        continue;
                    }

                    // ── Xử lý từng tổ hợp của ngành này ──────────────────────────
                    try {
                        for (XtNganhToHop toHop : toHops) {
                            String maToHop = toHop.getMaToHop();
                            String uniqueKey = buildKey(cccd, maNganh, maToHop);

                            if (existingKeys.contains(uniqueKey)) {
                                logWriter.printf(
                                        "[%s] Dòng %d | CCCD=%s | Mã ngành=%s | Mã tổ hợp=%s: Bỏ qua — đã tồn tại%n",
                                        sheetName, rowIdx + 1, cccd, maNganh, maToHop);
                                totalSkipDuplicate++;
                                continue;
                            }

                            // Kiểm tra trùng trong batch hiện tại
                            final String keyFinal = uniqueKey;
                            if (batch.stream().anyMatch(
                                    e -> keyFinal.equals(buildKey(e.getTsCccd(), e.getMaNganh(), e.getMaToHop())))) {
                                totalSkipDuplicate++;
                                continue;
                            }

                            // ── Tính điểm cộng dựa trên mã môn ──────────────────────
                            String monDatGiaiToUse = monDatGiai != null ? monDatGiai : maMon;
                            BigDecimal diemUtxt = calculateDiemUtxt(monDatGiaiToUse, maToHop, diemMonDatGiai,
                                    diemThxtKhongMon, toHopMonMap, logWriter, rowIdx, cccd, sheetName);

                            XtDiemCongXetTuyen entity = new XtDiemCongXetTuyen();
                            entity.setTsCccd(cccd);
                            entity.setMaNganh(maNganh);
                            entity.setMaToHop(maToHop);
                            entity.setPhuongThuc("THPT"); // Mặc định
                            entity.setDiemCc(null); // Điểm cộng tiếng Anh (chưa có logic xử lý)
                            entity.setDiemUtxt(diemUtxt); // Điểm cộng HSG
                            BigDecimal diemTong = diemUtxt; // Tổng = chỉ có diemUtxt (HSG)
                            entity.setDiemTong(diemTong);
                            entity.setGhiChu("");
                            entity.setDcKeys("");
                            logWriter.printf(
                                "SAVE -> CCCD=%s | maToHop=%s | diemMonDatGiai=%s | diemThxt=%s | diemUtxt=%s%n",
                                cccd,
                                maToHop,
                                diemMonDatGiai,
                                diemThxtKhongMon,
                                diemUtxt
                            );
                            batch.add(entity);
                            totalSuccess++;
                        }

                    } catch (Exception e) {
                        String errorMsg = String.format("[%s] Dòng %d | CCCD=%s: %s",
                                sheetName, rowIdx + 1, cccd, e.getMessage());
                        logWriter.println(errorMsg);
                        if (errorMessages.size() < 100) errorMessages.add(errorMsg);
                        totalSkipError++;
                        continue;
                    }

                    // ── Flush khi đủ batch ───────────────────────────────────────
                    if (batch.size() >= BATCH_SIZE) {
                        flushBatch(batch, existingKeys, logWriter);
                        batch.clear();
                    }
                }
            }

            // Flush phần còn lại
            if (!batch.isEmpty()) {
                flushBatch(batch, existingKeys, logWriter);
            }

            String summary = String.format(
                    "[DiemCongImporter] Hoàn thành — Sheets: %d | Thành công: %d | Bỏ qua trùng: %d | Lỗi: %d",
                    processedSheets.size(), totalSuccess, totalSkipDuplicate, totalSkipError);
            System.out.println(summary);
            logWriter.println(summary);

        } catch (Exception e) {
            System.err.println("[DiemCongImporter] Lỗi nghiêm trọng: " + e.getMessage());
            e.printStackTrace();
        }

        return new ImportResult(totalSuccess, totalSkipDuplicate, totalSkipError, logPath, processedSheets, errorMessages);
    }

    // ── Phát hiện vị trí cột dựa trên header row ────────────────────────────────

    private static ColumnMapping detectColumns(Sheet sheet, PrintWriter logWriter, String sheetName) {
        ColumnMapping colMap = new ColumnMapping();

        Row headerRow = sheet.getRow(HEADER_ROW_IDX);
        if (headerRow == null) {
            logWriter.println("[" + sheetName + "] Lỗi: Không tìm thấy header row");
            return colMap;
        }

        // Duyệt qua tất cả các cột để tìm tên cột
        for (int colIdx = 0; colIdx < headerRow.getLastCellNum(); colIdx++) {
            String colName = getString(headerRow, colIdx);
            if (colName == null || colName.isBlank()) {
                continue;
            }

            colName = colName.trim().toUpperCase();

            // Tìm các cột cần thiết
            if (colName.contains("CCCD")) {
                colMap.colCccd = colIdx;
            } else if (colName.contains("TÊN NGÀNH") || colName.contains("TEN NGANH")) {
                colMap.colTenNganh = colIdx;
            } else if (colName.contains("MÃ MÔN") || colName.contains("MA MON")) {
                colMap.colMaMon = colIdx;
            } else if ((colName.contains("MÔN ĐẠT GIẢI") || colName.contains("MON DAT GIAI")) && colMap.colMonDatGiai < 0) {
                colMap.colMonDatGiai = colIdx;
            } else if (colName.contains("ĐIỂM CỘNG CHO MÔN") || colName.contains("DIEM CONG CHO MON")) {
                // Ưu tiên cột đầu tiên nếu có 2 cột "Điểm cộng cho môn"
                if (colMap.colDiemMonDatGiai < 0) {
                    colMap.colDiemMonDatGiai = colIdx;
                }
            } else if (colName.contains("ĐIỂM CỘNG CHO THXT") || colName.contains("DIEM CONG CHO THXT")) {
                colMap.colDiemThxtKhongMon = colIdx;
            } 
                else if (colName.contains("ĐIỂM CỘNG")
                        || colName.contains("DIEM CONG")) {

                    colMap.colDiemCongTiengAnh = colIdx;
                    colMap.isEnglishCertificateFile = true;
        }
            
        }

        return colMap;
    }

    // ── Tính điểm cộng dựa trên mã môn ──────────────────────────────────────

    private static BigDecimal calculateDiemUtxt(
        String monDatGiai,
        String maToHop,
        BigDecimal diemMonDatGiai,
        BigDecimal diemThxtKhongMon,
        Map<String, List<String>> toHopMonMap,
        PrintWriter logWriter,
        int rowIdx,
        String cccd,
        String sheetName) {

    logWriter.println("\n========== DEBUG START ==========");

    logWriter.printf(
            "[%s] Row=%d | CCCD=%s%n",
            sheetName,
            rowIdx + 1,
            cccd
    );

    logWriter.printf("monDatGiai RAW = [%s]%n", monDatGiai);

    if (monDatGiai == null || monDatGiai.isBlank()) {
        logWriter.println("=> monDatGiai NULL -> dùng diemThxtKhongMon");
        return diemThxtKhongMon;
    }

    String maMonChuanHoa =
            normalizeMaMonFromName(monDatGiai.trim());

    logWriter.printf("maMonChuanHoa = [%s]%n", maMonChuanHoa);

    String maToHopNormalized = normalize(maToHop);

    logWriter.printf("maToHop = [%s]%n", maToHop);
    logWriter.printf("maToHopNormalized = [%s]%n", maToHopNormalized);

    List<String> danhSachMon =
            toHopMonMap.get(maToHopNormalized);

    logWriter.printf("danhSachMon = %s%n", danhSachMon);

    if (danhSachMon != null) {
        for (String mon : danhSachMon) {
            logWriter.printf(
                    "COMPARE DB=[%s] | INPUT=[%s] | EQUAL=%s%n",
                    mon,
                    maMonChuanHoa,
                    mon.equals(maMonChuanHoa)
            );
        }
    }

    boolean khop = danhSachMon != null &&
            danhSachMon.stream()
                    .anyMatch(mon ->
                            mon != null &&
                            mon.equals(maMonChuanHoa));

    logWriter.printf("RESULT KHOP = %s%n", khop);

    logWriter.println("========== DEBUG END ==========\n");

    return khop
            ? diemMonDatGiai
            : diemThxtKhongMon;
}
    /**
     * Convert từ tên môn sang mã môn (vd: "Tiếng Anh" → "N1", "Lịch sử" → "SU")
     * Hoặc nếu đã là mã môn rồi, trả về như cũ
     */
    private static String normalizeMaMonFromName(String tenMon) {
    if (tenMon == null || tenMon.isBlank()) {
        return null;
    }

    // Dùng hàm normalize có sẵn của bạn
    String normalized = normalize(tenMon);

    return switch (normalized) {

        case "TIENG ANH", "ENGLISH", "ANH" -> "N1";

        case "TOAN HOC", "MATH" -> "TO";

        case "LICH SU", "HISTORY", "SU" -> "SU";

        case "DIA LY", "DIA LI", "GEOGRAPHY", "DIA" -> "DI";

        case "HOA HOC", "CHEMISTRY", "HOA" -> "HO";

        case "VAT LY", "PHYSICS", "VAT" -> "VA";

        case "SINH HOC", "BIOLOGY", "SINH" -> "SI";

        case "NGU VAN" -> "VA";

        case "TIN HOC" -> "TI";

        case "GIAO DUC KINH TE VA PHAP LUAT" -> "KTPL";


        // nếu đã là mã môn
        default -> {
            if (normalized.length() <= 4) {
                yield normalized;
            }
            yield null;
        }
    };
}


    // ── Hibernate helpers ────────────────────────────────────────────────────

    private static Set<String> loadExistingKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session
                    .createQuery(
                            "select e.tsCccd, e.maNganh, e.maToHop from XtDiemCongXetTuyen e",
                            Object[].class)
                    .list();
            Set<String> keys = new HashSet<>(rows.size() * 2);
            for (Object[] r : rows) {
                keys.add(buildKey(
                        r[0] != null ? r[0].toString() : "",
                        r[1] != null ? r[1].toString() : "",
                        r[2] != null ? r[2].toString() : ""));
            }
            return keys;
        } catch (Exception e) {
            System.err.println("[DiemCongImporter] Không load được danh sách key: " + e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Chuẩn hóa tên ngành: loại bỏ dấu tiếng Việt, chuẩn hóa khoảng cách, viết hoa
     * Ví dụ: "Địa lý" → "DIA LY", "sư phạm lịch sử - địa lý" → "SU PHAM LICH SU-DIA LY"
     */
    private static String normalize(String input) {
        if (input == null || input.isBlank()) return "";
        
        // NFD decomposition để tách dấu riêng từ chữ cái
        String nfd = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        // Xóa các combining marks (dấu tiếng Việt)
        String normalized = nfd.replaceAll("\\p{Mn}", "");
        
        // Chuẩn hóa khoảng cách xung quanh dấu gạch: " - " → "-"
        normalized = normalized.replaceAll("\\s*-\\s*", "-");
        
        // Loại bỏ khoảng cách thừa
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized.trim().toUpperCase();
    }

    /**
     * Load tất cả ngành từ DB, map theo tên ngành (chuẩn hóa để dễ tìm kiếm)
     */
    private static Map<String, XtNganh> loadNganhMap() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<XtNganh> list = session
                    .createQuery("from XtNganh", XtNganh.class)
                    .list();
            return list.stream()
                    .collect(Collectors.toMap(
                            n -> {
                                String tenNganh = n.getTenNganh();
                                return normalize(tenNganh);
                            },
                            n -> n,
                            (a, b) -> a)); // Lấy cái đầu nếu trùng
        } catch (Exception e) {
            System.err.println("[DiemCongImporter] Không load được bảng ngành: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    /**
     * Load tất cả ngành-tổ hợp từ DB, map theo maNganh (in hoa)
     */
    private static Map<String, List<XtNganhToHop>> loadNganhToHopMap() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<XtNganhToHop> list = session
                    .createQuery("from XtNganhToHop", XtNganhToHop.class)
                    .list();
            return list.stream()
                    .collect(Collectors.groupingBy(
                            n -> {
                                String maNganh = n.getMaNganh();
                                return normalize(maNganh);
                            }));
        } catch (Exception e) {
            System.err.println("[DiemCongImporter] Không load được bảng ngành-tổ hợp: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    /**
     * Load tất cả tổ hợp môn thi từ DB, map theo mã tổ hợp (in hoa)
     * Value là List của [mon1, mon2, mon3] (tất cả in hoa)
     */
    private static Map<String, List<String>> loadToHopMonMap() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<XtToHopMonThi> list = session
                    .createQuery("from XtToHopMonThi", XtToHopMonThi.class)
                    .list();
            Map<String, List<String>> result = new java.util.HashMap<>();
            for (XtToHopMonThi th : list) {
                List<String> mon = new ArrayList<>();
                if (th.getMon1() != null)
                    mon.add(th.getMon1().trim().toUpperCase());
                if (th.getMon2() != null)
                    mon.add(th.getMon2().trim().toUpperCase());
                if (th.getMon3() != null)
                    mon.add(th.getMon3().trim().toUpperCase());
                String maToHop = th.getMaToHop() != null ? th.getMaToHop().trim().toUpperCase() : "";
                result.put(normalize(maToHop), mon);
            }
            return result;
        } catch (Exception e) {
            System.err.println("[DiemCongImporter] Không load được bảng tổ hợp môn: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    private static void flushBatch(List<XtDiemCongXetTuyen> batch,
            Set<String> existingKeys,
            PrintWriter logWriter) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < batch.size(); i++) {
                session.persist(batch.get(i));
                if ((i + 1) % BATCH_SIZE == 0) {
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            tx.commit();

            // Cập nhật existingKeys
            for (XtDiemCongXetTuyen e : batch) {
                existingKeys.add(buildKey(e.getTsCccd(), e.getMaNganh(), e.getMaToHop()));
            }

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logWriter.println("[BATCH FAIL] " + e.getMessage());
            for (XtDiemCongXetTuyen entity : batch) {
                logWriter.printf("  - Bị rollback: CCCD=%s | Mã ngành=%s | Mã tổ hợp=%s%n",
                        entity.getTsCccd(), entity.getMaNganh(), entity.getMaToHop());
            }
        }
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private static String getString(Row row, int col) {
        if (row == null || col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield (val == Math.floor(val))
                        ? String.valueOf((long) val)
                        : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private static BigDecimal getBigDecimal(Row row, int col) {
        if (row == null || col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> {
                try {
                    String str = cell.getStringCellValue().trim();
                    if (str.isBlank())
                        yield null;
                    yield new BigDecimal(str);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    private static String buildKey(String cccd, String maNganh, String maToHop) {
        return (cccd != null ? cccd.toUpperCase() : "") + "|"
                + (maNganh != null ? maNganh.toUpperCase() : "") + "|"
                + (maToHop != null ? maToHop.toUpperCase() : "");
    }

    private static BigDecimal sum(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.add(right);
    }
}
