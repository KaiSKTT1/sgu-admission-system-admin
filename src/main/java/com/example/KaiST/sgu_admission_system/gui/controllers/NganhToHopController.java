package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.bus.XtNganhToHopBus;
import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import com.example.KaiST.sgu_admission_system.gui.dialogs.NganhToHopDialog;
import com.example.KaiST.sgu_admission_system.gui.views.NganhToHopView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NganhToHopController {
    private final NganhToHopView view;
    private final XtNganhToHopBus bus;
    private final XtNganhBus nganhBus;
    private final XtToHopMonThiBus toHopBus;
    private List<XtNganhToHop> allData = new ArrayList<>();
    private List<XtNganhToHop> filteredData = new ArrayList<>();
    private Map<String, XtNganh> nganhMap = new HashMap<>();
    private Map<String, XtToHopMonThi> toHopMap = new HashMap<>();
    private int currentPage = 1;

    public NganhToHopController(NganhToHopView view, XtNganhToHopBus bus, 
            XtNganhBus nganhBus, XtToHopMonThiBus toHopBus) {
        this.view = view;
        this.bus = bus;
        this.nganhBus = nganhBus;
        this.toHopBus = toHopBus;
    }

    public void init() {
        loadMaps();
        onRefresh();
    }

    private void loadMaps() {
        nganhMap.clear();
        toHopMap.clear();
        for (XtNganh nganh : nganhBus.findAll()) {
            nganhMap.put(nganh.getMaNganh(), nganh);
        }
        for (XtToHopMonThi toHop : toHopBus.findAll()) {
            toHopMap.put(toHop.getMaToHop(), toHop);
        }
    }

    public void onRefresh() {
        allData = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredData = new ArrayList<>(allData);
        } else {
            boolean searchMaNganh = view.isSearchByMaNganh();
            boolean searchTenNganh = view.isSearchByTenNganh();
            boolean searchMaToHop = view.isSearchByMaToHop();
            boolean searchMon = view.isSearchByMon();

            filteredData = new ArrayList<>();
            for (XtNganhToHop data : allData) {
                if (matchesSearchCriteria(data, keyword, searchMaNganh, searchTenNganh, searchMaToHop, searchMon)) {
                    filteredData.add(data);
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

    public void onViewRow(int row) {
        XtNganhToHop data = getDataAtRow(row);
        if (data == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xem.");
            return;
        }
        XtNganh nganh = nganhMap.get(data.getMaNganh());
        XtToHopMonThi toHop = toHopMap.get(data.getMaToHop());
        NganhToHopDialog dialog = new NganhToHopDialog(
                view.getWindow(),
                "Xem chi tiết",
                data,
                nganh,
                toHop,
                NganhToHopDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onDeleteRow(int row) {
        XtNganhToHop data = getDataAtRow(row);
        if (data == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa bản ghi này?")) {
            bus.deleteById(data.getId());
            onRefresh();
        }
    }

    public void onImport() {
        System.out.println("[DEBUG] onImport() called");
        try {
            File file = view.chooseExcelFile();
            System.out.println("[DEBUG] chooseExcelFile() returned: " + (file != null ? file.getName() : "null"));
            if (file == null) {
                return;
            }

            List<Map<String, String>> rows;
            try {
                rows = ExcelUtils.readRows(file, List.of(
                        "manganh", "ma_nganh", "ma nganh",
                        "matohop", "ma_to_hop", "ma to hop"));
            } catch (Exception ex) {
                view.showError("Không thể đọc file Excel: " + ex.getMessage());
                return;
            }

            List<XtNganhToHop> newRecords = new ArrayList<>();
            int updatedCount = 0;

            for (Map<String, String> row : rows) {
                String maNganh = getValue(row, "manganh", "ma_nganh", "ma nganh");
                String maToHopRaw = getValue(row, "matohop", "ma_to_hop", "ma to hop");

                if (maNganh == null || maNganh.isBlank() || maToHopRaw == null || maToHopRaw.isBlank()) {
                    continue;
                }

                maNganh = maNganh.trim();

                // Tách maToHop từ định dạng "B03(TO-3, VA-3, SI-1)" → "B03"
                String maToHop = extractMaToHop(maToHopRaw);
                if (maToHop == null || maToHop.isBlank()) {
                    continue;
                }

                // Parse các môn và hệ số từ định dạng "(TO-3, VA-3, SI-1)"
                List<String> monItems = parseMonFromFormat(maToHopRaw);
                
                String thMon1 = null, thMon2 = null, thMon3 = null;
                Integer hsMon1 = null, hsMon2 = null, hsMon3 = null;

                // Xử lý môn 1
                if (monItems.size() > 0) {
                    thMon1 = extractMonCode(monItems.get(0));
                    hsMon1 = extractMonCoefficient(monItems.get(0));
                }

                // Xử lý môn 2
                if (monItems.size() > 1) {
                    thMon2 = extractMonCode(monItems.get(1));
                    hsMon2 = extractMonCoefficient(monItems.get(1));
                }

                // Xử lý môn 3
                if (monItems.size() > 2) {
                    thMon3 = extractMonCode(monItems.get(2));
                    hsMon3 = extractMonCoefficient(monItems.get(2));
                }

                // Tạo tbKeys từ maNganh và maToHop
                String tbKeys = maNganh + "_" + maToHop;

                // Kiểm tra bản ghi đã tồn tại
                XtNganhToHop existing = bus.findByMaNganhAndMaToHop(maNganh, maToHop).orElse(null);
                if (existing != null) {
                    // Cập nhật các trường trống
                    boolean updated = false;
                    if (existing.getThMon1() == null || existing.getThMon1().isBlank()) {
                        existing.setThMon1(thMon1);
                        updated = true;
                    }
                    if (existing.getHsMon1() == null) {
                        existing.setHsMon1(hsMon1);
                        updated = true;
                    }
                    if (existing.getThMon2() == null || existing.getThMon2().isBlank()) {
                        existing.setThMon2(thMon2);
                        updated = true;
                    }
                    if (existing.getHsMon2() == null) {
                        existing.setHsMon2(hsMon2);
                        updated = true;
                    }
                    if (existing.getThMon3() == null || existing.getThMon3().isBlank()) {
                        existing.setThMon3(thMon3);
                        updated = true;
                    }
                    if (existing.getHsMon3() == null) {
                        existing.setHsMon3(hsMon3);
                        updated = true;
                    }
                    if (existing.getTbKeys() == null || existing.getTbKeys().isBlank()) {
                        existing.setTbKeys(tbKeys);
                        updated = true;
                    }
                    if (updated) {
                        bus.save(existing);
                        updatedCount++;
                    }
                } else {
                    // Thêm bản ghi mới
                    XtNganhToHop newRecord = new XtNganhToHop();
                    newRecord.setMaNganh(maNganh);
                    newRecord.setMaToHop(maToHop);
                    newRecord.setThMon1(thMon1);
                    newRecord.setHsMon1(hsMon1);
                    newRecord.setThMon2(thMon2);
                    newRecord.setHsMon2(hsMon2);
                    newRecord.setThMon3(thMon3);
                    newRecord.setHsMon3(hsMon3);
                    newRecord.setTbKeys(tbKeys);
                    newRecords.add(newRecord);
                }
            }

            if (newRecords.isEmpty() && updatedCount == 0) {
                view.showInfo("Không có dữ liệu hợp lệ để import.");
                return;
            }

            if (!newRecords.isEmpty()) {
                bus.saveAll(newRecords);
            }

            onRefresh();
            String message = String.format("Đã import %d bản ghi mới, cập nhật %d bản ghi.", 
                    newRecords.size(), updatedCount);
            view.showInfo(message);
        } catch (Exception ex) {
            view.showError("Lỗi import: " + ex.getMessage());
            ex.printStackTrace();
        }
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
     * Parse các môn học từ định dạng "(TO-3, VA-3, SI-1)" → [TO-3, VA-3, SI-1]
     */
    private List<String> parseMonFromFormat(String raw) {
        List<String> items = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return items;
        }

        int startIndex = raw.indexOf('(');
        int endIndex = raw.indexOf(')');
        if (startIndex < 0 || endIndex < 0 || startIndex >= endIndex) {
            return items;
        }

        String monPart = raw.substring(startIndex + 1, endIndex).trim();
        String[] monItemsArray = monPart.split(",");

        for (String item : monItemsArray) {
            String trimmed = item.trim();
            if (!trimmed.isBlank()) {
                items.add(trimmed);
            }
        }

        return items;
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
     * Lấy hệ số (số sau dấu "-") từ "TO-3" → 3 hoặc từ "NK1-1" → 1
     */
    private Integer extractMonCoefficient(String item) {
        if (item == null || item.isBlank()) {
            return null;
        }
        int dashIndex = item.indexOf('-');
        if (dashIndex >= 0 && dashIndex < item.length() - 1) {
            String coeffStr = item.substring(dashIndex + 1).trim();
            try {
                return Integer.parseInt(coeffStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredData.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtNganhToHop data = filteredData.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(data.getMaNganh()),
                    safeText(data.getMaToHop()),
                    safeText(data.getThMon1()),
                    safeText(data.getHsMon1()),
                    safeText(data.getThMon2()),
                    safeText(data.getHsMon2()),
                    safeText(data.getThMon3()),
                    safeText(data.getHsMon3()),
                    safeText(data.getTbKeys()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtNganhToHop getDataAtRow(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredData.size()) {
            return null;
        }
        return filteredData.get(globalIndex);
    }

    private boolean containsKeyword(XtNganhToHop data, String keyword) {
        return containsIgnoreCase(data.getMaNganh(), keyword)
                || containsIgnoreCase(data.getMaToHop(), keyword)
                || containsIgnoreCase(data.getThMon1(), keyword)
                || containsIgnoreCase(data.getThMon2(), keyword)
                || containsIgnoreCase(data.getThMon3(), keyword);
    }

    private boolean matchesSearchCriteria(XtNganhToHop data, String keyword, 
            boolean searchMaNganh, boolean searchTenNganh, boolean searchMaToHop, boolean searchMon) {
        if (searchMaNganh && containsIgnoreCase(data.getMaNganh(), keyword)) {
            return true;
        }
        if (searchTenNganh) {
            XtNganh nganh = nganhMap.get(data.getMaNganh());
            if (nganh != null && containsIgnoreCase(nganh.getTenNganh(), keyword)) {
                return true;
            }
        }
        if (searchMaToHop && containsIgnoreCase(data.getMaToHop(), keyword)) {
            return true;
        }
        if (searchMon && (containsIgnoreCase(data.getThMon1(), keyword)
                || containsIgnoreCase(data.getThMon2(), keyword)
                || containsIgnoreCase(data.getThMon3(), keyword))) {
            return true;
        }
        return false;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
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
