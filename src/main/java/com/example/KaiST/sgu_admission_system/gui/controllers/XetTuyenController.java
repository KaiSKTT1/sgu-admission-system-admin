package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
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
            view.showInfo("Đã xét tuyển " + allRows.size() + " thí sinh.");
        } catch (Exception ex) {
            view.showError("Không thể thực hiện xét tuyển: " + ex.getMessage());
        }
    }

    public void onRun() {
        onRefresh();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredRows = new ArrayList<>(allRows);
        } else {
            filteredRows = new ArrayList<>();
            for (XetTuyenAdmittedRow row : allRows) {
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
}
