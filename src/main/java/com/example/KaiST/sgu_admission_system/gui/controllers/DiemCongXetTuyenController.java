package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtDiemCongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import com.example.KaiST.sgu_admission_system.gui.dialogs.DiemCongXetTuyenDialog;
import com.example.KaiST.sgu_admission_system.gui.views.DiemCongXetTuyenView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiemCongXetTuyenController {
    private final DiemCongXetTuyenView view;
    private final XtDiemCongXetTuyenBus bus;
    private List<XtDiemCongXetTuyen> allRows = new ArrayList<>();
    private List<XtDiemCongXetTuyen> filteredRows = new ArrayList<>();
    private int currentPage = 1;

    public DiemCongXetTuyenController(DiemCongXetTuyenView view, XtDiemCongXetTuyenBus bus) {
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
            for (XtDiemCongXetTuyen row : allRows) {
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
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Thêm điểm cộng",
                null,
                DiemCongXetTuyenDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtDiemCongXetTuyen record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xem.");
            return;
        }
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Xem chi tiết",
                record,
                DiemCongXetTuyenDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtDiemCongXetTuyen record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần sửa.");
            return;
        }
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Sửa điểm cộng",
                record,
                DiemCongXetTuyenDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtDiemCongXetTuyen record = getRowAt(row);
        if (record == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa bản ghi này?")) {
            bus.deleteById(record.getIdDiemCong());
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
                    "ts_cccd",
                    "cccd",
                    "manganh",
                    "matohop",
                    "phuongthuc",
                    "diemcc",
                    "diemutxt",
                    "diemtong",
                    "ghichu",
                    "dc_keys"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtDiemCongXetTuyen> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            XtDiemCongXetTuyen record = new XtDiemCongXetTuyen();
            boolean hasData = false;

            hasData |= applyValue(row, record::setTsCccd, "ts_cccd", "cccd");
            hasData |= applyValue(row, record::setMaNganh, "manganh");
            hasData |= applyValue(row, record::setMaToHop, "matohop");
            hasData |= applyValue(row, record::setPhuongThuc, "phuongthuc", "phuong thuc");
            hasData |= applyDecimal(row, record::setDiemCc, "diemcc", "diem_cc");
            hasData |= applyDecimal(row, record::setDiemUtxt, "diemutxt", "diem_utxt");
            hasData |= applyDecimal(row, record::setDiemTong, "diemtong", "diem_tong");
            hasData |= applyValue(row, record::setGhiChu, "ghichu", "ghi chu");
            hasData |= applyValue(row, record::setDcKeys, "dc_keys", "dc keys");

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
            XtDiemCongXetTuyen record = filteredRows.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(record.getTsCccd()),
                    safeText(record.getMaNganh()),
                    safeText(record.getMaToHop()),
                    safeText(record.getPhuongThuc()),
                    safeText(record.getDiemTong()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtDiemCongXetTuyen getRowAt(int row) {
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

    private boolean containsKeyword(XtDiemCongXetTuyen record, String keyword) {
        return containsIgnoreCase(record.getTsCccd(), keyword)
                || containsIgnoreCase(record.getMaNganh(), keyword)
                || containsIgnoreCase(record.getMaToHop(), keyword);
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
