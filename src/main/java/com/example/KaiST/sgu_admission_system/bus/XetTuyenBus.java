package com.example.KaiST.sgu_admission_system.bus;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import com.example.KaiST.sgu_admission_system.dto.DiemXetTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.KetQuaTrungTuyenRow;
import com.example.KaiST.sgu_admission_system.dto.ThongKeNganhRow;
import com.example.KaiST.sgu_admission_system.entity.XtBangQuyDoi;
import com.example.KaiST.sgu_admission_system.entity.XtDiemCongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtDiemThiXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtNganh;
import com.example.KaiST.sgu_admission_system.entity.XtNganhToHop;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;
import com.example.KaiST.sgu_admission_system.entity.XtThiSinhXetTuyen25;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class XetTuyenBus {
    private final XtThiSinhXetTuyen25Bus thiSinhBus;
    private final XtNguyenVongXetTuyenBus nguyenVongBus;
    private final XtDiemThiXetTuyenBus diemThiBus;
    private final XtDiemCongXetTuyenBus diemCongBus;
    private final XtBangQuyDoiBus bangQuyDoiBus;
    private final XtNganhBus nganhBus;
    private final XtNganhToHopBus nganhToHopBus;

    public XetTuyenBus(XtThiSinhXetTuyen25Bus thiSinhBus,
            XtNguyenVongXetTuyenBus nguyenVongBus,
            XtDiemThiXetTuyenBus diemThiBus,
            XtDiemCongXetTuyenBus diemCongBus,
            XtBangQuyDoiBus bangQuyDoiBus,
            XtNganhBus nganhBus,
            XtNganhToHopBus nganhToHopBus) {
        this.thiSinhBus = thiSinhBus;
        this.nguyenVongBus = nguyenVongBus;
        this.diemThiBus = diemThiBus;
        this.diemCongBus = diemCongBus;
        this.bangQuyDoiBus = bangQuyDoiBus;
        this.nganhBus = nganhBus;
        this.nganhToHopBus = nganhToHopBus;
    }

    public List<DiemXetTuyenRow> buildDiemXetTuyenRows() {
        List<XtThiSinhXetTuyen25> candidates = thiSinhBus.findAll();
        List<XtNguyenVongXetTuyen> nguyenVongs = nguyenVongBus.findAll();
        List<XtDiemThiXetTuyen> diemThi = diemThiBus.findAll();
        List<XtDiemCongXetTuyen> diemCong = diemCongBus.findAll();
        List<XtBangQuyDoi> bangQuyDoi = bangQuyDoiBus.findAll();
        List<XtNganh> nganh = nganhBus.findAll();
        List<XtNganhToHop> nganhToHop = nganhToHopBus.findAll();

        Map<String, XtThiSinhXetTuyen25> candidateByCccd = new HashMap<>();
        for (XtThiSinhXetTuyen25 candidate : candidates) {
            candidateByCccd.put(normalize(candidate.getCccd()), candidate);
        }

        Map<String, List<XtDiemThiXetTuyen>> diemThiByCccd = new HashMap<>();
        for (XtDiemThiXetTuyen score : diemThi) {
            String key = normalize(score.getCccd());
            diemThiByCccd.computeIfAbsent(key, ignored -> new ArrayList<>()).add(score);
        }

        Map<String, List<XtDiemCongXetTuyen>> diemCongByCccd = new HashMap<>();
        for (XtDiemCongXetTuyen record : diemCong) {
            String key = normalize(record.getTsCccd());
            diemCongByCccd.computeIfAbsent(key, ignored -> new ArrayList<>()).add(record);
        }

        Map<String, XtNganh> nganhByMa = new HashMap<>();
        for (XtNganh item : nganh) {
            nganhByMa.put(normalize(item.getMaNganh()), item);
        }

        Map<String, List<XtNganhToHop>> toHopByNganh = new HashMap<>();
        for (XtNganhToHop item : nganhToHop) {
            String key = normalize(item.getMaNganh());
            toHopByNganh.computeIfAbsent(key, ignored -> new ArrayList<>()).add(item);
        }

        Map<String, XtBangQuyDoi> quyDoiByMa = new HashMap<>();
        Map<String, XtBangQuyDoi> quyDoiByKey = new HashMap<>();
        for (XtBangQuyDoi item : bangQuyDoi) {
            String maQuyDoi = normalize(item.getMaQuyDoi());
            if (!maQuyDoi.isBlank()) {
                quyDoiByMa.put(maQuyDoi, item);
            }
            String method = normalizeMethod(item.getPhuongThuc());
            String toHop = normalize(item.getToHop());
            String mon = normalize(item.getMon());
            String phanVi = normalize(item.getPhanVi());
            String key = buildQuyDoiKey(method, toHop, mon, phanVi);
            quyDoiByKey.put(key, item);
        }

        List<DiemXetTuyenRow> result = new ArrayList<>();
        for (XtNguyenVongXetTuyen nv : nguyenVongs) {
            String cccd = normalize(nv.getNnCccd());
            if (cccd.isBlank()) {
                continue;
            }

            XtThiSinhXetTuyen25 candidate = candidateByCccd.get(cccd);
            if (candidate == null) {
                continue;
            }

            PhuongThuc method = PhuongThuc.fromText(nv.getTtPhuongThuc());
            if (method == null) {
                continue;
            }

            QuyDoiInfo quyDoiInfo = parseQuyDoiInfo(nv.getTtThm(), method);
            String toHopCode = normalize(quyDoiInfo.toHopCode);

            XtDiemThiXetTuyen score = findScore(diemThiByCccd.get(cccd), method);

            BigDecimal diemCongValue = findDiemCong(diemCongByCccd.get(cccd), nv.getNvMaNganh(),
                    method, toHopCode);
            BigDecimal diemUuTien = nv.getDiemUtqd();

            BigDecimal diemThm = null;
            BigDecimal diemThmMax = null;

            if (method == PhuongThuc.THPT) {
                List<XtNganhToHop> toHops = toHopByNganh.getOrDefault(normalize(nv.getNvMaNganh()),
                        List.of());
                diemThmMax = computeThmMax(score, toHops);
                XtNganhToHop toHop = findToHopByCode(toHops, toHopCode);
                diemThm = toHop != null ? computeThm(score, toHop) : diemThmMax;
            } else if (method == PhuongThuc.DGNL) {
                XtBangQuyDoi quyDoi = findQuyDoi(quyDoiByMa, quyDoiByKey, quyDoiInfo, method,
                        toHopCode, null);
                diemThm = convert(score == null ? null : score.getN1Thi(), quyDoi);
                diemThmMax = diemThm;
            } else if (method == PhuongThuc.VSAT) {
                List<XtNganhToHop> toHops = toHopByNganh.getOrDefault(normalize(nv.getNvMaNganh()),
                        List.of());
                XtNganhToHop toHop = findToHopByCode(toHops, toHopCode);
                diemThm = computeThmVsat(score, toHop, quyDoiByMa, quyDoiByKey, quyDoiInfo, method,
                        toHopCode);
                diemThmMax = computeThmMaxVsat(score, toHops, quyDoiByMa, quyDoiByKey, quyDoiInfo, method,
                        toHopCode);
            }

            BigDecimal diemXetTuyen = sum(diemThm, diemCongValue, diemUuTien);

            result.add(new DiemXetTuyenRow(
                    nv.getNnCccd(),
                    candidate.getTen(),
                    nv.getNvMaNganh(),
                    nv.getNvTt(),
                    method,
                    diemThmMax,
                    diemThm,
                    diemCongValue,
                    diemUuTien,
                    diemXetTuyen));
        }

        return result;
    }

    public XetTuyenResult runXetTuyen(List<DiemXetTuyenRow> rows) {
        List<XtNganh> nganh = nganhBus.findAll();
        Map<String, Integer> chiTieuByNganh = new HashMap<>();
        Map<String, BigDecimal> diemChuanByNganh = new HashMap<>();
        for (XtNganh item : nganh) {
            String key = normalize(item.getMaNganh());
            chiTieuByNganh.put(key, item.getChiTieu() == null ? 0 : item.getChiTieu());
            BigDecimal diemChuan = item.getDiemTrungTuyen() != null ? item.getDiemTrungTuyen() : item.getDiemSan();
            diemChuanByNganh.put(key, diemChuan);
        }

        Map<Integer, List<DiemXetTuyenRow>> byNv = new HashMap<>();
        for (DiemXetTuyenRow row : rows) {
            Integer nvTt = row.getNvTt();
            if (nvTt == null) {
                continue;
            }
            byNv.computeIfAbsent(nvTt, ignored -> new ArrayList<>()).add(row);
        }

        List<Integer> nvOrder = new ArrayList<>(byNv.keySet());
        nvOrder.sort(Comparator.naturalOrder());

        Set<String> admitted = new HashSet<>();
        List<KetQuaTrungTuyenRow> results = new ArrayList<>();

        // Xét theo thứ tự NV tăng dần: NV1 trước, NV2 sau, ...
        for (Integer nvTt : nvOrder) {
            Map<String, List<DiemXetTuyenRow>> groupByNganh = new HashMap<>();
            for (DiemXetTuyenRow row : byNv.get(nvTt)) {
                groupByNganh.computeIfAbsent(normalize(row.getMaNganh()), ignored -> new ArrayList<>()).add(row);
            }

            for (Map.Entry<String, List<DiemXetTuyenRow>> entry : groupByNganh.entrySet()) {
                String maNganh = entry.getKey();
                int remaining = chiTieuByNganh.getOrDefault(maNganh, 0);
                if (remaining <= 0) {
                    continue;
                }

                BigDecimal diemChuan = diemChuanByNganh.get(maNganh);
                List<DiemXetTuyenRow> candidates = new ArrayList<>();
                for (DiemXetTuyenRow row : entry.getValue()) {
                    if (admitted.contains(normalize(row.getCccd()))) {
                        continue;
                    }
                    if (row.getDiemXetTuyen() == null) {
                        continue;
                    }
                    if (diemChuan != null && row.getDiemXetTuyen().compareTo(diemChuan) < 0) {
                        continue;
                    }
                    candidates.add(row);
                }

                candidates.sort((a, b) -> compareDesc(a.getDiemXetTuyen(), b.getDiemXetTuyen()));

                int count = Math.min(remaining, candidates.size());
                for (int i = 0; i < count; i++) {
                    DiemXetTuyenRow row = candidates.get(i);
                    admitted.add(normalize(row.getCccd()));
                    results.add(new KetQuaTrungTuyenRow(
                            row.getCccd(),
                            row.getHoTen(),
                            row.getMaNganh(),
                            row.getDiemXetTuyen(),
                            row.getPhuongThuc()));
                }

                chiTieuByNganh.put(maNganh, remaining - count);
            }
        }

        Map<String, Integer> summary = new HashMap<>();
        for (KetQuaTrungTuyenRow row : results) {
            String key = normalize(row.getMaNganh());
            summary.put(key, summary.getOrDefault(key, 0) + 1);
        }

        List<ThongKeNganhRow> thongKe = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            thongKe.add(new ThongKeNganhRow(entry.getKey(), entry.getValue()));
        }

        return new XetTuyenResult(results, thongKe);
    }

    private XtDiemThiXetTuyen findScore(List<XtDiemThiXetTuyen> scores, PhuongThuc method) {
        if (scores == null || method == null) {
            return null;
        }
        for (XtDiemThiXetTuyen score : scores) {
            if (score.getPhuongThuc() == method) {
                return score;
            }
        }
        return null;
    }

    private XtNganhToHop findToHopByCode(List<XtNganhToHop> toHops, String code) {
        if (toHops == null || toHops.isEmpty()) {
            return null;
        }
        for (XtNganhToHop toHop : toHops) {
            if (normalize(toHop.getMaToHop()).equals(code)) {
                return toHop;
            }
        }
        return null;
    }

    private BigDecimal computeThmMax(XtDiemThiXetTuyen score, List<XtNganhToHop> toHops) {
        BigDecimal max = null;
        if (toHops == null) {
            return null;
        }
        for (XtNganhToHop toHop : toHops) {
            BigDecimal value = computeThm(score, toHop);
            if (value == null) {
                continue;
            }
            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }

    private BigDecimal computeThm(XtDiemThiXetTuyen score, XtNganhToHop toHop) {
        if (score == null || toHop == null) {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;

        total = total.add(applySubject(score, toHop.getThMon1(), toHop.getHsMon1()));
        total = total.add(applySubject(score, toHop.getThMon2(), toHop.getHsMon2()));
        total = total.add(applySubject(score, toHop.getThMon3(), toHop.getHsMon3()));

        hasValue = hasValue || hasScore(score, toHop.getThMon1())
                || hasScore(score, toHop.getThMon2())
                || hasScore(score, toHop.getThMon3());

        return hasValue ? total : null;
    }

    private BigDecimal applySubject(XtDiemThiXetTuyen score, String mon, Integer heSo) {
        BigDecimal value = getSubjectScore(score, mon);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        int hs = heSo == null || heSo <= 0 ? 1 : heSo;
        return value.multiply(BigDecimal.valueOf(hs));
    }

    private boolean hasScore(XtDiemThiXetTuyen score, String mon) {
        return getSubjectScore(score, mon) != null;
    }

    private BigDecimal computeThmVsat(XtDiemThiXetTuyen score, XtNganhToHop toHop,
            Map<String, XtBangQuyDoi> quyDoiByMa, Map<String, XtBangQuyDoi> quyDoiByKey,
            QuyDoiInfo quyDoiInfo, PhuongThuc method, String toHopCode) {
        if (score == null) {
            return null;
        }
        if (toHop == null) {
            XtBangQuyDoi fallback = findQuyDoi(quyDoiByMa, quyDoiByKey, quyDoiInfo, method, toHopCode, null);
            BigDecimal converted = convert(score.getTo(), fallback);
            return converted;
        }

        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;

        total = total.add(applyVsat(score, toHop.getThMon1(), toHop.getHsMon1(), quyDoiByMa, quyDoiByKey,
                quyDoiInfo, method, toHopCode));
        total = total.add(applyVsat(score, toHop.getThMon2(), toHop.getHsMon2(), quyDoiByMa, quyDoiByKey,
                quyDoiInfo, method, toHopCode));
        total = total.add(applyVsat(score, toHop.getThMon3(), toHop.getHsMon3(), quyDoiByMa, quyDoiByKey,
                quyDoiInfo, method, toHopCode));

        hasValue = hasValue || hasScore(score, toHop.getThMon1())
                || hasScore(score, toHop.getThMon2())
                || hasScore(score, toHop.getThMon3());

        return hasValue ? total : null;
    }

    private BigDecimal computeThmMaxVsat(XtDiemThiXetTuyen score, List<XtNganhToHop> toHops,
            Map<String, XtBangQuyDoi> quyDoiByMa, Map<String, XtBangQuyDoi> quyDoiByKey,
            QuyDoiInfo quyDoiInfo, PhuongThuc method, String toHopCode) {
        if (toHops == null) {
            return null;
        }
        BigDecimal max = null;
        for (XtNganhToHop toHop : toHops) {
            BigDecimal value = computeThmVsat(score, toHop, quyDoiByMa, quyDoiByKey, quyDoiInfo, method, toHopCode);
            if (value == null) {
                continue;
            }
            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }
        return max;
    }

    private BigDecimal applyVsat(XtDiemThiXetTuyen score, String mon, Integer heSo,
            Map<String, XtBangQuyDoi> quyDoiByMa, Map<String, XtBangQuyDoi> quyDoiByKey,
            QuyDoiInfo quyDoiInfo, PhuongThuc method, String toHopCode) {
        BigDecimal raw = getSubjectScore(score, mon);
        if (raw == null) {
            return BigDecimal.ZERO;
        }
        XtBangQuyDoi quyDoi = findQuyDoi(quyDoiByMa, quyDoiByKey, quyDoiInfo, method, toHopCode, mon);
        BigDecimal converted = convert(raw, quyDoi);
        if (converted == null) {
            return BigDecimal.ZERO;
        }
        int hs = heSo == null || heSo <= 0 ? 1 : heSo;
        return converted.multiply(BigDecimal.valueOf(hs));
    }

    private XtBangQuyDoi findQuyDoi(Map<String, XtBangQuyDoi> quyDoiByMa,
            Map<String, XtBangQuyDoi> quyDoiByKey,
            QuyDoiInfo info,
            PhuongThuc method,
            String toHop,
            String mon) {
        if (info != null && info.maQuyDoi != null) {
            XtBangQuyDoi direct = quyDoiByMa.get(normalize(info.maQuyDoi));
            if (direct != null) {
                return direct;
            }
        }
        String key = buildQuyDoiKey(method == null ? "" : method.getLabel(), normalize(toHop), normalize(mon),
                normalize(info == null ? null : info.phanVi));
        return quyDoiByKey.get(key);
    }

    private BigDecimal convert(BigDecimal x, XtBangQuyDoi quyDoi) {
        if (x == null || quyDoi == null) {
            return null;
        }
        BigDecimal a = quyDoi.getDiemA();
        BigDecimal b = quyDoi.getDiemB();
        BigDecimal c = quyDoi.getDiemC();
        BigDecimal d = quyDoi.getDiemD();
        if (a == null || b == null || c == null || d == null) {
            return null;
        }
        BigDecimal denom = b.subtract(a);
        if (BigDecimal.ZERO.compareTo(denom) == 0) {
            return null;
        }
        BigDecimal y = c.add(x.subtract(a).multiply(d.subtract(c)).divide(denom, 6, RoundingMode.HALF_UP));
        return y.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal findDiemCong(List<XtDiemCongXetTuyen> list, String maNganh, PhuongThuc method,
            String maToHop) {
        if (list == null) {
            return null;
        }
        String nganhKey = normalize(maNganh);
        String toHopKey = normalize(maToHop);
        for (XtDiemCongXetTuyen record : list) {
            if (!Objects.equals(normalize(record.getMaNganh()), nganhKey)) {
                continue;
            }
            if (!toHopKey.isBlank() && !Objects.equals(normalize(record.getMaToHop()), toHopKey)) {
                continue;
            }
            PhuongThuc recordMethod = PhuongThuc.fromText(record.getPhuongThuc());
            if (method != null && recordMethod != null && recordMethod != method) {
                continue;
            }
            return record.getDiemTong();
        }
        return null;
    }

    private BigDecimal sum(BigDecimal... values) {
        BigDecimal total = BigDecimal.ZERO;
        boolean hasValue = false;
        for (BigDecimal value : values) {
            if (value == null) {
                continue;
            }
            total = total.add(value);
            hasValue = true;
        }
        return hasValue ? total : null;
    }

    private int compareDesc(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return right.compareTo(left);
    }

    private BigDecimal getSubjectScore(XtDiemThiXetTuyen score, String mon) {
        if (score == null || mon == null) {
            return null;
        }
        String key = normalize(mon);
        switch (key) {
            case "TO":
                return score.getTo();
            case "LI":
                return score.getLi();
            case "HO":
                return score.getHo();
            case "SI":
                return score.getSi();
            case "SU":
                return score.getSu();
            case "DI":
                return score.getDi();
            case "VA":
                return score.getVa();
            case "N1":
            case "N1THI":
                return score.getN1Thi();
            case "N1CC":
                return score.getN1Cc();
            case "CNCN":
                return score.getCncn();
            case "CNNN":
                return score.getCnnn();
            case "TI":
                return score.getTi();
            case "KTPL":
                return score.getKtpl();
            case "NL1":
                return score.getNl1();
            case "NK1":
                return score.getNk1();
            case "NK2":
                return score.getNk2();
            default:
                return null;
        }
    }

    private QuyDoiInfo parseQuyDoiInfo(String raw, PhuongThuc method) {
        if (raw == null || raw.isBlank()) {
            return new QuyDoiInfo(null, null, null, method);
        }
        String trimmed = raw.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (upper.startsWith("DGNL_") || upper.startsWith("VSAT_")) {
            String[] parts = upper.split("_");
            String toHop = parts.length > 1 ? parts[1] : null;
            String phanVi = parts.length > 2 ? parts[2] : null;
            PhuongThuc parsedMethod = PhuongThuc.fromText(parts[0]);
            return new QuyDoiInfo(trimmed, toHop, phanVi, parsedMethod == null ? method : parsedMethod);
        }
        return new QuyDoiInfo(null, trimmed, null, method);
    }

    private String buildQuyDoiKey(String method, String toHop, String mon, String phanVi) {
        return normalize(method) + "|" + normalize(toHop) + "|" + normalize(mon) + "|" + normalize(phanVi);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeMethod(String value) {
        PhuongThuc method = PhuongThuc.fromText(value);
        return method == null ? normalize(value) : method.getLabel();
    }

    public static class XetTuyenResult {
        private final List<KetQuaTrungTuyenRow> chiTiet;
        private final List<ThongKeNganhRow> thongKe;

        public XetTuyenResult(List<KetQuaTrungTuyenRow> chiTiet, List<ThongKeNganhRow> thongKe) {
            this.chiTiet = chiTiet;
            this.thongKe = thongKe;
        }

        public List<KetQuaTrungTuyenRow> getChiTiet() {
            return chiTiet;
        }

        public List<ThongKeNganhRow> getThongKe() {
            return thongKe;
        }
    }

    private static class QuyDoiInfo {
        private final String maQuyDoi;
        private final String toHopCode;
        private final String phanVi;
        private final PhuongThuc method;

        private QuyDoiInfo(String maQuyDoi, String toHopCode, String phanVi, PhuongThuc method) {
            this.maQuyDoi = maQuyDoi;
            this.toHopCode = toHopCode;
            this.phanVi = phanVi;
            this.method = method;
        }
    }
}
