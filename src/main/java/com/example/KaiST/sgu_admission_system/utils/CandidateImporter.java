package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CandidateImporter {

    private static final int BATCH_SIZE = 50;

    // ── Kết quả trả về cho Controller hiển thị lên UI ────────────────────────
    public static class ImportResult {
        public final int totalSuccess;
        public final int totalSkipDuplicate;
        public final int totalSkipError;
        public final String logPath;

        public ImportResult(int totalSuccess, int totalSkipDuplicate, int totalSkipError, String logPath) {
            this.totalSuccess = totalSuccess;
            this.totalSkipDuplicate = totalSkipDuplicate;
            this.totalSkipError = totalSkipError;
            this.logPath = logPath;
        }
    }

    public static ImportResult importFromExcel(String filePath) {
        String logPath = filePath.replace(".xlsx", "_import_errors.log");

        int totalSuccess = 0;
        int totalSkipDuplicate = 0;
        int totalSkipError = 0;

        Set<String> existingCccds = loadExistingCccds();

        List<XtThiSinhXetTuyen25> candidateBatch = new ArrayList<>(BATCH_SIZE);
        List<XtDiemThiXetTuyen> diemBatch = new ArrayList<>(BATCH_SIZE);

        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook wb = new XSSFWorkbook(fis);
                PrintWriter logWriter = new PrintWriter(new FileWriter(logPath, true))) {

            logWriter.println("=== Import bắt đầu: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                    + " | File: " + filePath + " ===");
            logWriter.println("Số CCCD đã có trong DB: " + existingCccds.size());

            Sheet sheet = wb.getSheetAt(0);

            for (Row row : sheet) {
                int rowNum = row.getRowNum();
                if (rowNum == 0)
                    continue;
                if (isRowEmpty(row))
                    continue;

                String cccd = getString(row, 1);

                if (cccd == null || cccd.isBlank()) {
                    logWriter.printf("Dòng %d: Bỏ qua — CCCD trống%n", rowNum + 1);
                    totalSkipError++;
                    continue;
                }

                if (existingCccds.contains(cccd)) {
                    logWriter.printf("Dòng %d | CCCD=%s: Bỏ qua — đã tồn tại trong DB%n",
                            rowNum + 1, cccd);
                    totalSkipDuplicate++;
                    continue;
                }

                if (candidateBatch.stream().anyMatch(c -> cccd.equals(c.getCccd()))) {
                    logWriter.printf("Dòng %d | CCCD=%s: Bỏ qua — trùng trong file Excel%n",
                            rowNum + 1, cccd);
                    totalSkipDuplicate++;
                    continue;
                }

                try {
                    candidateBatch.add(buildCandidate(row, cccd));
                    diemBatch.add(buildDiem(row, cccd));
                    totalSuccess++;
                } catch (Exception e) {
                    logWriter.printf("Dòng %d | CCCD=%s: Lỗi parse — %s%n",
                            rowNum + 1, cccd, e.getMessage());
                    totalSkipError++;
                    continue;
                }

                if (candidateBatch.size() >= BATCH_SIZE) {
                    flushBatch(candidateBatch, diemBatch, existingCccds, logWriter);
                    candidateBatch.clear();
                    diemBatch.clear();
                }
            }

            if (!candidateBatch.isEmpty()) {
                flushBatch(candidateBatch, diemBatch, existingCccds, logWriter);
            }

            String summary = String.format(
                    "[CandidateImporter] Hoàn thành — Thành công: %d | Bỏ qua trùng: %d | Lỗi: %d",
                    totalSuccess, totalSkipDuplicate, totalSkipError);
            System.out.println(summary);
            logWriter.println(summary);

        } catch (Exception e) {
            System.err.println("[CandidateImporter] Lỗi nghiêm trọng: " + e.getMessage());
            e.printStackTrace();
        }

        return new ImportResult(totalSuccess, totalSkipDuplicate, totalSkipError, logPath);
    }

    private static Set<String> loadExistingCccds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<String> list = session
                    .createQuery("select cccd from XtThiSinhXetTuyen25", String.class)
                    .list();
            return new HashSet<>(list);
        } catch (Exception e) {
            System.err.println("[CandidateImporter] Không load được danh sách CCCD: " + e.getMessage());
            return new HashSet<>();
        }
    }

    private static void flushBatch(
            List<XtThiSinhXetTuyen25> candidates,
            List<XtDiemThiXetTuyen> diems,
            Set<String> existingCccds,
            PrintWriter logWriter) {

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < candidates.size(); i++) {
                session.persist(candidates.get(i));
                session.persist(diems.get(i));

                if ((i + 1) % BATCH_SIZE == 0) {
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            tx.commit();

            for (XtThiSinhXetTuyen25 c : candidates) {
                existingCccds.add(c.getCccd());
            }

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logWriter.println("[BATCH FAIL] " + e.getMessage());
            for (XtThiSinhXetTuyen25 c : candidates) {
                logWriter.println("  - CCCD bị rollback: " + c.getCccd());
            }
        }
    }

    // ── Builder methods ───────────────────────────────────────────────────────

    private static XtThiSinhXetTuyen25 buildCandidate(Row row, String cccd) {
        XtThiSinhXetTuyen25 c = new XtThiSinhXetTuyen25();
        c.setCccd(cccd);

        String hoTen = getString(row, 2);
        if (hoTen != null) {
            String[] parts = hoTen.trim().split("\\s+");
            c.setTen(parts[parts.length - 1]);
        }

        c.setNgaySinh(parseDateString(row, 3));
        c.setGioiTinh(getString(row, 4));
        c.setDoiTuong(getString(row, 5));
        c.setKhuVuc(getString(row, 6));
        c.setNoiSinh(getString(row, 35));
        return c;
    }

    private static XtDiemThiXetTuyen buildDiem(Row row, String cccd) {
        XtDiemThiXetTuyen d = new XtDiemThiXetTuyen();
        d.setCccd(cccd);
        d.setPhuongThuc(PhuongThuc.THPT);
        d.setTo(parseDiem(row, 7));
        d.setVa(parseDiem(row, 8));
        d.setLi(parseDiem(row, 9));
        d.setHo(parseDiem(row, 10));
        d.setSi(parseDiem(row, 11));
        d.setSu(parseDiem(row, 12));
        d.setDi(parseDiem(row, 13));

        String maMon = getString(row, 16);
        if ("N1".equalsIgnoreCase(maMon)) {
            d.setN1Thi(parseDiem(row, 15));
        }

        d.setKtpl(parseDiem(row, 17));
        d.setTi(parseDiem(row, 18));
        d.setCncn(parseDiem(row, 19));
        d.setCnnn(parseDiem(row, 20));
        d.setNk1(parseDiem(row, 22));
        d.setNk2(parseDiem(row, 23));
        return d;
    }

    // ── Tiện ích ─────────────────────────────────────────────────────────────

    static String getString(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    static BigDecimal parseDiem(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return BigDecimal.valueOf(cell.getNumericCellValue());
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim().replace(",", ".");
                return s.isBlank() ? null : new BigDecimal(s);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    static String parseDateString(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
                return new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
            if (cell.getCellType() == CellType.STRING)
                return cell.getStringCellValue().trim();
        } catch (Exception ignored) {
        }
        return null;
    }

    static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (Cell cell : row)
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        return true;
    }
}