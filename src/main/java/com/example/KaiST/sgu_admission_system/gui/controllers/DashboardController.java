package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtThiSinhXetTuyen25Bus;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import com.example.KaiST.sgu_admission_system.gui.dialogs.ThiSinhXetTuyenDialog;
import com.example.KaiST.sgu_admission_system.gui.views.DashboardView;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

public class DashboardController {
    private final DashboardView view;
    private final XtThiSinhXetTuyen25Bus bus;
    private List<XtThiSinhXetTuyen25> visibleCandidates = new ArrayList<>();

    public DashboardController(DashboardView view, XtThiSinhXetTuyen25Bus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        List<XtThiSinhXetTuyen25> all = bus.findAll();
        int maxRows = view.getMaxRows();
        visibleCandidates = new ArrayList<>();
        for (int i = 0; i < Math.min(maxRows, all.size()); i++) {
            visibleCandidates.add(all.get(i));
        }

        List<Object[]> rows = new ArrayList<>();
        for (XtThiSinhXetTuyen25 candidate : visibleCandidates) {
            rows.add(new Object[] {
                    candidate.getCccd(),
                    candidate.getSoBaoDanh(),
                    safeText(candidate.getTen()),
                    candidate.getNgaySinh(),
                    candidate.getGioiTinh(),
                    candidate.getKhuVuc(),
                    "",
                    "",
                    ""
            });
        }

        view.setTableRows(rows);
        view.setStats(all.size(), formatCounts(countByDoiTuong(all)), formatCounts(countByKhuVuc(all)));
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

    private XtThiSinhXetTuyen25 getCandidateAtRow(int row) {
        if (row < 0 || row >= visibleCandidates.size()) {
            return null;
        }
        return visibleCandidates.get(row);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, Integer> countByDoiTuong(List<XtThiSinhXetTuyen25> all) {
        Map<String, Integer> counts = new TreeMap<>();
        for (XtThiSinhXetTuyen25 candidate : all) {
            String key = normalizeKey(candidate.getDoiTuong());
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    private Map<String, Integer> countByKhuVuc(List<XtThiSinhXetTuyen25> all) {
        Map<String, Integer> counts = new TreeMap<>();
        for (XtThiSinhXetTuyen25 candidate : all) {
            String key = normalizeKey(candidate.getKhuVuc());
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            return "Khác";
        }
        return value.trim();
    }

    private String formatCounts(Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return "Chưa có";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
        }
        return builder.toString();
    }
}
