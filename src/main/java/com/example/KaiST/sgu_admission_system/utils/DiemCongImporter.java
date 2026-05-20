package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Import bảng điểm cộng xét tuyển từ file Excel.
 */
public class DiemCongImporter {
    private static final int BATCH_SIZE = 50;
    private static final String SHEET_NAME = "Sheet1";

    public static class ImportResult {
        public int totalSuccess;
        public int totalSkipDuplicate;
        public int totalSkipError;
        public String logPath;

        public ImportResult(int success, int duplicate, int error, String log) {
            totalSuccess = success;
            totalSkipDuplicate = duplicate;
            totalSkipError = error;
            logPath = log;
        }
    }

    public static ImportResult importFromExcel(String filePath) {
        String logPath = filePath.replace(".xlsx", "_diem_cong.log");
        int success = 0, duplicate = 0, error = 0;

        Set<String> existingKeys = loadExistingKeys();
        Map<String, XtNganh> nganhMap = loadNganhMap();
        Map<String, List<XtNganhToHop>> toHopMap = loadNganhToHopMap();
        Map<String, List<String>> monMap = loadToHopMonMap();

        List<XtDiemCongXetTuyen> batch = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis);
             PrintWriter log = new PrintWriter(new FileWriter(logPath, true))) {

            log.println("=== Import: " + LocalDateTime.now() + " | " + filePath + " ===");
            Sheet sheet = wb.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.println("[ERROR] Sheet not found");
                return new ImportResult(0, 0, 0, logPath);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) continue;

                String cccd = getString(row, 1);
                String tenNganh = getString(row, 2);
                String monDatGiai = getString(row, 3);
                BigDecimal diemMon = getBigDecimal(row, 4);
                BigDecimal diemThxt = getBigDecimal(row, 5);

                if (cccd == null || tenNganh == null) {
                    error++;
                    continue;
                }

                XtNganh nganh = nganhMap.get(tenNganh.toUpperCase());
                if (nganh == null) {
                    log.printf("Row %d: Nganh '%s' not found%n", i, tenNganh);
                    error++;
                    continue;
                }

                String maNganh = nganh.getMaNganh();
                List<XtNganhToHop> toHops = toHopMap.getOrDefault(maNganh, new ArrayList<>());

                for (XtNganhToHop th : toHops) {
                    String maToHop = th.getMaToHop();
                    String key = buildKey(cccd, maNganh, maToHop);

                    if (existingKeys.contains(key)) {
                        duplicate++;
                        continue;
                    }

                    BigDecimal diemUtxt = calculateScore(monDatGiai, maToHop, diemMon, diemThxt, monMap);
                    
                    XtDiemCongXetTuyen entity = new XtDiemCongXetTuyen();
                    entity.setTsCccd(cccd);
                    entity.setMaNganh(maNganh);
                    entity.setMaToHop(maToHop);
                    entity.setDiemCc(diemMon);
                    entity.setDiemUtxt(diemUtxt);
                    entity.setDiemTong(sum(diemMon, diemUtxt));
                    entity.setPhuongThuc("THPT");
                    batch.add(entity);
                    success++;
                    
                    if (batch.size() >= BATCH_SIZE) {
                        flushBatch(batch, existingKeys, log);
                        batch.clear();
                    }
                }
            }

            if (!batch.isEmpty()) {
                flushBatch(batch, existingKeys, log);
            }

            log.printf("[RESULT] Success: %d, Duplicate: %d, Error: %d%n", success, duplicate, error);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ImportResult(success, duplicate, error, logPath);
    }

    private static BigDecimal calculateScore(String monDatGiai, String maToHop, BigDecimal diemMon, BigDecimal diemThxt, Map<String, List<String>> monMap) {
        if (monDatGiai == null || monDatGiai.isBlank()) {
            return diemThxt;
        }

        String maMon = convertMonName(monDatGiai.trim());
        if (maMon == null) return diemThxt;

        List<String> mons = monMap.getOrDefault(maToHop, new ArrayList<>());
        boolean match = mons.stream().anyMatch(m -> m != null && m.equalsIgnoreCase(maMon));
        
        return match ? diemMon : diemThxt;
    }

    private static String convertMonName(String name) {
        String upper = name.toUpperCase();
        return switch(upper) {
            case "TIẾNG ANH", "ANH", "N1" -> "N1";
            case "TOÁN", "TO" -> "TO";
            case "LỊCH SỬ", "SU" -> "SU";
            case "ĐỊA LÝ", "DI" -> "DI";
            case "HÓA HỌC", "HO" -> "HO";
            case "VẬT LÝ", "VA" -> "VA";
            case "SINH HỌC", "SI" -> "SI";
            case "TIẾNG VIỆT", "VI" -> "VI";
            default -> upper.length() == 2 ? upper : null;
        };
    }

    private static Set<String> loadExistingKeys() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = s.createQuery("select e.tsCccd, e.maNganh, e.maToHop from XtDiemCongXetTuyen e", Object[].class).list();
            Set<String> keys = new HashSet<>();
            for (Object[] r : rows) {
                keys.add(buildKey(r[0] != null ? r[0].toString() : "", r[1] != null ? r[1].toString() : "", r[2] != null ? r[2].toString() : ""));
            }
            return keys;
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private static Map<String, XtNganh> loadNganhMap() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from XtNganh", XtNganh.class).list().stream()
                    .collect(Collectors.toMap(n -> n.getTenNganh() != null ? n.getTenNganh().toUpperCase() : "", n -> n, (a, b) -> a));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static Map<String, List<XtNganhToHop>> loadNganhToHopMap() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from XtNganhToHop", XtNganhToHop.class).list().stream()
                    .collect(Collectors.groupingBy(n -> n.getMaNganh() != null ? n.getMaNganh().toUpperCase() : ""));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static Map<String, List<String>> loadToHopMonMap() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Map<String, List<String>> result = new HashMap<>();
            for (XtToHopMonThi t : s.createQuery("from XtToHopMonThi", XtToHopMonThi.class).list()) {
                List<String> mons = new ArrayList<>();
                if (t.getMon1() != null) mons.add(t.getMon1().trim());
                if (t.getMon2() != null) mons.add(t.getMon2().trim());
                if (t.getMon3() != null) mons.add(t.getMon3().trim());
                result.put(t.getMaToHop() != null ? t.getMaToHop().trim() : "", mons);
            }
            return result;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void flushBatch(List<XtDiemCongXetTuyen> batch, Set<String> keys, PrintWriter log) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            for (int i = 0; i < batch.size(); i++) {
                s.persist(batch.get(i));
                if ((i + 1) % BATCH_SIZE == 0) {
                    s.flush();
                    s.clear();
                }
            }
            s.flush();
            tx.commit();
            for (XtDiemCongXetTuyen e : batch) {
                keys.add(buildKey(e.getTsCccd(), e.getMaNganh(), e.getMaToHop()));
            }
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.println("[BATCH ERROR] " + e.getMessage());
        }
    }

    private static String buildKey(String cccd, String maNganh, String maToHop) {
        return (cccd != null ? cccd.toUpperCase() : "") + "|" + (maNganh != null ? maNganh.toUpperCase() : "") + "|" + (maToHop != null ? maToHop.toUpperCase() : "");
    }

    private static String getString(Row row, int col) {
        if (row == null || col >= row.getLastCellNum()) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch(cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long)cell.getNumericCellValue());
            default -> null;
        };
    }

    private static BigDecimal getBigDecimal(Row row, int col) {
        if (row == null || col >= row.getLastCellNum()) return null;
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return switch(cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> new BigDecimal(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private static BigDecimal sum(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) return null;
        if (left == null) return right;
        if (right == null) return left;
        return left.add(right);
    }
}
