package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import com.example.KaiST.sgu_admission_system.gui.dialogs.ToHopMonThiDialog;
import com.example.KaiST.sgu_admission_system.gui.views.ToHopMonThiView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
                    "matohop", "ma_to_hop", "ma to hop", "ma",
                    "tentohop", "ten_to_hop", "ten to hop", "ten",
                    "mon1", "mon2", "mon3"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtToHopMonThi> imported = new ArrayList<>();
        Set<String> processedMaToHop = new HashSet<>();
        
        for (Map<String, String> row : rows) {
            String maToHopRaw = getValue(row, "matohop", "ma_to_hop", "ma to hop", "ma");
            if (maToHopRaw == null || maToHopRaw.isBlank()) {
                continue;
            }

            // Tách maToHop từ định dạng "B03(TO-3, VA-3, SI-1)"
            String maToHop = extractMaToHop(maToHopRaw);
            if (maToHop == null || maToHop.isBlank()) {
                continue;
            }

            // Kiểm tra mã tổ hợp đã được xử lý chưa
            if (processedMaToHop.contains(maToHop)) {
                continue;
            }
            processedMaToHop.add(maToHop);

            // Parse mon1, mon2, mon3 từ định dạng
            List<String> mons = parseMonFromFormat(maToHopRaw);
            String mon1 = mons.size() > 0 ? mons.get(0) : null;
            String mon2 = mons.size() > 1 ? mons.get(1) : null;
            String mon3 = mons.size() > 2 ? mons.get(2) : null;

            // Tạo tenToHop từ các môn
            String tenToHop = buildTenToHop(mon1, mon2, mon3);

            XtToHopMonThi existing = bus.findByMaToHop(maToHop).orElse(null);
            if (existing == null) {
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
            updated |= setIfBlank(existing.getTenToHop(), existing::setTenToHop, tenToHop);
            updated |= setIfBlank(existing.getMon1(), existing::setMon1, mon1);
            updated |= setIfBlank(existing.getMon2(), existing::setMon2, mon2);
            updated |= setIfBlank(existing.getMon3(), existing::setMon3, mon3);

            if (updated) {
                bus.save(existing);
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

    /**
     * Tách maToHop từ định dạng "B03(TO-3, VA-3, SI-1)" → "B03"
     */
    private String extractMaToHop(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        int parenIndex = raw.indexOf('(');
        if (parenIndex > 0) {
            return raw.substring(0, parenIndex).trim();
        }
        return raw.trim();
    }

    /**
     * Parse các môn học từ định dạng "(TO-3, VA-3, SI-1)" → [TO, VA, SI]
     * Lấy phần trước dấu "-", bao gồm cả ký tự số (ví dụ: NK1-1 → NK1)
     */
    private List<String> parseMonFromFormat(String raw) {
        List<String> mons = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return mons;
        }

        int startIndex = raw.indexOf('(');
        int endIndex = raw.indexOf(')');
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            return mons;
        }

        String monPart = raw.substring(startIndex + 1, endIndex).trim();
        String[] monItems = monPart.split(",");

        for (String item : monItems) {
            String monCode = extractMonCode(item.trim());
            if (monCode != null && !monCode.isBlank()) {
                mons.add(monCode);
            }
        }

        return mons;
    }

    /**
     * Lấy phần trước dấu "-" từ "TO-3" → "TO" hoặc từ "NK1-1" → "NK1"
     */
    private String extractMonCode(String item) {
        if (item == null || item.isBlank()) {
            return null;
        }
        int dashIndex = item.indexOf('-');
        if (dashIndex > 0) {
            return item.substring(0, dashIndex).trim();
        }
        return item.trim();
    }

    /**
     * Map mã môn sang tên viết tắt và tạo tenToHop
     * Ví dụ: TO, VA, SI → "Toán, Văn, Sinh"
     */
    private String buildTenToHop(String mon1, String mon2, String mon3) {
        List<String> names = new ArrayList<>();
        
        if (mon1 != null && !mon1.isBlank()) {
            names.add(mapMonToName(mon1));
        }
        if (mon2 != null && !mon2.isBlank()) {
            names.add(mapMonToName(mon2));
        }
        if (mon3 != null && !mon3.isBlank()) {
            names.add(mapMonToName(mon3));
        }

        return String.join(", ", names);
    }

    /**
     * Map mã môn học sang tên đầy đủ
     */
    private String mapMonToName(String monCode) {
        if (monCode == null) {
            return "";
        }

        return switch (monCode.toUpperCase(Locale.ROOT)) {
            case "TO" -> "Toán";
            case "HO" -> "Hóa";
            case "SI" -> "Sinh";
            case "VA" -> "Văn";
            case "GD" -> "GDCD";
            case "DI" -> "Địa lý";
            case "TI" -> "Tin học";
            case "KTPL" -> "Kinh tế - Pháp luật";
            case "CNCN" -> "Công nghệ Chăn nuôi";
            case "LI" -> "Vật lý";
            case "SU" -> "Lịch Sử";
            case "CNNN" -> "Công nghệ Nông Nghiệp";
            case "N1" -> "Tiếng Anh";
            case "NK1" -> "Kể chuyện - Đọc diễn cảm";
            case "NK2" -> "Hát - Nhạc";
            case "NK3" -> "Hình họa";
            case "NK4" -> "Trang Trí";
            case "NK5" -> "Hát - Nhạc cụ";
            case "NK6" -> "Xướng âm - Thẩm âm";
            default -> monCode;
        };
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
