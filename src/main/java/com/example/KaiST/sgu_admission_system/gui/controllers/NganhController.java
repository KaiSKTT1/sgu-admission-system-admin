package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.gui.dialogs.NganhDialog;
import com.example.KaiST.sgu_admission_system.gui.views.NganhView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NganhController {
    private final NganhView view;
    private final XtNganhBus bus;
    private List<XtNganh> allNganh = new ArrayList<>();
    private List<XtNganh> filteredNganh = new ArrayList<>();
    private Map<String, Long> nguyenVongCount = new HashMap<>();
    private int currentPage = 1;

    public NganhController(NganhView view, XtNganhBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allNganh = bus.findAll();
        nguyenVongCount = bus.countNguyenVongByMaNganh();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredNganh = new ArrayList<>(allNganh);
        } else {
            filteredNganh = new ArrayList<>();
            for (XtNganh nganh : allNganh) {
                if (containsKeyword(nganh, keyword)) {
                    filteredNganh.add(nganh);
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
        NganhDialog dialog = new NganhDialog(
                view.getWindow(),
                "Thêm ngành",
                null,
                NganhDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getNganh());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtNganh nganh = getNganhAtRow(row);
        if (nganh == null) {
            view.showInfo("Vui lòng chọn ngành cần xem.");
            return;
        }
        NganhDialog dialog = new NganhDialog(
                view.getWindow(),
                "Xem chi tiết",
                nganh,
                NganhDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtNganh nganh = getNganhAtRow(row);
        if (nganh == null) {
            view.showInfo("Vui lòng chọn ngành cần sửa.");
            return;
        }
        NganhDialog dialog = new NganhDialog(
                view.getWindow(),
                "Sửa ngành",
                nganh,
                NganhDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getNganh());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtNganh nganh = getNganhAtRow(row);
        if (nganh == null) {
            view.showInfo("Vui lòng chọn ngành cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa ngành này?")) {
            bus.deleteById(nganh.getIdNganh());
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
                    "manganh",
                    "ma nganh",
                    "tennganh",
                    "ten nganh",
                    "chi tieu",
                    "chi tieu chot",
                    "nguong dau vao",
                    "diem san"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtNganh> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String maNganh = getValue(row, "manganh", "ma_nganh", "ma nganh", "ma");
            if (maNganh == null || maNganh.isBlank()) {
                continue;
            }

            String tenNganh = getValue(row, "tennganh", "ten_nganh", "ten nganh", "ten");
            String toHopGoc = getValue(row, "n_tohopgoc", "tohopgoc", "to hop goc", "tohop", "tentohop", "ten_to_hop");
            Integer chiTieu = parseInteger(getValue(row, "n_chitieu", "chitieu", "chi tieu", "chi tieu chot"));
            BigDecimal diemSan = parseBigDecimal(getValue(row, "n_diemsan", "diemsan", "diem san", "nguong dau vao"));
            BigDecimal diemTrungTuyen = parseBigDecimal(
                    getValue(row, "n_diemtrungtuyen", "diemtrungtuyen", "diem trung tuyen"));

            XtNganh nganh = bus.findByMaNganh(maNganh).orElse(null);
            if (nganh == null) {
                XtNganh newNganh = new XtNganh();
                newNganh.setMaNganh(maNganh);
                newNganh.setTenNganh(tenNganh);
                newNganh.setToHopGoc(toHopGoc);
                newNganh.setChiTieu(chiTieu);
                newNganh.setDiemSan(diemSan);
                newNganh.setDiemTrungTuyen(diemTrungTuyen);
                imported.add(newNganh);
                continue;
            }

            boolean updated = false;
            updated |= setIfBlank(nganh.getTenNganh(), nganh::setTenNganh, tenNganh);
            updated |= setIfBlank(nganh.getToHopGoc(), nganh::setToHopGoc, toHopGoc);
            updated |= setIfBlank(nganh.getChiTieu(), nganh::setChiTieu, chiTieu);
            updated |= setIfPresent(nganh.getDiemSan(), nganh::setDiemSan, diemSan);
            updated |= setIfPresent(nganh.getDiemTrungTuyen(), nganh::setDiemTrungTuyen, diemTrungTuyen);

            if (updated) {
                bus.save(nganh);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " ngành.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredNganh.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtNganh nganh = filteredNganh.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(nganh.getTenNganh()),
                    safeText(nganh.getChiTieu()),
                    safeText(nganh.getDiemSan()),
                    safeText(nganh.getDiemTrungTuyen()),
                    buildPhuongThucText(nganh),
                    safeText(nguyenVongCount.getOrDefault(nganh.getMaNganh(), 0L)),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtNganh getNganhAtRow(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredNganh.size()) {
            return null;
        }
        return filteredNganh.get(globalIndex);
    }

    private boolean containsKeyword(XtNganh nganh, String keyword) {
        return containsIgnoreCase(nganh.getMaNganh(), keyword)
                || containsIgnoreCase(nganh.getTenNganh(), keyword)
                || containsIgnoreCase(nganh.getToHopGoc(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String buildPhuongThucText(XtNganh nganh) {
        List<String> methods = new ArrayList<>();
        if (hasPhuongThuc(nganh.getTuyenThang())) {
            methods.add("Tuyển thẳng");
        }
        if (hasPhuongThuc(nganh.getDgnl())) {
            methods.add("ĐGNL");
        }
        if (hasPhuongThuc(nganh.getThpt())) {
            methods.add("THPT");
        }
        if (hasPhuongThuc(nganh.getVsat())) {
            methods.add("VSAT");
        }
        if (methods.isEmpty()) {
            return "-";
        }
        return String.join(", ", methods);
    }

    private boolean hasPhuongThuc(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        return !normalized.isBlank() && !"0".equals(normalized);
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

    private boolean setIfBlank(Integer current, java.util.function.Consumer<Integer> setter, Integer value) {
        if (value == null) {
            return false;
        }
        if (current == null) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private boolean setIfBlank(BigDecimal current, java.util.function.Consumer<BigDecimal> setter, BigDecimal value) {
        if (value == null) {
            return false;
        }
        if (current == null) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private boolean setIfPresent(BigDecimal current, java.util.function.Consumer<BigDecimal> setter, BigDecimal value) {
        if (value == null) {
            return false;
        }
        if (current == null || current.compareTo(value) != 0) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
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
