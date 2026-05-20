package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Import nguyện vọng xét tuyển từ file Excel (.xlsx) vào bảng
 * xt_nguyenvongxettuyen.
 *
 * Cấu trúc file Excel (Sheet "Sheet2"):
 * - Dòng header: STT | CCCD | Thứ tự NV | Mã trường | Tên trường | Mã xét tuyển
 * | Tên mã xét tuyển | Nguyện vọng tuyển thẳng(điều 8)
 * - Col 0: STT
 * - Col 1: CCCD → nn_cccd
 * - Col 2: Thứ tự NV → nv_tt
 * - Col 3: Mã trường (bỏ qua)
 * - Col 4: Tên trường (bỏ qua)
 * - Col 5: Mã xét tuyển → nv_manganh
 * - Col 6: Tên mã xét tuyển (bỏ qua)
 * - Col 7: Nguyện vọng tuyển thẳng (bỏ qua)
 *
 * Mặc định: tt_phuongthuc = "THPT"
 * Các trường không có trong Excel để null: diem_thxt, diem_utqd, diem_cong,
 * diem_xettuyen, nv_ketqua, nv_keys, tt_thm
 */
public class NguyenVongImporter {

    private static final int BATCH_SIZE = 50;
    private static final String SHEET_NAME = "Sheet2";
    private static final int HEADER_ROW_IDX = 4; // 0-based, dòng chứa "STT | CCCD | ..."
    private static final int DATA_START_IDX = 5; // 0-based, dòng dữ liệu đầu tiên

    // Chỉ số cột (0-based)
    private static final int COL_CCCD = 1;
    private static final int COL_NV_TT = 2;
    private static final int COL_MA_NGANH = 5;

    private static final String DEFAULT_PHUONG_THUC = "THPT";

    // ── Kết quả trả về cho nơi gọi (hiển thị lên UI hoặc log) ───────────────
    public static class ImportResult {
        public final int totalSuccess;
        public final int totalSkipDuplicate;
        public final int totalSkipError;
        public final String logPath;

        public ImportResult(int totalSuccess, int totalSkipDuplicate,
                int totalSkipError, String logPath) {
            this.totalSuccess = totalSuccess;
            this.totalSkipDuplicate = totalSkipDuplicate;
            this.totalSkipError = totalSkipError;
            this.logPath = logPath;
        }
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    public static ImportResult importFromExcel(String filePath) {
        String logPath = filePath.replace(".xlsx", "_nguyen_vong_import.log");

        int totalSuccess = 0;
        int totalSkipDuplicate = 0;
        int totalSkipError = 0;

        // Tải trước các (cccd + manganh + nvTt) đã tồn tại để kiểm tra trùng
        Set<String> existingKeys = loadExistingKeys();

        List<XtNguyenVongXetTuyen> batch = new ArrayList<>(BATCH_SIZE);

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook wb = new XSSFWorkbook(fis);
                PrintWriter logWriter = new PrintWriter(new FileWriter(logPath, true))) {

            logWriter.println("=== Import bắt đầu: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    + " | File: " + filePath + " ===");
            logWriter.println("Số bản ghi đã có trong DB: " + existingKeys.size());

            Sheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) {
                logWriter.println("[LỖI] Không tìm thấy sheet '" + SHEET_NAME + "' trong file.");
                return new ImportResult(0, 0, 0, logPath);
            }

            for (int rowIdx = DATA_START_IDX; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isRowEmpty(row))
                    continue;

                // ── Đọc CCCD ────────────────────────────────────────────────
                String cccd = getString(row, COL_CCCD);
                if (cccd == null || cccd.isBlank()) {
                    logWriter.printf("Dòng %d: Bỏ qua — CCCD trống%n", rowIdx + 1);
                    totalSkipError++;
                    continue;
                }
                cccd = cccd.trim();

                // ── Đọc các cột còn lại ──────────────────────────────────────
                Integer nvTt = getInt(row, COL_NV_TT);
                String maNganh = getString(row, COL_MA_NGANH);
                if (maNganh != null)
                    maNganh = maNganh.trim();

                // Key trùng = cccd + maNganh + nvTt (một thí sinh có thể đăng ký nhiều ngành)
                String uniqueKey = buildKey(cccd, maNganh, nvTt);

                if (existingKeys.contains(uniqueKey)) {
                    logWriter.printf("Dòng %d | CCCD=%s | Ngành=%s | TT=%s: Bỏ qua — đã tồn tại%n",
                            rowIdx + 1, cccd, maNganh, nvTt);
                    totalSkipDuplicate++;
                    continue;
                }

                // Kiểm tra trùng ngay trong batch hiện tại
                final String keyFinal = uniqueKey;
                if (batch.stream().anyMatch(e -> keyFinal.equals(
                        buildKey(e.getNnCccd(), e.getNvMaNganh(), e.getNvTt())))) {
                    logWriter.printf("Dòng %d | CCCD=%s | Ngành=%s | TT=%s: Bỏ qua — trùng trong file%n",
                            rowIdx + 1, cccd, maNganh, nvTt);
                    totalSkipDuplicate++;
                    continue;
                }

                // ── Build entity ─────────────────────────────────────────────
                try {
                    XtNguyenVongXetTuyen entity = buildEntity(cccd, nvTt, maNganh);
                    batch.add(entity);
                    totalSuccess++;
                } catch (Exception e) {
                    logWriter.printf("Dòng %d | CCCD=%s: Lỗi parse — %s%n",
                            rowIdx + 1, cccd, e.getMessage());
                    totalSkipError++;
                    continue;
                }

                // ── Flush khi đủ batch ───────────────────────────────────────
                if (batch.size() >= BATCH_SIZE) {
                    flushBatch(batch, existingKeys, logWriter);
                    batch.clear();
                }
            }

            // Flush phần còn lại
            if (!batch.isEmpty()) {
                flushBatch(batch, existingKeys, logWriter);
            }

            String summary = String.format(
                    "[NguyenVongImporter] Hoàn thành — Thành công: %d | Bỏ qua trùng: %d | Lỗi: %d",
                    totalSuccess, totalSkipDuplicate, totalSkipError);
            System.out.println(summary);
            logWriter.println(summary);

        } catch (Exception e) {
            System.err.println("[NguyenVongImporter] Lỗi nghiêm trọng: " + e.getMessage());
            e.printStackTrace();
        }

        return new ImportResult(totalSuccess, totalSkipDuplicate, totalSkipError, logPath);
    }

    // ── Hibernate helpers ────────────────────────────────────────────────────

    /**
     * Tải tập hợp key (cccd|manganh|nvTt) đã có trong DB để kiểm tra trùng.
     */
    private static Set<String> loadExistingKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session
                    .createQuery(
                            "select e.nnCccd, e.nvMaNganh, e.nvTt from XtNguyenVongXetTuyen e",
                            Object[].class)
                    .list();
            Set<String> keys = new HashSet<>(rows.size() * 2);
            for (Object[] r : rows) {
                keys.add(buildKey(
                        r[0] != null ? r[0].toString() : "",
                        r[1] != null ? r[1].toString() : "",
                        r[2] != null ? ((Number) r[2]).intValue() : null));
            }
            return keys;
        } catch (Exception e) {
            System.err.println("[NguyenVongImporter] Không load được danh sách key: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static void flushBatch(List<XtNguyenVongXetTuyen> batch,
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

            // Cập nhật existingKeys sau khi lưu thành công
            for (XtNguyenVongXetTuyen e : batch) {
                existingKeys.add(buildKey(e.getNnCccd(), e.getNvMaNganh(), e.getNvTt()));
            }

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logWriter.println("[BATCH FAIL] " + e.getMessage());
            for (XtNguyenVongXetTuyen entity : batch) {
                logWriter.printf("  - Bị rollback: CCCD=%s | Ngành=%s | TT=%s%n",
                        entity.getNnCccd(), entity.getNvMaNganh(), entity.getNvTt());
            }
        }
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    private static XtNguyenVongXetTuyen buildEntity(String cccd, Integer nvTt, String maNganh) {
        XtNguyenVongXetTuyen e = new XtNguyenVongXetTuyen();
        e.setNnCccd(cccd);
        e.setNvTt(nvTt);
        e.setNvMaNganh(maNganh);
        e.setTtPhuongThuc(DEFAULT_PHUONG_THUC);
        // Các trường không có trong Excel → null (để trống)
        e.setDiemThxt(null);
        e.setDiemUtqd(null);
        e.setDiemCong(null);
        e.setDiemXetTuyen(null);
        e.setNvKetQua(null);
        e.setNvKeys(null);
        e.setTtThm(null);
        return e;
    }

    // ── Tiện ích đọc cell ────────────────────────────────────────────────────

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
                // Mã ngành có thể là số (vd: 7140217) → không để dấu .0
                yield (val == Math.floor(val))
                        ? String.valueOf((long) val)
                        : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private static Integer getInt(Row row, int col) {
        if (row == null || col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().trim());
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
        for (Cell cell : row)
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        return true;
    }

    private static String buildKey(String cccd, String maNganh, Integer nvTt) {
        return (cccd != null ? cccd.toUpperCase() : "") + "|"
                + (maNganh != null ? maNganh.toUpperCase() : "") + "|"
                + (nvTt != null ? nvTt : "");
    }
}