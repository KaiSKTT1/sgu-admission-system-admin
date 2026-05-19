package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NganhImporter {

    // ── Kết quả trả về cho Controller ────────────────────────────────────────
    public static class ImportResult {
        public final int totalInserted; // ngành mới được tạo
        public final int totalUpdated; // ngành đã tồn tại được cập nhật
        public final int totalSkipError; // dòng bị lỗi parse
        public final List<String> errorMessages;

        public ImportResult(int totalInserted, int totalUpdated, int totalSkipError,
                List<String> errorMessages) {
            this.totalInserted = totalInserted;
            this.totalUpdated = totalUpdated;
            this.totalSkipError = totalSkipError;
            this.errorMessages = errorMessages;
        }
    }

    /**
     * Import đồng thời 2 file:
     * - nguongDauvaoPath : chứa mã ngành, tên ngành, điểm sàn (n_diemsan)
     * - chiTieuPath : chứa mã ngành, chỉ tiêu (n_chitieu)
     *
     * Với mỗi mã ngành:
     * - Nếu đã tồn tại trong DB → chỉ cập nhật diemSan và/hoặc chiTieu
     * - Nếu chưa tồn tại → tạo mới, các trường khác để null/trống
     */
    public static ImportResult importFromExcel(String nguongDauvaoPath,
            String chiTieuPath,
            XtNganhBus bus) {
        List<String> errors = new ArrayList<>();
        int inserted = 0, updated = 0, skipError = 0;

        // ── 1. Đọc file ngưỡng đầu vào → Map<maNganh, diemSan> ──────────────
        Map<String, BigDecimal> diemSanMap = new HashMap<>();
        Map<String, String> tenNganhMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(nguongDauvaoPath);
                Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // bỏ header
                if (isRowEmpty(row))
                    continue;

                String maNganh = getMaNganh(row, 1);
                if (maNganh == null) {
                    errors.add("nguongdauvao – Dòng " + (row.getRowNum() + 1) + ": mã ngành trống");
                    skipError++;
                    continue;
                }

                String tenNganh = getString(row, 2);
                BigDecimal diemSan = parseBigDecimal(row, 3);

                tenNganhMap.put(maNganh, tenNganh);
                if (diemSan != null) {
                    diemSanMap.put(maNganh, diemSan);
                }
            }
        } catch (Exception e) {
            errors.add("Lỗi đọc file ngưỡng đầu vào: " + e.getMessage());
        }

        // ── 2. Đọc file chỉ tiêu → Map<maNganh, chiTieu> ────────────────────
        Map<String, Integer> chiTieuMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(chiTieuPath);
                Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            boolean headerFound = false;
            for (Row row : sheet) {
                if (isRowEmpty(row))
                    continue;

                // Bỏ qua dòng tiêu đề phụ (dòng đầu không phải header cột)
                String firstCell = getString(row, 0);
                if (!headerFound) {
                    // Header thực sự có "TT" ở cột 0
                    if ("TT".equalsIgnoreCase(firstCell)) {
                        headerFound = true;
                    }
                    continue;
                }

                String maNganh = getMaNganh(row, 1);
                if (maNganh == null) {
                    errors.add("chitieu – Dòng " + (row.getRowNum() + 1) + ": mã ngành trống");
                    skipError++;
                    continue;
                }

                Integer chiTieu = getInteger(row, 3);
                if (chiTieu != null) {
                    chiTieuMap.put(maNganh, chiTieu);
                }

                // Nếu tên ngành chưa có từ file kia thì lấy luôn từ đây
                if (!tenNganhMap.containsKey(maNganh)) {
                    tenNganhMap.put(maNganh, getString(row, 2));
                }
            }
        } catch (Exception e) {
            errors.add("Lỗi đọc file chỉ tiêu: " + e.getMessage());
        }

        // ── 3. Gộp tất cả mã ngành từ 2 map ─────────────────────────────────
        Map<String, XtNganh> toSave = new HashMap<>();

        for (String maNganh : tenNganhMap.keySet()) {
            Optional<XtNganh> existing = bus.findByMaNganh(maNganh);

            XtNganh nganh;
            boolean isNew;
            if (existing.isPresent()) {
                nganh = existing.get();
                isNew = false;
            } else {
                nganh = new XtNganh();
                nganh.setMaNganh(maNganh);
                nganh.setTenNganh(tenNganhMap.get(maNganh));
                isNew = true;
            }

            if (diemSanMap.containsKey(maNganh)) {
                nganh.setDiemSan(diemSanMap.get(maNganh));
            }
            if (chiTieuMap.containsKey(maNganh)) {
                nganh.setChiTieu(chiTieuMap.get(maNganh));
            }

            toSave.put(maNganh, nganh);
            if (isNew)
                inserted++;
            else
                updated++;
        }

        // Các mã chỉ có trong chitieu mà không có trong nguong dau vao
        for (String maNganh : chiTieuMap.keySet()) {
            if (toSave.containsKey(maNganh))
                continue;

            Optional<XtNganh> existing = bus.findByMaNganh(maNganh);
            XtNganh nganh;
            boolean isNew;
            if (existing.isPresent()) {
                nganh = existing.get();
                isNew = false;
            } else {
                nganh = new XtNganh();
                nganh.setMaNganh(maNganh);
                nganh.setTenNganh(tenNganhMap.getOrDefault(maNganh, null));
                isNew = true;
            }
            nganh.setChiTieu(chiTieuMap.get(maNganh));
            toSave.put(maNganh, nganh);
            if (isNew)
                inserted++;
            else
                updated++;
        }

        // ── 4. Lưu DB ─────────────────────────────────────────────────────────
        try {
            bus.saveAll(new ArrayList<>(toSave.values()));
        } catch (Exception e) {
            errors.add("Lỗi lưu DB: " + e.getMessage());
        }

        String summary = String.format(
                "[NganhImporter] Hoàn thành — Thêm mới: %d | Cập nhật: %d | Lỗi: %d",
                inserted, updated, skipError);
        System.out.println(summary);

        return new ImportResult(inserted, updated, skipError, errors);
    }

    // ── Tiện ích ──────────────────────────────────────────────────────────────

    /** Lấy mã ngành dạng String (vd: "7140114") từ cell có thể là numeric */
    private static String getMaNganh(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            long v = (long) cell.getNumericCellValue();
            return v > 0 ? String.valueOf(v) : null;
        }
        if (cell.getCellType() == CellType.STRING) {
            String s = cell.getStringCellValue().trim();
            return s.isBlank() ? null : s;
        }
        return null;
    }

    private static String getString(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private static BigDecimal parseBigDecimal(Row row, int col) {
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

    private static Integer getInteger(Row row, int col) {
        if (col >= row.getLastCellNum())
            return null;
        Cell cell = row.getCell(col);
        if (cell == null)
            return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC)
                return (int) cell.getNumericCellValue();
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                return s.isBlank() ? null : Integer.parseInt(s);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        for (Cell cell : row)
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        return true;
    }
}