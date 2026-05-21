package com.example.KaiST.sgu_admission_system.gui.controllers;

import com.example.KaiST.sgu_admission_system.bus.XetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtNguyenVongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtToHopMonThiBus;
import com.example.KaiST.sgu_admission_system.bus.XtDiemThiXetTuyenBus;
import com.example.KaiST.sgu_admission_system.bus.XtNganhToHopBus;
import com.example.KaiST.sgu_admission_system.bus.XtNganhBus;
import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.entity.XtToHopMonThi;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.bus.XtDiemCongXetTuyenBus;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;

import java.math.BigDecimal;
import java.util.Optional;
import com.example.KaiST.sgu_admission_system.dto.DiemXetTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.KetQuaTrungTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.NguyenVongXetTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.ThongKeNganhRow;
import com.example.KaiST.sgu_admission_system.gui.views.DiemXetTuyenView;
import com.example.KaiST.sgu_admission_system.utils.ExcelUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class DiemXetTuyenController {
    private final DiemXetTuyenView view;
    private final XetTuyenBus bus;
    private final XtNguyenVongXetTuyenBus nguyenVongBus;
    private List<NguyenVongXetTuyenRow> allRows = new ArrayList<>();
    private List<NguyenVongXetTuyenRow> filteredRows = new ArrayList<>();
    private int currentPage = 1;
    private XetTuyenBus.XetTuyenResult lastResult;
    private Runnable onXetTuyenComplete;
    private Set<String> admittedKeys = Set.of();
    private final XtToHopMonThiBus toHopMonThiBus = new XtToHopMonThiBus();
    private final XtDiemThiXetTuyenBus diemThiXetTuyenBus = new XtDiemThiXetTuyenBus();
    private final Map<String, XtToHopMonThi> toHopCache = new HashMap<>();
    private final Map<String, XtDiemThiXetTuyen> diemThiCache = new HashMap<>();
    private final XtNganhToHopBus xtNganhToHopBus = new XtNganhToHopBus();
    private final Map<String, List<XtNganhToHop>> nganhToHopCache = new HashMap<>();
    private final XtNganhBus xtNganhBus = new XtNganhBus();
    private final Map<String, String> nganhGocCache = new HashMap<>();
    private final XtDiemCongXetTuyenBus diemCongBus = new XtDiemCongXetTuyenBus();
    private final Map<String, BigDecimal> diemTongByCccdThm = new HashMap<>();

    private static final Map<String, Map<String, BigDecimal>> CONVERSION_TABLE = new HashMap<>();
    static {
        // Row A00
        Map<String, BigDecimal> rA00 = new HashMap<>();
        rA00.put("A00", new BigDecimal("0"));
        rA00.put("A01", new BigDecimal("-0.69"));
        rA00.put("B00", new BigDecimal("-1.21"));
        rA00.put("C00", new BigDecimal("2.32"));
        rA00.put("C01", new BigDecimal("0.94"));
        rA00.put("D01", new BigDecimal("-0.68"));
        rA00.put("D07", new BigDecimal("-1.62"));
        CONVERSION_TABLE.put("A00", rA00);

        // Row A01
        Map<String, BigDecimal> rA01 = new HashMap<>();
        rA01.put("A00", new BigDecimal("0.69"));
        rA01.put("A01", new BigDecimal("0"));
        rA01.put("B00", new BigDecimal("-0.52"));
        rA01.put("C00", new BigDecimal("3.01"));
        rA01.put("C01", new BigDecimal("1.63"));
        rA01.put("D01", new BigDecimal("0.01"));
        rA01.put("D07", new BigDecimal("-0.93"));
        CONVERSION_TABLE.put("A01", rA01);

        // Row B00
        Map<String, BigDecimal> rB00 = new HashMap<>();
        rB00.put("A00", new BigDecimal("1.21"));
        rB00.put("A01", new BigDecimal("0.52"));
        rB00.put("B00", new BigDecimal("0"));
        rB00.put("C00", new BigDecimal("3.53"));
        rB00.put("C01", new BigDecimal("2.15"));
        rB00.put("D01", new BigDecimal("0.53"));
        rB00.put("D07", new BigDecimal("-0.41"));
        CONVERSION_TABLE.put("B00", rB00);

        // Row C00
        Map<String, BigDecimal> rC00 = new HashMap<>();
        rC00.put("A00", new BigDecimal("-2.32"));
        rC00.put("A01", new BigDecimal("-3.01"));
        rC00.put("B00", new BigDecimal("-3.53"));
        rC00.put("C00", new BigDecimal("0"));
        rC00.put("C01", new BigDecimal("-1.38"));
        rC00.put("D01", new BigDecimal("-3.00"));
        rC00.put("D07", new BigDecimal("-3.94"));
        CONVERSION_TABLE.put("C00", rC00);

        // Row C01
        Map<String, BigDecimal> rC01 = new HashMap<>();
        rC01.put("A00", new BigDecimal("-0.94"));
        rC01.put("A01", new BigDecimal("-1.63"));
        rC01.put("B00", new BigDecimal("-2.15"));
        rC01.put("C00", new BigDecimal("1.38"));
        rC01.put("C01", new BigDecimal("0"));
        rC01.put("D01", new BigDecimal("-1.62"));
        rC01.put("D07", new BigDecimal("-2.56"));
        CONVERSION_TABLE.put("C01", rC01);

        // Row D01
        Map<String, BigDecimal> rD01 = new HashMap<>();
        rD01.put("A00", new BigDecimal("0.68"));
        rD01.put("A01", new BigDecimal("-0.01"));
        rD01.put("B00", new BigDecimal("-0.53"));
        rD01.put("C00", new BigDecimal("3.00"));
        rD01.put("C01", new BigDecimal("1.62"));
        rD01.put("D01", new BigDecimal("0"));
        rD01.put("D07", new BigDecimal("-0.94"));
        CONVERSION_TABLE.put("D01", rD01);
    }

    public DiemXetTuyenController(DiemXetTuyenView view, XetTuyenBus bus, XtNguyenVongXetTuyenBus nguyenVongBus) {
        this.view = view;
        this.bus = bus;
        this.nguyenVongBus = nguyenVongBus;
    }

    public void init() {
        onRefresh();
    }

    public void setOnXetTuyenComplete(Runnable onXetTuyenComplete) {
        this.onXetTuyenComplete = onXetTuyenComplete;
    }

    private void preloadToHop() {
        toHopCache.clear();
        for (XtToHopMonThi th : toHopMonThiBus.findAll()) {
            if (th.getMaToHop() != null) {
                toHopCache.put(th.getMaToHop(), th);
            }
        }
    }

    private void preloadDiemThi() {
        diemThiCache.clear();
        for (XtDiemThiXetTuyen dt : diemThiXetTuyenBus.findAll()) {
            if (dt.getCccd() != null) {
                diemThiCache.put(dt.getCccd(), dt);
            }
        }
    }

    private void preloadNganhToHop() {
        nganhToHopCache.clear();
        for (XtNganhToHop nth : xtNganhToHopBus.findAll()) {
            if (nth.getMaNganh() != null) {
                nganhToHopCache.computeIfAbsent(nth.getMaNganh(), k -> new ArrayList<>()).add(nth);
            }
        }
    }

    private void preloadNganhGoc() {
        nganhGocCache.clear();
        for (XtNganh n : xtNganhBus.findAll()) {
            if (n.getMaNganh() != null && n.getToHopGoc() != null) {
                nganhGocCache.put(n.getMaNganh().trim().toUpperCase(), n.getToHopGoc().trim().toUpperCase());
            }
        }
    }

    private void preloadDiemCong() {
        diemTongByCccdThm.clear();
        diemTongByCccdThm.putAll(diemCongBus.buildDiemTongByCccdAndToHop());
    }

    private BigDecimal lookupDiemTong(String cccd, String thm) {
        if (cccd == null || cccd.isBlank() || thm == null || thm.isBlank()) {
            return null;
        }
        return diemTongByCccdThm.get(XtDiemCongXetTuyenBus.buildLookupKey(cccd, thm));
    }

    public void onRefresh() {
        preloadToHop();
        preloadDiemThi();
        preloadNganhToHop();
        preloadNganhGoc();
        preloadDiemCong();
        allRows = nguyenVongBus.findAllWithThiSinhInfo();
        refreshAdmittedKeys();
        onSearch();
    }

    private void refreshAdmittedKeys() {
        try {
            bus.selectAdmittedByQuota("");
            admittedKeys = bus.buildAdmittedKeySet();
        } catch (Exception ex) {
            admittedKeys = new HashSet<>();
        }
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
        try {
            List<DiemXetTuyenRow> diemRows = bus.buildDiemXetTuyenRows();
            lastResult = bus.runXetTuyen(diemRows);
            int admitted = bus.selectAdmittedByQuota("").size();
            admittedKeys = bus.buildAdmittedKeySet();
            onSearch();
            if (onXetTuyenComplete != null) {
                onXetTuyenComplete.run();
            }
            view.showInfo("Đã xét tuyển: " + admitted + " thí sinh trúng tuyển (đã cập nhật panel Xét tuyển).");
        } catch (Exception ex) {
            view.showError("Không thể xét tuyển: " + ex.getMessage());
        }
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

    private BigDecimal getNormalScore(XtDiemThiXetTuyen diemThi, String maToHop) {
        String cleanToHop = maToHop.trim().toUpperCase();
        XtToHopMonThi toHop = toHopCache.get(cleanToHop);
        if (toHop == null) {
            toHop = toHopMonThiBus.findByMaToHop(cleanToHop).orElse(null);
            if (toHop != null) {
                toHopCache.put(cleanToHop, toHop);
            }
        }
        if (toHop == null) {
            return null;
        }

        BigDecimal s1 = getSubjectScore(diemThi, toHop.getMon1());
        BigDecimal s2 = getSubjectScore(diemThi, toHop.getMon2());
        BigDecimal s3 = getSubjectScore(diemThi, toHop.getMon3());

        if (s1 == null || s2 == null || s3 == null) {
            return null;
        }

        return s1.add(s2).add(s3);
    }

    private BigDecimal calculateDiemThxt(String cccd, String maNganh, String maToHopTarget) {
        if (cccd == null || cccd.isBlank() || maNganh == null || maNganh.isBlank() || maToHopTarget == null || maToHopTarget.isBlank()) {
            return null;
        }

        XtDiemThiXetTuyen diemThi = diemThiCache.get(cccd);
        if (diemThi == null) {
            return null;
        }

        String gocCombination = nganhGocCache.get(maNganh.trim().toUpperCase());
        if (gocCombination == null) {
            XtNganh n = xtNganhBus.findByMaNganh(maNganh).orElse(null);
            if (n != null && n.getToHopGoc() != null) {
                gocCombination = n.getToHopGoc().trim().toUpperCase();
                nganhGocCache.put(maNganh.trim().toUpperCase(), gocCombination);
            }
        }

        if (gocCombination == null) {
            return null;
        }

        boolean isGocInList = gocCombination.equals("A00") || gocCombination.equals("A01") ||
                              gocCombination.equals("B00") || gocCombination.equals("C00") ||
                              gocCombination.equals("C01") || gocCombination.equals("D01") ||
                              gocCombination.equals("D07");

        String toHopThiSinh = null;
        if (isGocInList) {
            String[] checkList = {"A00", "A01", "B00", "C00", "C01", "D01", "D07"};
            BigDecimal maxNormalScore = null;
            for (String code : checkList) {
                BigDecimal score = getNormalScore(diemThi, code);
                if (score != null) {
                    if (maxNormalScore == null || score.compareTo(maxNormalScore) > 0) {
                        maxNormalScore = score;
                        toHopThiSinh = code;
                    }
                }
            }
        } else {
            toHopThiSinh = gocCombination;
        }

        if (toHopThiSinh == null) {
            return null;
        }

        if (toHopThiSinh.equalsIgnoreCase(gocCombination)) {
            List<XtNganhToHop> nthList = nganhToHopCache.get(maNganh);
            if (nthList == null) {
                nthList = xtNganhToHopBus.findByMaNganh(maNganh);
                if (nthList != null) {
                    nganhToHopCache.put(maNganh, nthList);
                }
            }

            XtNganhToHop matched = null;
            if (nthList != null) {
                for (XtNganhToHop nth : nthList) {
                    if (nth.getMaToHop() != null && nth.getMaToHop().equalsIgnoreCase(gocCombination)) {
                        matched = nth;
                        break;
                    }
                }
            }

            String mon1, mon2, mon3;
            Integer w1, w2, w3;

            if (matched != null) {
                mon1 = matched.getThMon1();
                w1 = matched.getHsMon1();
                mon2 = matched.getThMon2();
                w2 = matched.getHsMon2();
                mon3 = matched.getThMon3();
                w3 = matched.getHsMon3();
            } else {
                XtToHopMonThi toHop = toHopCache.get(gocCombination);
                if (toHop == null) {
                    toHop = toHopMonThiBus.findByMaToHop(gocCombination).orElse(null);
                    if (toHop != null) {
                        toHopCache.put(gocCombination, toHop);
                    }
                }
                if (toHop == null) {
                    return null;
                }
                mon1 = toHop.getMon1();
                w1 = 1;
                mon2 = toHop.getMon2();
                w2 = 1;
                mon3 = toHop.getMon3();
                w3 = 1;
            }

            BigDecimal d1 = getSubjectScore(diemThi, mon1);
            BigDecimal d2 = getSubjectScore(diemThi, mon2);
            BigDecimal d3 = getSubjectScore(diemThi, mon3);

            if (d1 == null || d2 == null || d3 == null) {
                return null;
            }

            BigDecimal bgW1 = w1 != null ? BigDecimal.valueOf(w1) : BigDecimal.ONE;
            BigDecimal bgW2 = w2 != null ? BigDecimal.valueOf(w2) : BigDecimal.ONE;
            BigDecimal bgW3 = w3 != null ? BigDecimal.valueOf(w3) : BigDecimal.ONE;

            BigDecimal sumWeights = bgW1.add(bgW2).add(bgW3);
            if (sumWeights.compareTo(BigDecimal.ZERO) == 0) {
                return null;
            }

            BigDecimal weightedSum = d1.multiply(bgW1)
                                       .add(d2.multiply(bgW2))
                                       .add(d3.multiply(bgW3));
            BigDecimal score = weightedSum.divide(sumWeights, 4, java.math.RoundingMode.HALF_UP)
                                         .multiply(new BigDecimal("3"));
            return score.setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            BigDecimal normalScore = getNormalScore(diemThi, toHopThiSinh);
            if (normalScore == null) {
                return null;
            }

            Map<String, BigDecimal> rowMap = CONVERSION_TABLE.get(gocCombination);
            if (rowMap != null) {
                BigDecimal offset = rowMap.get(toHopThiSinh);
                if (offset != null) {
                    return normalScore.add(offset);
                }
            }
            return normalScore;
        }
    }

    private BigDecimal getSubjectScore(XtDiemThiXetTuyen dt, String monCode) {
        if (dt == null || monCode == null) {
            return null;
        }
        return switch (monCode.toUpperCase().trim()) {
            case "TO" -> dt.getTo();
            case "VA" -> dt.getVa();
            case "LI" -> dt.getLi();
            case "HO" -> dt.getHo();
            case "SI" -> dt.getSi();
            case "SU" -> dt.getSu();
            case "DI" -> dt.getDi();
            case "KTPL" -> dt.getKtpl();
            case "TI" -> dt.getTi();
            case "CNCN" -> dt.getCncn();
            case "CNNN" -> dt.getCnnn();
            case "N1" -> {
                BigDecimal thi = dt.getN1Thi();
                BigDecimal cc = dt.getN1Cc();
                if (thi == null && cc == null) yield null;
                if (thi == null) yield cc;
                if (cc == null) yield thi;
                yield thi.compareTo(cc) >= 0 ? thi : cc;
            }
            case "NL1" -> dt.getNl1();
            case "NK1" -> dt.getNk1();
            case "NK2" -> dt.getNk2();
            default -> null;
        };
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
            BigDecimal thxt = calculateDiemThxt(record.getNnCccd(), record.getNvMaNganh(), record.getTtThm());
            if (thxt == null) {
                thxt = record.getDiemThxt();
            }

            BigDecimal cong = lookupDiemTong(record.getNnCccd(), record.getTtThm());
            BigDecimal ut = record.getDiemUtqd();

            BigDecimal finalDiemXetTuyen = null;
            if (thxt != null) {
                finalDiemXetTuyen = thxt
                        .add(cong != null ? cong : BigDecimal.ZERO)
                        .add(ut != null ? ut : BigDecimal.ZERO);
            }

            String ketQua = isAdmitted(record) ? "Đậu" : "Rớt";

            rows.add(new Object[] {
                    stt,
                    safeText(record.getNnCccd()),
                    safeText(record.getTenThiSinh()),
                    safeText(record.getNvMaNganh()),
                    safeText(record.getNvTt()),
                    safeText(record.getTtPhuongThuc()),
                    safeText(record.getTtThm()),
                    thxt != null ? thxt : "",
                    cong != null ? cong : "",
                    ut != null ? ut : "",
                    finalDiemXetTuyen != null ? finalDiemXetTuyen : "",
                    ketQua,
                    safeText(record.getNvKeys())
            });
        }

        view.setTableRows(rows);
        view.updatePagination(currentPage, totalPages);
    }

    private boolean isAdmitted(NguyenVongXetTuyenRow record) {
        return admittedKeys.contains(buildAdmittedKey(record));
    }

    private String buildAdmittedKey(NguyenVongXetTuyenRow record) {
        PhuongThuc method = PhuongThuc.fromText(record.getTtPhuongThuc());
        String phuongThuc = method != null ? method.getLabel() : record.getTtPhuongThuc();
        return XetTuyenBus.buildAdmittedRowKey(record.getNnCccd(), record.getNvMaNganh(), phuongThuc);
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
