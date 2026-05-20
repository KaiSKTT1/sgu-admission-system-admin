package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import tổ hợp xét tuyển từ file Excel (.xlsx) vào bảng xt_nganh_tohop.
 * Sau khi import xong, cập nhật n_tohopgoc trong xt_nganh theo cột "Gốc".
 *
 * ── Cấu trúc file Excel (Sheet "Sheet1") ────────────────────────────────────
 * Dòng 0 (header): STT | MANGANH | TEN_NGANHCHUAN | MA_TO_HOP | tb_keys |
 * TEN_TO_HOP | Gốc | Độ lệch
 * Dữ liệu từ dòng 1.
 *
 * Col 0: STT
 * Col 1: MANGANH → maNganh
 * Col 2: TEN_NGANH (bỏ qua)
 * Col 3: MA_TO_HOP → parse tên tổ hợp + hệ số từng môn
 * Định dạng: "D01(TO-3,VA-3,N1-1)" hoặc "X01(TO-3,VA-3,KTPL-1)"
 * Col 4: tb_keys → tbKeys (ví dụ: "7140114_D01") — UNIQUE KEY trong DB
 * Col 5: TEN_TO_HOP (bỏ qua)
 * Col 6: Gốc → nếu = "Gốc" thì maToHop này là n_tohopgoc của ngành
 * Col 7: Độ lệch → doLech (có thể là "1,62" hoặc 0)
 *
 * ── Cập nhật n_tohopgoc ─────────────────────────────────────────────────────
 * Sau khi insert xong xt_nganh_tohop, duyệt lại Map<maNganh, maToHopGoc>
 * thu thập được trong quá trình đọc file, rồi UPDATE xt_nganh.n_tohopgoc.
 * Nếu ngành chưa có trong xt_nganh → tạo mới bản ghi tối thiểu (chỉ maNganh
 * + n_tohopgoc) để đảm bảo fillTtThmFromNganhGoc() hoạt động được.
 *
 * ── Kiểm tra trùng ──────────────────────────────────────────────────────────
 * Dùng cả 2 key:
 * - compositeKey = maNganh + "|" + maToHop
 * - tbKeys (unique key riêng, fallback = maNganh + "_" + maToHop nếu trống)
 */
public class NganhToHopImporter {

    private static final int BATCH_SIZE = 50;
    private static final String SHEET_NAME = "Sheet1";
    private static final int DATA_START_IDX = 1; // 0-based, sau header

    // Chỉ số cột
    private static final int COL_MANGANH = 1;
    private static final int COL_MA_TO_HOP = 3;
    private static final int COL_TB_KEYS = 4;
    private static final int COL_GOC = 6; // "Gốc" = tổ hợp gốc của ngành
    private static final int COL_DO_LECH = 7;

    // Pattern parse: "D01(TO-3,VA-3,N1-1)"
    private static final Pattern TOHOP_PATTERN = Pattern.compile("^(\\w+)\\((.+)\\)$");

    // ── Kết quả ──────────────────────────────────────────────────────────────

    public static class ImportResult {
        public final int totalSuccess;
        public final int totalSkipDuplicate;
        public final int totalSkipError;
        public final int totalNganhUpdated; // số ngành được cập nhật n_tohopgoc
        public final String logPath;

        public ImportResult(int s, int d, int e, int n, String l) {
            totalSuccess = s;
            totalSkipDuplicate = d;
            totalSkipError = e;
            totalNganhUpdated = n;
            logPath = l;
        }
    }

    // ── Entry point ──────────────────────────────────────────────────────────

    public static ImportResult importFromExcel(String filePath) {
        String logPath = filePath.replace(".xlsx", "_nganh_tohop_import.log");
        int totalSuccess = 0, totalSkipDuplicate = 0, totalSkipError = 0;

        // Load cả 2 tập key đã có trong DB
        Set<String> existingCompositeKeys = loadExistingCompositeKeys();
        Set<String> existingTbKeys = loadExistingTbKeys();

        // Map thu thập tổ hợp gốc: maNganh → maToHop (chỉ dòng có cột Gốc = "Gốc")
        Map<String, String> tohopGocMap = new LinkedHashMap<>();

        List<XtNganhToHop> batch = new ArrayList<>(BATCH_SIZE);

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook wb = new XSSFWorkbook(fis);
                PrintWriter log = new PrintWriter(new FileWriter(logPath, true))) {

            log.println("=== Import bắt đầu: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    + " | File: " + filePath + " ===");
            log.println("Số bản ghi đã có trong DB: " + existingCompositeKeys.size());

            Sheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.println("[LỖI] Không tìm thấy sheet '" + SHEET_NAME + "'.");
                return new ImportResult(0, 0, 0, 0, logPath);
            }

            for (int rowIdx = DATA_START_IDX; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (isRowEmpty(row))
                    continue;

                // ── Đọc mã ngành ────────────────────────────────────────────
                String maNganh = getString(row, COL_MANGANH);
                if (maNganh == null || maNganh.isBlank()) {
                    log.printf("Dòng %d: Bỏ qua — mã ngành trống%n", rowIdx + 1);
                    totalSkipError++;
                    continue;
                }

                // ── Đọc & validate tb_keys ────────────────────────────────────
                String tbKeys = getString(row, COL_TB_KEYS);
                if (tbKeys == null || tbKeys.isBlank()) {
                    String maToHopRaw = getString(row, COL_MA_TO_HOP);
                    String maCode = (maToHopRaw != null && maToHopRaw.contains("("))
                            ? maToHopRaw.substring(0, maToHopRaw.indexOf('(')).trim()
                            : maToHopRaw;
                    tbKeys = maNganh + "_" + (maCode != null ? maCode : "UNKNOWN");
                    log.printf("Dòng %d | Ngành=%s: tb_keys trống — tự tạo fallback: %s%n",
                            rowIdx + 1, maNganh, tbKeys);
                }
                final String tbKeysFinal = tbKeys;

                // ── Đọc & parse MA_TO_HOP ────────────────────────────────────
                String maToHopRaw = getString(row, COL_MA_TO_HOP);
                if (maToHopRaw == null || maToHopRaw.isBlank()) {
                    log.printf("Dòng %d | Ngành=%s: Bỏ qua — MA_TO_HOP trống%n",
                            rowIdx + 1, maNganh);
                    totalSkipError++;
                    continue;
                }

                ParsedToHop parsed;
                try {
                    parsed = parseToHop(maToHopRaw);
                } catch (Exception ex) {
                    log.printf("Dòng %d | Ngành=%s | Raw=%s: Lỗi parse — %s%n",
                            rowIdx + 1, maNganh, maToHopRaw, ex.getMessage());
                    totalSkipError++;
                    continue;
                }

                // ── Đọc cột Gốc → thu thập n_tohopgoc ───────────────────────
                String goc = getString(row, COL_GOC);
                if ("Gốc".equalsIgnoreCase(goc != null ? goc.trim() : "")) {
                    // Ưu tiên dòng đầu tiên nếu ngành có nhiều dòng "Gốc" (không hợp lệ)
                    tohopGocMap.putIfAbsent(maNganh, parsed.maToHop);
                    log.printf("Dòng %d | Ngành=%s | ToHop=%s: Đánh dấu là tổ hợp GỐC%n",
                            rowIdx + 1, maNganh, parsed.maToHop);
                }

                // ── Kiểm tra trùng theo compositeKey ─────────────────────────
                String compositeKey = buildCompositeKey(maNganh, parsed.maToHop);
                if (existingCompositeKeys.contains(compositeKey)) {
                    log.printf("Dòng %d | Ngành=%s | ToHop=%s: Bỏ qua — composite key đã tồn tại trong DB%n",
                            rowIdx + 1, maNganh, parsed.maToHop);
                    totalSkipDuplicate++;
                    continue;
                }

                // ── Kiểm tra trùng theo tb_keys ──────────────────────────────
                if (existingTbKeys.contains(tbKeysFinal)) {
                    log.printf("Dòng %d | Ngành=%s | tb_keys=%s: Bỏ qua — tb_keys đã tồn tại trong DB%n",
                            rowIdx + 1, maNganh, tbKeysFinal);
                    totalSkipDuplicate++;
                    continue;
                }

                // ── Kiểm tra trùng trong batch ────────────────────────────────
                boolean dupInBatch = batch.stream()
                        .anyMatch(e -> compositeKey.equals(buildCompositeKey(e.getMaNganh(), e.getMaToHop()))
                                || tbKeysFinal.equals(e.getTbKeys()));
                if (dupInBatch) {
                    log.printf("Dòng %d | Ngành=%s | ToHop=%s: Bỏ qua — trùng trong file%n",
                            rowIdx + 1, maNganh, parsed.maToHop);
                    totalSkipDuplicate++;
                    continue;
                }

                // ── Build entity ─────────────────────────────────────────────
                BigDecimal doLech = parseDoLech(getString(row, COL_DO_LECH));
                XtNganhToHop entity = buildEntity(maNganh, parsed, tbKeysFinal, doLech);
                batch.add(entity);

                existingCompositeKeys.add(compositeKey);
                existingTbKeys.add(tbKeysFinal);
                totalSuccess++;

                if (batch.size() >= BATCH_SIZE) {
                    flushBatch(batch, log);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(batch, log);
            }

            // ── Cập nhật n_tohopgoc vào xt_nganh ─────────────────────────────
            int nganhUpdated = updateToHopGoc(tohopGocMap, log);

            String summary = String.format(
                    "[NganhToHopImporter] Hoàn thành — Thành công: %d | Trùng: %d | Lỗi: %d | Ngành cập nhật n_tohopgoc: %d",
                    totalSuccess, totalSkipDuplicate, totalSkipError, nganhUpdated);
            System.out.println(summary);
            log.println(summary);

            return new ImportResult(totalSuccess, totalSkipDuplicate, totalSkipError, nganhUpdated, logPath);

        } catch (Exception e) {
            System.err.println("[NganhToHopImporter] Lỗi nghiêm trọng: " + e.getMessage());
            e.printStackTrace();
        }

        return new ImportResult(totalSuccess, totalSkipDuplicate, totalSkipError, 0, logPath);
    }

    // ── Cập nhật n_tohopgoc vào xt_nganh ────────────────────────────────────

    /**
     * Với mỗi maNganh trong tohopGocMap:
     * - Nếu bản ghi xt_nganh đã tồn tại → UPDATE n_tohopgoc
     * - Nếu chưa tồn tại → INSERT bản ghi tối thiểu (maNganh + n_tohopgoc)
     * để fillTtThmFromNganhGoc() hoạt động đúng.
     *
     * @return số ngành được xử lý (update hoặc insert)
     */
    private static int updateToHopGoc(Map<String, String> tohopGocMap, PrintWriter log) {
        if (tohopGocMap.isEmpty()) {
            log.println("[n_tohopgoc] Không có dòng nào đánh dấu 'Gốc' trong file.");
            return 0;
        }

        int count = 0;
        log.printf("[n_tohopgoc] Bắt đầu cập nhật %d ngành...%n", tohopGocMap.size());

        for (Map.Entry<String, String> entry : tohopGocMap.entrySet()) {
            String maNganh = entry.getKey();
            String maToHop = entry.getValue();

            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();

                // Kiểm tra xt_nganh đã có bản ghi chưa
                XtNganh existing = session.createQuery(
                        "from XtNganh where maNganh = :ma", XtNganh.class)
                        .setParameter("ma", maNganh)
                        .uniqueResult();

                if (existing != null) {
                    // UPDATE n_tohopgoc
                    existing.setToHopGoc(maToHop);
                    session.merge(existing);
                    log.printf("[n_tohopgoc] UPDATE  ngành=%s → n_tohopgoc=%s%n", maNganh, maToHop);
                } else {
                    // INSERT bản ghi tối thiểu
                    XtNganh newNganh = XtNganh.builder()
                            .maNganh(maNganh)
                            .toHopGoc(maToHop)
                            .build();
                    session.persist(newNganh);
                    log.printf("[n_tohopgoc] INSERT   ngành=%s → n_tohopgoc=%s (bản ghi mới)%n",
                            maNganh, maToHop);
                }

                tx.commit();
                count++;
            } catch (Exception e) {
                if (tx != null)
                    tx.rollback();
                log.printf("[n_tohopgoc] LỖI ngành=%s: %s%n", maNganh, e.getMessage());
                System.err.printf("[NganhToHopImporter] Lỗi cập nhật n_tohopgoc ngành=%s: %s%n",
                        maNganh, e.getMessage());
            }
        }

        log.printf("[n_tohopgoc] Hoàn thành — %d/%d ngành được cập nhật%n",
                count, tohopGocMap.size());
        return count;
    }

    // ── Parse "D01(TO-3,VA-3,N1-1)" ─────────────────────────────────────────

    private static ParsedToHop parseToHop(String raw) {
        Matcher m = TOHOP_PATTERN.matcher(raw.trim());
        if (!m.matches())
            throw new IllegalArgumentException("Định dạng không hợp lệ: " + raw);

        ParsedToHop p = new ParsedToHop();
        p.maToHop = m.group(1).trim();

        String[] parts = m.group(2).split(",");
        if (parts.length < 1 || parts.length > 3)
            throw new IllegalArgumentException("Cần 1–3 môn, nhận được: " + parts.length);

        for (int i = 0; i < parts.length; i++) {
            String[] kv = parts[i].trim().split("-");
            if (kv.length != 2)
                throw new IllegalArgumentException("Sai định dạng môn-hệsố: " + parts[i]);
            String mon = kv[0].trim().toUpperCase();
            int hs;
            try {
                hs = Integer.parseInt(kv[1].trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Hệ số không hợp lệ: " + kv[1]);
            }
            switch (i) {
                case 0 -> {
                    p.thMon1 = mon;
                    p.hsMon1 = hs;
                }
                case 1 -> {
                    p.thMon2 = mon;
                    p.hsMon2 = hs;
                }
                case 2 -> {
                    p.thMon3 = mon;
                    p.hsMon3 = hs;
                }
            }
        }

        Set<String> monSet = new HashSet<>();
        if (p.thMon1 != null)
            monSet.add(p.thMon1);
        if (p.thMon2 != null)
            monSet.add(p.thMon2);
        if (p.thMon3 != null)
            monSet.add(p.thMon3);

        p.flagTO = monSet.contains("TO");
        p.flagLI = monSet.contains("LI");
        p.flagHO = monSet.contains("HO");
        p.flagSI = monSet.contains("SI");
        p.flagVA = monSet.contains("VA");
        p.flagSU = monSet.contains("SU");
        p.flagDI = monSet.contains("DI");
        p.flagTI = monSet.contains("TI");
        p.flagKTPL = monSet.contains("KTPL");
        p.flagN1 = monSet.contains("N1");
        Set<String> STANDARD = Set.of("TO", "VA", "LI", "HO", "SI", "SU", "DI", "TI", "KTPL", "N1");
        p.flagKHAC = monSet.stream().anyMatch(mn -> !STANDARD.contains(mn));

        return p;
    }

    // ── Build entity ─────────────────────────────────────────────────────────

    private static XtNganhToHop buildEntity(String maNganh, ParsedToHop p,
            String tbKeys, BigDecimal doLech) {
        return XtNganhToHop.builder()
                .maNganh(maNganh)
                .maToHop(p.maToHop)
                .thMon1(p.thMon1).hsMon1(p.hsMon1)
                .thMon2(p.thMon2).hsMon2(p.hsMon2)
                .thMon3(p.thMon3).hsMon3(p.hsMon3)
                .tbKeys(tbKeys)
                .n1(p.flagN1)
                .to(p.flagTO)
                .li(p.flagLI)
                .ho(p.flagHO)
                .si(p.flagSI)
                .va(p.flagVA)
                .su(p.flagSU)
                .di(p.flagDI)
                .ti(p.flagTI)
                .ktpl(p.flagKTPL)
                .khac(p.flagKHAC)
                .doLech(doLech)
                .build();
    }

    // ── Hibernate helpers ────────────────────────────────────────────────────

    private static Set<String> loadExistingCompositeKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                    "select e.maNganh, e.maToHop from XtNganhToHop e",
                    Object[].class).list();
            Set<String> keys = new HashSet<>(rows.size() * 2);
            for (Object[] r : rows)
                keys.add(buildCompositeKey(str(r[0]), str(r[1])));
            return keys;
        } catch (Exception e) {
            System.err.println("[NganhToHopImporter] Không load được composite keys: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static Set<String> loadExistingTbKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<String> rows = session.createQuery(
                    "select e.tbKeys from XtNganhToHop e where e.tbKeys is not null",
                    String.class).list();
            return new HashSet<>(rows);
        } catch (Exception e) {
            System.err.println("[NganhToHopImporter] Không load được tb_keys: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static void flushBatch(List<XtNganhToHop> batch, PrintWriter log) {
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
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            log.println("[BATCH FAIL] " + e.getMessage());
            for (XtNganhToHop entity : batch)
                log.printf("  - Rollback: Ngành=%s | ToHop=%s | tb_keys=%s%n",
                        entity.getMaNganh(), entity.getMaToHop(), entity.getTbKeys());
        }
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    private static BigDecimal parseDoLech(String raw) {
        if (raw == null || raw.isBlank() || raw.equals("0"))
            return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

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

    private static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (Cell cell : row)
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        return true;
    }

    private static String buildCompositeKey(String maNganh, String maToHop) {
        return (maNganh != null ? maNganh.trim() : "") + "|"
                + (maToHop != null ? maToHop.trim().toUpperCase() : "");
    }

    private static String str(Object o) {
        return o != null ? o.toString().trim() : null;
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    private static class ParsedToHop {
        String maToHop;
        String thMon1, thMon2, thMon3;
        int hsMon1 = 1, hsMon2 = 1, hsMon3 = 1;
        boolean flagTO, flagLI, flagHO, flagSI, flagVA,
                flagSU, flagDI, flagTI, flagKTPL, flagN1, flagKHAC;
    }
}