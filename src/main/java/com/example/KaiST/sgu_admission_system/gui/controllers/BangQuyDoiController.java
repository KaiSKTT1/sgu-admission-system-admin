package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtBangQuyDoiBus;
import com.example.KaiST.sgu_admission_system.entity.XtBangQuyDoi;
import com.example.KaiST.sgu_admission_system.gui.dialogs.BangQuyDoiDialog;
import com.example.KaiST.sgu_admission_system.gui.views.BangQuyDoiView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BangQuyDoiController {
    private final BangQuyDoiView view;
    private final XtBangQuyDoiBus bus;
    private List<XtBangQuyDoi> allRows = new ArrayList<>();
    private List<XtBangQuyDoi> filteredRows = new ArrayList<>();
    private int currentPage = 1;

    public BangQuyDoiController(BangQuyDoiView view, XtBangQuyDoiBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allRows = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredRows = new ArrayList<>(allRows);
        } else {
            filteredRows = new ArrayList<>();
            for (XtBangQuyDoi row : allRows) {
                if (containsKeyword(row, keyword)) {
                    filteredRows.add(row);
                }
            }
        }
        currentPage = 1;
        updateTable();
    }

    public void onPageChange(int page) {
        currentPage = page;
        updateTable();
    }

    public void onAdd() {
        BangQuyDoiDialog dialog = new BangQuyDoiDialog(
                view.getWindow(),
                "Thêm quy đổi",
                null,
                BangQuyDoiDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtBangQuyDoi record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xem.");
            return;
        }
        BangQuyDoiDialog dialog = new BangQuyDoiDialog(
                view.getWindow(),
                "Xem chi tiết",
                record,
                BangQuyDoiDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtBangQuyDoi record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần sửa.");
            return;
        }
        BangQuyDoiDialog dialog = new BangQuyDoiDialog(
                view.getWindow(),
                "Sửa quy đổi",
                record,
                BangQuyDoiDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtBangQuyDoi record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa bản ghi này?")) {
            bus.deleteById(record.getIdQd());
            onRefresh();
        }
    }

    public void onImport() {
        File file = view.chooseExcelFile();
        if (file == null) {
            return;
        }

        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.readRows(file, List.of(
                    "d_phuongthuc",
                    "phuongthuc",
                    "d_tohop",
                    "tohop",
                    "d_mon",
                    "mon",
                    "d_diema",
                    "d_diemb",
                    "d_diemc",
                    "d_diemd",
                    "d_maquydoi",
                    "d_phanvi"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtBangQuyDoi> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            XtBangQuyDoi record = new XtBangQuyDoi();
            boolean hasData = false;

            hasData |= applyValue(row, record::setPhuongThuc, "d_phuongthuc", "phuongthuc", "phuong thuc");
            hasData |= applyValue(row, record::setToHop, "d_tohop", "tohop", "to hop");
            hasData |= applyValue(row, record::setMon, "d_mon", "mon");
            hasData |= applyDecimal(row, record::setDiemA, "d_diema", "diema", "diem a");
            hasData |= applyDecimal(row, record::setDiemB, "d_diemb", "diemb", "diem b");
            hasData |= applyDecimal(row, record::setDiemC, "d_diemc", "diemc", "diem c");
            hasData |= applyDecimal(row, record::setDiemD, "d_diemd", "diemd", "diem d");
            hasData |= applyValue(row, record::setMaQuyDoi, "d_maquydoi", "maquydoi", "ma quy doi");
            hasData |= applyValue(row, record::setPhanVi, "d_phanvi", "phanvi", "phan vi");

            if (hasData) {
                imported.add(record);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " bản ghi.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredRows.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtBangQuyDoi record = filteredRows.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(record.getPhuongThuc()),
                    safeText(record.getToHop()),
                    safeText(record.getMon()),
                    safeText(record.getMaQuyDoi()),
                    safeText(record.getDiemA()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtBangQuyDoi getRowAt(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredRows.size()) {
            return null;
        }
        return filteredRows.get(globalIndex);
    }

    private boolean containsKeyword(XtBangQuyDoi record, String keyword) {
        return containsIgnoreCase(record.getMaQuyDoi(), keyword)
                || containsIgnoreCase(record.getPhuongThuc(), keyword)
                || containsIgnoreCase(record.getMon(), keyword)
                || containsIgnoreCase(record.getToHop(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String getValue(Map<String, String> row, String... headers) {
        for (String header : headers) {
            String normalized = ExcelUtils.normalizeHeader(header);
            String value = row.get(normalized);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean applyValue(Map<String, String> row, java.util.function.Consumer<String> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isEmpty()) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private boolean applyDecimal(Map<String, String> row, java.util.function.Consumer<BigDecimal> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isBlank()) {
            return false;
        }
        BigDecimal parsed = parseBigDecimal(value);
        if (parsed == null) {
            return false;
        }
        setter.accept(parsed);
        return true;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalized = value.trim();
            boolean hasComma = normalized.contains(",");
            boolean hasDot = normalized.contains(".");
            if (hasComma && !hasDot) {
                normalized = normalized.replace(',', '.');
            }
            normalized = normalized.replaceAll("[^0-9.\\-]", "");
            int firstDot = normalized.indexOf('.');
            if (firstDot != -1) {
                normalized = normalized.substring(0, firstDot + 1)
                        + normalized.substring(firstDot + 1).replace(".", "");
            }
            if (normalized.isBlank() || "-".equals(normalized) || ".".equals(normalized)) {
                return null;
            }
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
