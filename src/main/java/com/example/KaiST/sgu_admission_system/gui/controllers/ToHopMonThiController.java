package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import com.example.KaiST.sgu_admission_system.gui.dialogs.ToHopMonThiDialog;
import com.example.KaiST.sgu_admission_system.gui.views.ToHopMonThiView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ToHopMonThiController {
    private final ToHopMonThiView view;
    private final XtToHopMonThiBus bus;
    private List<XtToHopMonThi> allToHop = new ArrayList<>();
    private List<XtToHopMonThi> filteredToHop = new ArrayList<>();
    private int currentPage = 1;

    public ToHopMonThiController(ToHopMonThiView view, XtToHopMonThiBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allToHop = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredToHop = new ArrayList<>(allToHop);
        } else {
            filteredToHop = new ArrayList<>();
            for (XtToHopMonThi toHop : allToHop) {
                if (containsKeyword(toHop, keyword)) {
                    filteredToHop.add(toHop);
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
        ToHopMonThiDialog dialog = new ToHopMonThiDialog(
                view.getWindow(),
                "Thêm tổ hợp môn thi",
                null,
                ToHopMonThiDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getToHop());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtToHopMonThi toHop = getToHopAtRow(row);
        if (toHop == null) {
            view.showInfo("Vui lòng chọn tổ hợp cần xem.");
            return;
        }
        ToHopMonThiDialog dialog = new ToHopMonThiDialog(
                view.getWindow(),
                "Xem chi tiết",
                toHop,
                ToHopMonThiDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtToHopMonThi toHop = getToHopAtRow(row);
        if (toHop == null) {
            view.showInfo("Vui lòng chọn tổ hợp cần sửa.");
            return;
        }
        ToHopMonThiDialog dialog = new ToHopMonThiDialog(
                view.getWindow(),
                "Sửa tổ hợp",
                toHop,
                ToHopMonThiDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getToHop());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtToHopMonThi toHop = getToHopAtRow(row);
        if (toHop == null) {
            view.showInfo("Vui lòng chọn tổ hợp cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa tổ hợp này?")) {
            bus.deleteById(toHop.getIdToHop());
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
                    "matohop",
                    "ma to hop",
                    "tentohop",
                    "ten to hop",
                    "mon1",
                    "mon2",
                    "mon3"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtToHopMonThi> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String maToHop = getValue(row, "matohop", "ma_to_hop", "ma to hop", "ma");
            if (maToHop == null || maToHop.isBlank()) {
                continue;
            }

            String tenToHop = getValue(row, "tentohop", "ten_to_hop", "ten to hop", "ten");
            String mon1 = getValue(row, "mon1", "mon 1", "mon_1");
            String mon2 = getValue(row, "mon2", "mon 2", "mon_2");
            String mon3 = getValue(row, "mon3", "mon 3", "mon_3");

            XtToHopMonThi toHop = bus.findByMaToHop(maToHop).orElse(null);
            if (toHop == null) {
                XtToHopMonThi newToHop = new XtToHopMonThi();
                newToHop.setMaToHop(maToHop);
                newToHop.setTenToHop(tenToHop);
                newToHop.setMon1(mon1);
                newToHop.setMon2(mon2);
                newToHop.setMon3(mon3);
                imported.add(newToHop);
                continue;
            }

            boolean updated = false;
            updated |= setIfBlank(toHop.getTenToHop(), toHop::setTenToHop, tenToHop);
            updated |= setIfBlank(toHop.getMon1(), toHop::setMon1, mon1);
            updated |= setIfBlank(toHop.getMon2(), toHop::setMon2, mon2);
            updated |= setIfBlank(toHop.getMon3(), toHop::setMon3, mon3);

            if (updated) {
                bus.save(toHop);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " tổ hợp.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredToHop.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtToHopMonThi toHop = filteredToHop.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(toHop.getMaToHop()),
                    safeText(toHop.getTenToHop()),
                    safeText(toHop.getMon1()),
                    safeText(toHop.getMon2()),
                    safeText(toHop.getMon3()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtToHopMonThi getToHopAtRow(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredToHop.size()) {
            return null;
        }
        return filteredToHop.get(globalIndex);
    }

    private boolean containsKeyword(XtToHopMonThi toHop, String keyword) {
        return containsIgnoreCase(toHop.getMaToHop(), keyword)
                || containsIgnoreCase(toHop.getTenToHop(), keyword)
                || containsIgnoreCase(toHop.getMon1(), keyword)
                || containsIgnoreCase(toHop.getMon2(), keyword)
                || containsIgnoreCase(toHop.getMon3(), keyword);
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

    private boolean setIfBlank(String current, java.util.function.Consumer<String> setter, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (current == null || current.isBlank()) {
            setter.accept(value.trim());
            return true;
        }
        return false;
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
