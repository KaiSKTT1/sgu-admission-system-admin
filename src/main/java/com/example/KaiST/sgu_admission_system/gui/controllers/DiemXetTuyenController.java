package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtNguyenVongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.dto.DiemXetTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.KetQuaTrungTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.NguyenVongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.ThongKeNganhRow;
import com.example.KaiST.sgu_admission_system.gui.views.DiemXetTuyenView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiemXetTuyenController {
    private final DiemXetTuyenView view;
    private final XetTuyenBus bus;
    private final XtNguyenVongXetTuyenBus nguyenVongBus;
    private List<NguyenVongXetTuyenRow> allRows = new ArrayList<>();
    private List<NguyenVongXetTuyenRow> filteredRows = new ArrayList<>();
    private int currentPage = 1;
    private XetTuyenBus.XetTuyenResult lastResult;

    public DiemXetTuyenController(DiemXetTuyenView view, XetTuyenBus bus, XtNguyenVongXetTuyenBus nguyenVongBus) {
        this.view = view;
        this.bus = bus;
        this.nguyenVongBus = nguyenVongBus;
    }

    public void init() {
        onRefresh();
    }

    public void onRefresh() {
        allRows = nguyenVongBus.findAllWithThiSinhInfo();
        onSearch();
    }

    public void onSearch() {
        String keyword = view.getSearchKeyword().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            filteredRows = new ArrayList<>(allRows);
        } else {
            filteredRows = new ArrayList<>();
            for (NguyenVongXetTuyenRow row : allRows) {
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

    public void onRunXetTuyen() {
        List<DiemXetTuyenRow> diemRows = bus.buildDiemXetTuyenRows();
        lastResult = bus.runXetTuyen(diemRows);
        int total = lastResult.getChiTiet().size();
        view.showInfo("Đã xét tuyển: " + total + " thí sinh trúng tuyển.");
    }

    public void onExport() {
        if (lastResult == null) {
            view.showInfo("Vui lòng chạy xét tuyển trước khi export.");
            return;
        }

        File detailFile = view.chooseSaveFile("Lưu danh sách trúng tuyển", "trung_tuyen_chi_tiet.xlsx");
        if (detailFile == null) {
            return;
        }
        File summaryFile = view.chooseSaveFile("Lưu thống kê theo ngành", "thong_ke_nganh.xlsx");
        if (summaryFile == null) {
            return;
        }

        try {
            exportChiTiet(detailFile, lastResult.getChiTiet());
            exportThongKe(summaryFile, lastResult.getThongKe());
            view.showInfo("Đã export 2 file kết quả.");
        } catch (Exception ex) {
            view.showError("Không thể export: " + ex.getMessage());
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
            NguyenVongXetTuyenRow record = filteredRows.get(i);
            int stt = i + 1;
            rows.add(new Object[] {
                    stt,
                    safeText(record.getNnCccd()),
                    safeText(record.getTenThiSinh()),
                    safeText(record.getNvMaNganh()),
                    safeText(record.getNvTt()),
                    safeText(record.getTtPhuongThuc()),
                    safeText(record.getTtThm()),
                    safeText(record.getDiemThxt()),
                    safeText(record.getDiemCong()),
                    safeText(record.getDiemUtqd()),
                    safeText(record.getDiemXetTuyen()),
                    safeText(record.getNvKetQua()),
                    safeText(record.getNvKeys())
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private boolean containsKeyword(NguyenVongXetTuyenRow row, String keyword) {
        return containsIgnoreCase(row.getNnCccd(), keyword)
                || containsIgnoreCase(row.getTenThiSinh(), keyword)
                || containsIgnoreCase(row.getNvMaNganh(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void exportChiTiet(File file, List<KetQuaTrungTuyenRow> rows) throws Exception {
        List<String> headers = List.of("CCCD", "Họ tên", "Mã ngành", "Điểm xét tuyển", "Phương thức");
        List<List<Object>> data = new ArrayList<>();
        for (KetQuaTrungTuyenRow row : rows) {
            data.add(List.of(
                    row.getCccd(),
                    row.getHoTen(),
                    row.getMaNganh(),
                    safeText(row.getDiemXetTuyen()),
                    phuongThucText(row.getPhuongThuc())));
        }
        ExcelUtils.writeRows(file, headers, data);
    }

    private void exportThongKe(File file, List<ThongKeNganhRow> rows) throws Exception {
        List<String> headers = List.of("Mã ngành", "Số lượng trúng tuyển");
        List<List<Object>> data = new ArrayList<>();
        for (ThongKeNganhRow row : rows) {
            data.add(List.of(row.getMaNganh(), row.getSoLuongTrungTuyen()));
        }
        ExcelUtils.writeRows(file, headers, data);
    }

    private String safeText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String phuongThucText(PhuongThuc method) {
        return method == null ? "" : method.getLabel();
    }
}
