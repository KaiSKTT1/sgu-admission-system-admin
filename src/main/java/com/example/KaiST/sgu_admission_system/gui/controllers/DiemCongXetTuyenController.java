package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtDiemCongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.dto.DiemCongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import com.example.KaiST.sgu_admission_system.gui.dialogs.DiemCongXetTuyenDialog;
import com.example.KaiST.sgu_admission_system.gui.views.DiemCongXetTuyenView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiemCongXetTuyenController {
    private final DiemCongXetTuyenView view;
    private final XtDiemCongXetTuyenBus bus;
    private List<DiemCongXetTuyenRow> allRows = new ArrayList<>();
    private List<DiemCongXetTuyenRow> filteredRows = new ArrayList<>();
    private int currentPage = 1;

    public DiemCongXetTuyenController(DiemCongXetTuyenView view, XtDiemCongXetTuyenBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allRows = bus.findAllRows();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredRows = new ArrayList<>(allRows);
        } else {
            filteredRows = new ArrayList<>();
            for (DiemCongXetTuyenRow row : allRows) {
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

    public void onAdd() {
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Thêm điểm cộng",
                null,
                DiemCongXetTuyenDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        DiemCongXetTuyenRow rowData = getRowAt(row);
        if (rowData == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xem.");
            return;
        }
        XtDiemCongXetTuyen record = bus.findById(rowData.getIdDiemCong());
        if (record == null) {
            view.showInfo("Không tìm thấy bản ghi cần xem.");
            return;
        }
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Xem chi tiết",
                record,
                DiemCongXetTuyenDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        DiemCongXetTuyenRow rowData = getRowAt(row);
        if (rowData == null) {
            view.showInfo("Vui lòng chọn bản ghi cần sửa.");
            return;
        }
        XtDiemCongXetTuyen record = bus.findById(rowData.getIdDiemCong());
        if (record == null) {
            view.showInfo("Không tìm thấy bản ghi cần sửa.");
            return;
        }
        DiemCongXetTuyenDialog dialog = new DiemCongXetTuyenDialog(
                view.getWindow(),
                "Sửa điểm cộng",
                record,
                DiemCongXetTuyenDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getRow());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        DiemCongXetTuyenRow rowData = getRowAt(row);
        if (rowData == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        Integer id = rowData.getIdDiemCong();
        if (id == null) {
            view.showInfo("Không tìm thấy bản ghi cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa bản ghi này?")) {
            bus.deleteById(id);
            onRefresh();
        }
    }

    public void onImport() {
        File file = view.chooseExcelFile();
        if (file == null) {
            return;
        }

        try {
            // Sử dụng DiemCongImporter để xử lý logic tính toán điểm cộng
            com.example.KaiST.sgu_admission_system.utils.DiemCongImporter.ImportResult result = 
                com.example.KaiST.sgu_admission_system.utils.DiemCongImporter.importFromExcel(file.getAbsolutePath());
            
            StringBuilder message = new StringBuilder();
            message.append("Kết quả import:\\n");
            message.append("✓ Thành công: ").append(result.totalSuccess).append("\\n");
            if (result.totalSkipDuplicate > 0) {
                message.append("⊘ Bỏ qua (trùng): ").append(result.totalSkipDuplicate).append("\\n");
            }
            if (result.totalSkipError > 0) {
                message.append("✗ Lỗi: ").append(result.totalSkipError).append("\\n");
            }
            message.append("\\n📝 Log file: ").append(result.logPath);
            
            view.showInfo(message.toString());
            onRefresh();
        } catch (Exception ex) {
            view.showError("Lỗi khi import từ Excel: " + ex.getMessage());
            ex.printStackTrace();
        }
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
            DiemCongXetTuyenRow record = filteredRows.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(record.getTsCccd()),
                    safeText(resolveNguyenVong(record)),
                    safeText(resolveToHop(record)),
                    safeText(record.getDiemCc()),
                    safeText(record.getDiemUtxt()),
                    safeText(record.getDiemTong()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private DiemCongXetTuyenRow getRowAt(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredRows.size()) {
            return null;
        }
        return filteredRows.get(globalIndex);
    }

    private boolean containsKeyword(DiemCongXetTuyenRow record, String keyword) {
        return containsIgnoreCase(record.getTsCccd(), keyword);
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

    private boolean applyValue(Map<String, String> row, java.util.function.Consumer<String> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isEmpty()) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private boolean applyDecimal(Map<String, String> row, java.util.function.Consumer<BigDecimal> setter,
            String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isBlank()) {
            return false;
        }
        BigDecimal parsed = parseBigDecimal(value);
        if (parsed == null) {
            return false;
        }
        setter.accept(parsed);
        return true;
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

    private String resolveNguyenVong(DiemCongXetTuyenRow record) {
        String maNganh = record.getMaNganh();
        if (maNganh != null && !maNganh.isBlank()) {
            return maNganh.trim();
        }
        String keys = record.getNvKeys();
        if (keys != null && !keys.isBlank()) {
            return keys.trim();
        }
        Integer nvTt = record.getNvTt();
        return nvTt == null ? "" : "NV" + nvTt;
    }

    private String resolveToHop(DiemCongXetTuyenRow record) {
        String tenToHop = record.getTenToHop();
        if (tenToHop != null && !tenToHop.isBlank()) {
            return tenToHop.trim();
        }
        return safeText(record.getMaToHop());
    }
}
