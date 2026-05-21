package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.dto.XetTuyenAdmittedRow;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.gui.views.XetTuyenView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XetTuyenController {
    private final XetTuyenView view;
    private final XetTuyenBus xetTuyenBus;
    private final XtNganhBus nganhBus;
    private List<XetTuyenAdmittedRow> allRows = new ArrayList<>();
    private List<XetTuyenAdmittedRow> filteredRows = new ArrayList<>();
    private int currentPage = 1;

    public XetTuyenController(XetTuyenView view, XetTuyenBus xetTuyenBus, XtNganhBus nganhBus) {
        this.view = view;
        this.xetTuyenBus = xetTuyenBus;
        this.nganhBus = nganhBus;
    }

    public void init() {
        view.setNganhOptions(buildNganhList());
        onRefresh();
    }

    public void onRefresh() {
        try {
            String selectedMaNganh = normalize(view.getSelectedMaNganh());
            allRows = xetTuyenBus.selectAdmittedByQuota(selectedMaNganh);
            onSearch();
            view.showInfo("Đã xét tuyển " + allRows.size() + " thí sinh (theo chỉ tiêu và điểm sàn từng ngành).");
        } catch (Exception ex) {
            view.showError("Không thể thực hiện xét tuyển: " + ex.getMessage());
        }
    }

    public void onRun() {
        onRefresh();
    }

    /** Cập nhật bảng từ kết quả xét tuyển vừa chạy ở panel Điểm xét tuyển. */
    public void loadFromLastResult() {
        allRows = new ArrayList<>(xetTuyenBus.getLastAdmittedRows());
        String selectedMaNganh = normalize(view.getSelectedMaNganh());
        if (!selectedMaNganh.isEmpty()) {
            List<XetTuyenAdmittedRow> filtered = new ArrayList<>();
            for (XetTuyenAdmittedRow row : allRows) {
                if (selectedMaNganh.equals(normalize(row.getMaNganh()))) {
                    filtered.add(row);
                }
            }
            allRows = filtered;
        }
        onSearch();
    }

    public void onFilterChange() {
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        String phuongThucFilter = normalizePhuongThuc(view.getSelectedPhuongThuc());
        filteredRows = new ArrayList<>();
        for (XetTuyenAdmittedRow row : allRows) {
            if (!keyword.isEmpty() && !containsKeyword(row, keyword)) {
                continue;
            }
            if (!phuongThucFilter.isEmpty() && !phuongThucFilter.equals(normalizePhuongThuc(row.getPhuongThuc()))) {
                continue;
            }
            filteredRows.add(row);
        }
        currentPage = 1;
        updateTable();
    }

    public void onPageChange(int page) {
        currentPage = page;
        updateTable();
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
            XetTuyenAdmittedRow row = filteredRows.get(i);
            rows.add(new Object[] {
                    i + 1,
                    safeText(row.getCccd()),
                    safeText(row.getHoTen()),
                    safeText(row.getMaNganh()),
                    safeText(row.getPhuongThuc()),
                    safeText(row.getDiemXetTuyen()),
                    safeText(row.getChiTieu()),
                    safeText(row.getDiemChuan())
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private boolean containsKeyword(XetTuyenAdmittedRow row, String keyword) {
        return containsIgnoreCase(row.getCccd(), keyword)
                || containsIgnoreCase(row.getHoTen(), keyword)
                || containsIgnoreCase(row.getMaNganh(), keyword)
                || containsIgnoreCase(row.getPhuongThuc(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<String> buildNganhList() {
        List<String> result = new ArrayList<>();
        for (XtNganh nganh : nganhBus.findAll()) {
            if (nganh != null && nganh.getMaNganh() != null && !nganh.getMaNganh().isBlank()) {
                result.add(nganh.getMaNganh().trim());
            }
        }
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePhuongThuc(String value) {
        PhuongThuc method = PhuongThuc.fromText(value);
        if (method != null) {
            return method.getLabel();
        }
        return normalize(value);
    }
}
