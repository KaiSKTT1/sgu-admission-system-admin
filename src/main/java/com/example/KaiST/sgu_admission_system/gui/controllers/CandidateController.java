package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtDiemThiXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.gui.dialogs.ThiSinhScoreDialog;
import com.example.KaiST.sgu_admission_system.gui.dialogs.ThiSinhXetTuyenDialog;
import com.example.KaiST.sgu_admission_system.gui.views.CandidateView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class CandidateController {
    private final CandidateView view;
    private final XtThiSinhXetTuyen25Bus bus;
    private final XtDiemThiXetTuyenBus diemThiBus;
    private List<XtThiSinhXetTuyen25> allCandidates = new ArrayList<>();
    private List<XtThiSinhXetTuyen25> filteredCandidates = new ArrayList<>();
    private int currentPage = 1;

    public CandidateController(CandidateView view, XtThiSinhXetTuyen25Bus bus, XtDiemThiXetTuyenBus diemThiBus) {
        this.view = view;
        this.bus = bus;
        this.diemThiBus = diemThiBus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allCandidates = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredCandidates = new ArrayList<>(allCandidates);
        } else {
            filteredCandidates = new ArrayList<>();
            for (XtThiSinhXetTuyen25 candidate : allCandidates) {
                if (containsKeyword(candidate, keyword)) {
                    filteredCandidates.add(candidate);
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
        ThiSinhXetTuyenDialog dialog = new ThiSinhXetTuyenDialog(
                view.getWindow(),
                "Thêm thí sinh",
                null,
                ThiSinhXetTuyenDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getCandidate());
            onRefresh();
        }
    }

    public void onView() {
        onViewRow(view.getSelectedRow());
    }

    public void onEdit() {
        onEditRow(view.getSelectedRow());
    }

    public void onDelete() {
        onDeleteRow(view.getSelectedRow());
    }

    public void onViewRow(int row) {
        XtThiSinhXetTuyen25 candidate = getCandidateAtRow(row);
        if (candidate == null) {
            view.showInfo("Vui lòng chọn thí sinh cần xem.");
            return;
        }
        ThiSinhXetTuyenDialog dialog = new ThiSinhXetTuyenDialog(
                view.getWindow(),
                "Xem chi tiết",
                candidate,
                ThiSinhXetTuyenDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtThiSinhXetTuyen25 candidate = getCandidateAtRow(row);
        if (candidate == null) {
            view.showInfo("Vui lòng chọn thí sinh cần sửa.");
            return;
        }
        ThiSinhXetTuyenDialog dialog = new ThiSinhXetTuyenDialog(
                view.getWindow(),
                "Sửa thí sinh",
                candidate,
                ThiSinhXetTuyenDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getCandidate());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtThiSinhXetTuyen25 candidate = getCandidateAtRow(row);
        if (candidate == null) {
            view.showInfo("Vui lòng chọn thí sinh cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa thí sinh này?")) {
            bus.deleteById(candidate.getIdThiSinh());
            onRefresh();
        }
    }

    public void onScoreRow(int row) {
        XtThiSinhXetTuyen25 candidate = getCandidateAtRow(row);
        if (candidate == null) {
            view.showInfo("Vui lòng chọn thí sinh cần xem điểm.");
            return;
        }

        List<XtDiemThiXetTuyen> scores = diemThiBus.findByCccdOrSbd(
                candidate.getCccd(),
                candidate.getSoBaoDanh());
        ThiSinhScoreDialog dialog = new ThiSinhScoreDialog(view.getWindow(), candidate, scores);
        dialog.setVisible(true);
    }

    public void onImport() {
        File file = view.chooseExcelFile();
        if (file == null) {
            return;
        }

        List<Map<String, String>> rows;
        try {
            rows = ExcelUtils.readRows(file);
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtThiSinhXetTuyen25> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            XtThiSinhXetTuyen25 candidate = new XtThiSinhXetTuyen25();
            boolean hasData = false;

            hasData |= applyValue(row, candidate::setCccd, "cccd");
            hasData |= applyValue(row, candidate::setSoBaoDanh, "sobaodanh", "sbd");
            hasData |= applyHoTen(row, candidate);
            hasData |= applyValue(row, candidate::setNgaySinh, "ngaysinh");
            hasData |= applyValue(row, candidate::setGioiTinh, "gioitinh");
            hasData |= applyValue(row, candidate::setDienThoai, "dienthoai", "sodienthoai");
            hasData |= applyValue(row, candidate::setEmail, "email");
            hasData |= applyValue(row, candidate::setNoiSinh, "noisinh");
            hasData |= applyValue(row, candidate::setDoiTuong, "doituong", "dtut", "doituonguutien");
            hasData |= applyValue(row, candidate::setKhuVuc, "khuvuc", "kvut", "khuvucuutien");

            if (hasData) {
                imported.add(candidate);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " thí sinh.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredCandidates.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtThiSinhXetTuyen25 candidate = filteredCandidates.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    candidate.getCccd(),
                    candidate.getNoiSinh(),
                    safeText(candidate.getTen()),
                    candidate.getNgaySinh(),
                    candidate.getGioiTinh(),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtThiSinhXetTuyen25 getSelectedCandidate() {
        int selectedRow = view.getSelectedRow();
        return getCandidateAtRow(selectedRow);
    }

    private XtThiSinhXetTuyen25 getCandidateAtRow(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredCandidates.size()) {
            return null;
        }
        return filteredCandidates.get(globalIndex);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean containsKeyword(XtThiSinhXetTuyen25 candidate, String keyword) {
        return containsIgnoreCase(candidate.getCccd(), keyword)
                || containsIgnoreCase(candidate.getSoBaoDanh(), keyword)
                || containsIgnoreCase(candidate.getTen(), keyword)
                || containsIgnoreCase(candidate.getDienThoai(), keyword)
                || containsIgnoreCase(candidate.getEmail(), keyword)
                || containsIgnoreCase(candidate.getNoiSinh(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private boolean applyValue(Map<String, String> row, Consumer<String> setter, String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isEmpty()) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private boolean applyHoTen(Map<String, String> row, XtThiSinhXetTuyen25 candidate) {
        boolean updated = false;
        String hoTen = getValue(row, "hoten", "hovaten");
        if (hoTen != null && !hoTen.isEmpty()) {
            candidate.setTen(hoTen.trim().replaceAll("\\s+", " "));
            updated = true;
        }

        String ten = getValue(row, "ten");
        if (ten != null && !ten.isEmpty()) {
            candidate.setTen(ten);
            updated = true;
        }
        return updated;
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
}
