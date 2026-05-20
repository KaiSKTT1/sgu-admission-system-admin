package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
import com.example.KaiST.sgu_admission_system.dto.DiemXetTuyenRow;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.gui.views.XetTuyenView;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XetTuyenController {
    private final XetTuyenView view;
    private final XetTuyenBus xetTuyenBus;
    private final XtNganhBus nganhBus;
    private List<XetTuyenRow> allRows = new ArrayList<>();
    private List<XetTuyenRow> filteredRows = new ArrayList<>();
    private int currentPage = 1;

    public XetTuyenController(XetTuyenView view, XetTuyenBus xetTuyenBus, XtNganhBus nganhBus) {
        this.view = view;
        this.xetTuyenBus = xetTuyenBus;
        this.nganhBus = nganhBus;
    }

    public void init() {
        view.setNganhOptions(buildNganhList());
        view.setPhuongThucOptions(buildPhuongThucList());
        onRefresh();
    }

    public void onRefresh() {
        try {
            Map<String, XtNganh> nganhByMa = buildNganhMap();
            String selectedMaNganh = normalize(view.getSelectedMaNganh());
            String selectedPhuongThuc = view.getSelectedPhuongThuc().trim();
            List<DiemXetTuyenRow> rows = xetTuyenBus.buildDiemXetTuyenRows();
            allRows = selectAdmittedRows(rows, nganhByMa, selectedMaNganh, selectedPhuongThuc);
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
            for (XetTuyenRow row : allRows) {
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
            XetTuyenRow row = filteredRows.get(i);
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

    private boolean containsKeyword(XetTuyenRow row, String keyword) {
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

    private Map<String, XtNganh> buildNganhMap() {
        Map<String, XtNganh> result = new HashMap<>();
        for (XtNganh nganh : nganhBus.findAll()) {
            if (nganh != null && nganh.getMaNganh() != null) {
                result.put(normalize(nganh.getMaNganh()), nganh);
            }
        }
        return result;
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

    private List<String> buildPhuongThucList() {
        List<String> result = new ArrayList<>();
        for (PhuongThuc method : PhuongThuc.values()) {
            result.add(method.getLabel());
        }
        return result;
    }

    private List<XetTuyenRow> selectAdmittedRows(List<DiemXetTuyenRow> rows,
            Map<String, XtNganh> nganhByMa,
            String selectedMaNganh,
            String selectedPhuongThuc) {
        Map<String, List<DiemXetTuyenRow>> rowsByNganh = new HashMap<>();
        for (DiemXetTuyenRow row : rows) {
            if (row == null || row.getMaNganh() == null || row.getDiemXetTuyen() == null) {
                continue;
            }
            String key = normalize(row.getMaNganh());
            if (!selectedMaNganh.isEmpty() && !key.equals(selectedMaNganh)) {
                continue;
            }
            rowsByNganh.computeIfAbsent(key, ignored -> new ArrayList<>()).add(row);
        }

        List<XetTuyenRow> result = new ArrayList<>();
        for (Map.Entry<String, List<DiemXetTuyenRow>> entry : rowsByNganh.entrySet()) {
            String maNganh = entry.getKey();
            XtNganh nganh = nganhByMa.get(maNganh);
            if (nganh == null) {
                continue;
            }
            // Use diemSan (n_diemsan) as the threshold
            BigDecimal diemSan = nganh.getDiemSan();

            for (PhuongThuc method : PhuongThuc.values()) {
                if (!selectedPhuongThuc.isEmpty() && !selectedPhuongThuc.equalsIgnoreCase(method.getLabel())) {
                    continue;
                }
                if (!isMethodEnabled(nganh, method)) {
                    continue;
                }
                Integer quota = getQuota(nganh, method);
                if (quota == null || quota <= 0) {
                    continue;
                }

                List<DiemXetTuyenRow> candidates = new ArrayList<>();
                for (DiemXetTuyenRow row : entry.getValue()) {
                    if (row.getDiemXetTuyen() == null || row.getPhuongThuc() == null) {
                        continue;
                    }
                    if (row.getPhuongThuc() != method) {
                        continue;
                    }
                    boolean scoreValid = diemSan == null || row.getDiemXetTuyen().compareTo(diemSan) >= 0;
                    if (scoreValid) {
                        candidates.add(row);
                    }
                }

                candidates.sort((a, b) -> compareDesc(a.getDiemXetTuyen(), b.getDiemXetTuyen()));
                int count = Math.min(quota, candidates.size());
                for (int i = 0; i < count; i++) {
                    DiemXetTuyenRow candidate = candidates.get(i);
                    result.add(new XetTuyenRow(
                            candidate.getCccd(),
                            candidate.getHoTen(),
                            candidate.getMaNganh(),
                            candidate.getPhuongThuc() == null ? null : candidate.getPhuongThuc().getLabel(),
                            candidate.getDiemXetTuyen(),
                            quota,
                            diemSan));
                }
            }
        }

        result.sort((a, b) -> compareDesc(a.getDiemXetTuyen(), b.getDiemXetTuyen()));
        return result;
    }

    private boolean isMethodEnabled(XtNganh nganh, PhuongThuc method) {
        if (nganh == null || method == null) {
            return false;
        }
        String value = null;
        switch (method) {
            case THPT:
                value = nganh.getThpt();
                break;
            case DGNL:
                value = nganh.getDgnl();
                break;
            case VSAT:
                value = nganh.getVsat();
                break;
            default:
                return false;
        }
        // Enabled if value is "1" or any non-null, non-zero value
        // Disabled only if explicitly "0" or null/empty
        if (value == null || value.isEmpty()) {
            return false;
        }
        return !"0".equals(value);
    }

    private Integer getQuota(XtNganh nganh, PhuongThuc method) {
        if (nganh == null || method == null) {
            return null;
        }
        switch (method) {
            case THPT:
                return parseInteger(nganh.getSlThpt());
            case DGNL:
                return nganh.getSlDgnl();
            case VSAT:
                return nganh.getSlVsat();
            default:
                return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int compareDesc(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return b.compareTo(a);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private static class XetTuyenRow {
        private final String cccd;
        private final String hoTen;
        private final String maNganh;
        private final String phuongThuc;
        private final BigDecimal diemXetTuyen;
        private final Integer chiTieu;
        private final BigDecimal diemChuan;

        public XetTuyenRow(String cccd, String hoTen, String maNganh, String phuongThuc,
                BigDecimal diemXetTuyen, Integer chiTieu, BigDecimal diemChuan) {
            this.cccd = cccd;
            this.hoTen = hoTen;
            this.maNganh = maNganh;
            this.phuongThuc = phuongThuc;
            this.diemXetTuyen = diemXetTuyen;
            this.chiTieu = chiTieu;
            this.diemChuan = diemChuan;
        }

        public String getCccd() {
            return cccd;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getMaNganh() {
            return maNganh;
        }

        public String getPhuongThuc() {
            return phuongThuc;
        }

        public BigDecimal getDiemXetTuyen() {
            return diemXetTuyen;
        }

        public Integer getChiTieu() {
            return chiTieu;
        }

        public BigDecimal getDiemChuan() {
            return diemChuan;
        }
    }
}
