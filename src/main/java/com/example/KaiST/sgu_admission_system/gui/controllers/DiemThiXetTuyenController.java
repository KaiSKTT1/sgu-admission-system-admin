package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XtDiemThiXetTuyenBus;
import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.gui.dialogs.DiemThiXetTuyenDialog;
import com.example.KaiST.sgu_admission_system.gui.views.DiemThiXetTuyenView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiemThiXetTuyenController {
    private final DiemThiXetTuyenView view;
    private final XtDiemThiXetTuyenBus bus;
    private List<XtDiemThiXetTuyen> allScores = new ArrayList<>();
    private List<XtDiemThiXetTuyen> filteredScores = new ArrayList<>();
    private int currentPage = 1;

    public DiemThiXetTuyenController(DiemThiXetTuyenView view, XtDiemThiXetTuyenBus bus) {
        this.view = view;
        this.bus = bus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allScores = bus.findAll();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredScores = new ArrayList<>(allScores);
        } else {
            filteredScores = new ArrayList<>();
            for (XtDiemThiXetTuyen score : allScores) {
                if (containsKeyword(score, keyword)) {
                    filteredScores.add(score);
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
        DiemThiXetTuyenDialog dialog = new DiemThiXetTuyenDialog(
                view.getWindow(),
                "Thêm điểm thi",
                null,
                DiemThiXetTuyenDialog.Mode.ADD);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getScore());
            onRefresh();
        }
    }

    public void onViewRow(int row) {
        XtDiemThiXetTuyen score = getScoreAtRow(row);
        if (score == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xem.");
            return;
        }
        DiemThiXetTuyenDialog dialog = new DiemThiXetTuyenDialog(
                view.getWindow(),
                "Xem chi tiết",
                score,
                DiemThiXetTuyenDialog.Mode.VIEW);
        dialog.setVisible(true);
    }

    public void onEditRow(int row) {
        XtDiemThiXetTuyen score = getScoreAtRow(row);
        if (score == null) {
            view.showInfo("Vui lòng chọn bản ghi cần sửa.");
            return;
        }
        DiemThiXetTuyenDialog dialog = new DiemThiXetTuyenDialog(
                view.getWindow(),
                "Sửa điểm thi",
                score,
                DiemThiXetTuyenDialog.Mode.EDIT);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            bus.save(dialog.getScore());
            onRefresh();
        }
    }

    public void onDeleteRow(int row) {
        XtDiemThiXetTuyen score = getScoreAtRow(row);
        if (score == null) {
            view.showInfo("Vui lòng chọn bản ghi cần xóa.");
            return;
        }
        if (view.confirm("Bạn có chắc chắn muốn xóa bản ghi này?")) {
            bus.deleteById(score.getIdDiemThi());
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
                    "cccd",
                    "sobaodanh",
                    "d_phuongthuc",
                    "phuongthuc",
                    "to",
                    "li",
                    "ho",
                    "si",
                    "su",
                    "di",
                    "va",
                    "n1_thi",
                    "n1_cc",
                    "cncn",
                    "cnnn",
                    "ti",
                    "ktpl",
                    "nl1",
                    "nk1",
                    "nk2"));
        } catch (Exception ex) {
            view.showError("Không thể đọc file Excel: " + ex.getMessage());
            return;
        }

        List<XtDiemThiXetTuyen> imported = new ArrayList<>();
        for (Map<String, String> row : rows) {
            XtDiemThiXetTuyen score = new XtDiemThiXetTuyen();
            boolean hasData = false;

            hasData |= applyValue(row, score::setCccd, "cccd");
            hasData |= applyValue(row, score::setSoBaoDanh, "sobaodanh", "sbd");
            hasData |= applyPhuongThuc(row, score);

            hasData |= applyDecimal(row, score::setTo, "to");
            hasData |= applyDecimal(row, score::setLi, "li");
            hasData |= applyDecimal(row, score::setHo, "ho");
            hasData |= applyDecimal(row, score::setSi, "si");
            hasData |= applyDecimal(row, score::setSu, "su");
            hasData |= applyDecimal(row, score::setDi, "di");
            hasData |= applyDecimal(row, score::setVa, "va");
            hasData |= applyDecimal(row, score::setN1Thi, "n1_thi", "n1thi");
            hasData |= applyDecimal(row, score::setN1Cc, "n1_cc", "n1cc");
            hasData |= applyDecimal(row, score::setCncn, "cncn");
            hasData |= applyDecimal(row, score::setCnnn, "cnnn");
            hasData |= applyDecimal(row, score::setTi, "ti");
            hasData |= applyDecimal(row, score::setKtpl, "ktpl");
            hasData |= applyDecimal(row, score::setNl1, "nl1");
            hasData |= applyDecimal(row, score::setNk1, "nk1");
            hasData |= applyDecimal(row, score::setNk2, "nk2");

            if (hasData) {
                imported.add(score);
            }
        }

        if (imported.isEmpty()) {
            view.showInfo("Không có dữ liệu hợp lệ để import.");
            return;
        }

        bus.saveAll(imported);
        onRefresh();
        view.showInfo("Đã import " + imported.size() + " bản ghi.");
    }

    private void updateTable() {
        int pageSize = view.getPageSize();
        int total = filteredScores.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        currentPage = Math.min(Math.max(1, currentPage), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Object[]> rows = new ArrayList<>();
        for (int i = start; i < end; i++) {
            XtDiemThiXetTuyen score = filteredScores.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(score.getCccd()),
                    safeText(score.getSoBaoDanh()),
                    phuongThucText(score.getPhuongThuc()),
                    safeText(score.getTo()),
                    safeText(score.getLi()),
                    ""
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private XtDiemThiXetTuyen getScoreAtRow(int row) {
        if (row < 0) {
            return null;
        }
        int pageSize = view.getPageSize();
        int globalIndex = (currentPage - 1) * pageSize + row;
        if (globalIndex < 0 || globalIndex >= filteredScores.size()) {
            return null;
        }
        return filteredScores.get(globalIndex);
    }

    private boolean containsKeyword(XtDiemThiXetTuyen score, String keyword) {
        return containsIgnoreCase(score.getCccd(), keyword)
                || containsIgnoreCase(score.getSoBaoDanh(), keyword)
                || containsIgnoreCase(phuongThucText(score.getPhuongThuc()), keyword);
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

    private boolean applyValue(Map<String, String> row, java.util.function.Consumer<String> setter, String... headers) {
        String value = getValue(row, headers);
        if (value == null || value.isEmpty()) {
            return false;
        }
        setter.accept(value);
        return true;
    }

    private boolean applyPhuongThuc(Map<String, String> row, XtDiemThiXetTuyen score) {
        String value = getValue(row, "d_phuongthuc", "phuongthuc", "phuong thuc");
        PhuongThuc method = PhuongThuc.fromText(value);
        if (method == null) {
            return false;
        }
        score.setPhuongThuc(method);
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

    private String phuongThucText(PhuongThuc method) {
        return method == null ? "" : method.getLabel();
    }
}
