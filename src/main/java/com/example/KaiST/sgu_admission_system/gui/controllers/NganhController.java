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
import java.util.Optional;

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
        File[] files = view.chooseMultipleExcelFiles();
        if (files == null || files.length == 0) {
            return;
        }

        ImportResult importResult = new ImportResult();
        
        for (File file : files) {
            try {
                List<Map<String, String>> rows = ExcelUtils.readRows(file, getAllPossibleHeaders());
                
                for (Map<String, String> row : rows) {
                    processRow(row, importResult);
                }
            } catch (Exception ex) {
                view.showError("Lỗi đọc file " + file.getName() + ": " + ex.getMessage());
            }
        }

        if (!importResult.conflicts.isEmpty()) {
            displayConflicts(importResult.conflicts);
            return;
        }

        // Save new nganh
        for (XtNganh nganh : importResult.newNganh) {
            bus.save(nganh);
        }
        
        // Save updated nganh
        for (XtNganh nganh : importResult.updatedNganh) {
            bus.save(nganh);
        }

        onRefresh();
        int total = importResult.newNganh.size() + importResult.updatedNganh.size();
        view.showInfo("Đã import " + total + " ngành (" + importResult.newNganh.size() + " mới, "
                + importResult.updatedNganh.size() + " cập nhật).");
    }

    private void processRow(Map<String, String> row, ImportResult result) {
        // Lấy mã ngành từ MÃ CTĐT hoặc Mã xét tuyển
        String maNganh = getValue(row, "manganh", "ma_nganh", "ma nganh", "ma", 
                                    "ma ctdt", "ma_ctdt", "mã ctđt",
                                    "ma xet tuyen", "ma_xet_tuyen", "mã xét tuyển");
        if (maNganh == null || maNganh.isBlank()) {
            return;
        }

        // Lấy tên ngành từ Tên CTĐT, Tên ngành chương trình đào tạo, hoặc Tên mã xét tuyển
        String tenNganh = getValue(row, "tennganh", "ten_nganh", "ten nganh", "ten", 
                                     "ten ctdt", "ten_ctdt", "tên ctđt",
                                     "ten nganh, chuong trinh dao tao", "ten chuong trinh dao tao",
                                     "ten ma xet tuyen", "ten_ma_xet_tuyen", "tên mã xét tuyển");
        
        // Lấy chỉ tiêu
        Integer chiTieu = parseInteger(getValue(row, "n_chitieu", "chitieu", "chi tieu", "chi tieu chot", "chi_tieu_chot"));
        
        // Lấy ngưỡng đầu vào (diemSan)
        BigDecimal diemSan = parseBigDecimal(getValue(row, "n_diemsan", "diemsan", "diem san", 
                                                        "nguong dau vao", "nguong_dau_vao", "ngưỡng đầu vào"));

        Optional<XtNganh> existing = bus.findByMaNganh(maNganh);
        if (existing.isEmpty()) {
            // Tạo mới nếu chưa tồn tại
            XtNganh newNganh = new XtNganh();
            newNganh.setMaNganh(maNganh);
            newNganh.setTenNganh(tenNganh);
            newNganh.setChiTieu(chiTieu);
            newNganh.setDiemSan(diemSan);
            result.newNganh.add(newNganh);
        } else {
            XtNganh nganh = existing.get();
            
            // Kiểm tra xung đột: chỉ cập nhật tenNganh nếu chưa có, không kiểm tra xung đột
            if (chiTieu != null && nganh.getChiTieu() != null && !nganh.getChiTieu().equals(chiTieu)) {
                result.conflicts.add(new ConflictInfo(maNganh, "chiTieu", String.valueOf(nganh.getChiTieu()), String.valueOf(chiTieu)));
                return;
            }
            
            if (diemSan != null && nganh.getDiemSan() != null && nganh.getDiemSan().compareTo(diemSan) != 0) {
                result.conflicts.add(new ConflictInfo(maNganh, "diemSan", String.valueOf(nganh.getDiemSan()), String.valueOf(diemSan)));
                return;
            }

            // Cập nhật nếu không xung đột
            boolean updated = false;
            // Chỉ cập nhật tenNganh nếu chưa có
            updated |= setIfBlank(nganh.getTenNganh(), nganh::setTenNganh, tenNganh);
            updated |= setIfBlank(nganh.getChiTieu(), nganh::setChiTieu, chiTieu);
            updated |= setIfPresent(nganh.getDiemSan(), nganh::setDiemSan, diemSan);
            
            // Xử lý toHopGoc: chỉ lấy nếu cột có chứa "Gốc"
            if (hasColumnWithKeyword(row, "goc")) {
                String toHopGoc = getValue(row, "n_tohopgoc", "tohopgoc", "to hop goc", "to_hop_goc", "ten_to_hop");
                updated |= setIfBlank(nganh.getToHopGoc(), nganh::setToHopGoc, toHopGoc);
            }
            
            if (updated && !result.updatedNganh.contains(nganh)) {
                result.updatedNganh.add(nganh);
            }
        }
    }

    private boolean hasColumnWithKeyword(Map<String, String> row, String keyword) {
        for (String key : row.keySet()) {
            if (key.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<String> getAllPossibleHeaders() {
        return List.of(
                // Mã ngành: từ MÃ CTĐT hoặc Mã xét tuyển
                "manganh", "ma_nganh", "ma nganh", "ma",
                "ma ctdt", "ma_ctdt", "mã ctđt",
                "ma xet tuyen", "ma_xet_tuyen", "mã xét tuyển",
                
                // Tên ngành: từ Tên CTĐT, Tên ngành chương trình, hoặc Tên mã xét tuyển
                "tennganh", "ten_nganh", "ten nganh", "ten",
                "ten ctdt", "ten_ctdt", "tên ctđt",
                "ten nganh, chuong trinh dao tao", "ten chuong trinh dao tao",
                "ten ma xet tuyen", "ten_ma_xet_tuyen", "tên mã xét tuyển",
                
                // Tổ hợp gốc
                "n_tohopgoc", "tohopgoc", "to hop goc", "to_hop_goc", "ten_to_hop",
                "goc", "to hop goc",
                
                // Chỉ tiêu chốt
                "n_chitieu", "chitieu", "chi tieu", "chi tieu chot", "chi_tieu_chot",
                
                // Ngưỡng đầu vào
                "n_diemsan", "diemsan", "diem san", "diem_san",
                "nguong dau vao", "nguong_dau_vao", "ngưỡng đầu vào",
                
                // Các cột tạm bỏ trống (để tương lai)
                "n_diemtrungtuyen", "diemtrungtuyen", "diem trung tuyen",
                "n_tuyenthang", "tuyenthang", "tuyen thang",
                "n_dgnl", "dgnl",
                "n_thpt", "thpt",
                "n_vsat", "vsat",
                "sl_xtt", "slxtt", "sl xtt",
                "sl_dgnl", "sldgnl", "sl dgnl",
                "sl_vsat", "slvsat", "sl vsat",
                "sl_thpt", "slthpt", "sl thpt",
                
                // Các cột khác
                "cccd", "stt", "thu tu", "thu tu nv");
    }

    private void displayConflicts(List<ConflictInfo> conflicts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Phát hiện ").append(conflicts.size()).append(" xung đột:\n\n");
        
        for (ConflictInfo conflict : conflicts) {
            sb.append("Mã ngành: ").append(conflict.maNganh).append("\n");
            sb.append("Trường: ").append(conflict.field).append("\n");
            sb.append("Dữ liệu hiện tại: ").append(conflict.currentValue).append("\n");
            sb.append("Dữ liệu mới: ").append(conflict.newValue).append("\n");
            sb.append("---\n");
        }
        
        sb.append("\nVui lòng kiểm tra và sửa lỗi trong các file Excel.");
        view.showError(sb.toString());
    }

    private static class ConflictInfo {
        String maNganh;
        String field;
        String currentValue;
        String newValue;

        ConflictInfo(String maNganh, String field, String currentValue, String newValue) {
            this.maNganh = maNganh;
            this.field = field;
            this.currentValue = currentValue;
            this.newValue = newValue;
        }
    }

    private static class ImportResult {
        List<XtNganh> newNganh = new ArrayList<>();
        List<XtNganh> updatedNganh = new ArrayList<>();
        List<ConflictInfo> conflicts = new ArrayList<>();
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
