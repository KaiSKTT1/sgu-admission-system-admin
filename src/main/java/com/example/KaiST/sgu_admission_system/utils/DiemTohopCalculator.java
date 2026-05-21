package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.config.HibernateUtil;
import com.example.KaiST.sgu_admission_system.entity.XtNguyenVongXetTuyen;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tính điểm tổ hợp môn cho từng nguyện vọng trong bảng xt_nguyenvongxettuyen,
 * rồi cập nhật lại diem_thxt, diem_utqd, diem_cong, diem_xettuyen.
 *
 * ── Luồng xử lý (theo công thức Bộ GD&ĐT) ──────────────────────────────────
 * 1. Load tất cả nguyện vọng chưa có diem_thxt (= null), phương thức THPT.
 * 2. Với mỗi nguyện vọng:
 * a. Tra xt_nganh_tohop (manganh + tt_thm) → hệ số môn, do_lech.
 * b. Tra xt_diemthixettuyen (cccd) → điểm từng môn.
 * c. Tính ĐTHXT (điểm tổ hợp xét tuyển):
 * ĐTHXT = [(d1×w1 + d2×w2 + d3×w3) / W] × 3
 * trong đó W = w1 + w2 + w3
 * d. Tính ĐTHGXT (điểm tổ hợp gốc xét tuyển):
 * ĐTHGXT = ĐTHXT - do_lech
 * (do_lech = mức chênh lệch của tổ hợp đăng ký so với tổ hợp gốc ngành)
 * e. Tra xt_diemcongxetuyen → ĐC (điểm cộng), MĐƯT (mức điểm ưu tiên).
 * f. Tính ĐƯT (điểm ưu tiên) theo điều kiện:
 * - Nếu (ĐTHGXT + ĐC) < 22.5 : ĐƯT = MĐƯT
 * - Nếu (ĐTHGXT + ĐC) >= 22.5: ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐƯT
 * g. ĐXT = ĐTHGXT + ĐC + ĐƯT (tối đa 30 điểm)
 * 3. Cập nhật batch vào DB:
 * diem_thxt = ĐTHGXT
 * diem_utqd = ĐƯT
 * diem_cong = ĐC
 * diem_xettuyen = ĐXT
 *
 * ── Ghi chú môn NN ──────────────────────────────────────────────────────────
 * Khi tổ hợp có N1_FLAG=1, điểm NN = max(N1_THI, N1_CC).
 */
public class DiemTohopCalculator {

    private static final int BATCH_SIZE = 50;
    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    // ── Entry point ──────────────────────────────────────────────────────────

    public static void calculateAndUpdate() {
        System.out.println("[DiemTohopCalculator] Bắt đầu: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        // Load lookup tables
        Map<String, NganhTohopInfo> nganhTohopMap = loadNganhTohop();
        Map<String, DiemThiInfo> diemThiMap = loadDiemThi();
        Map<String, DiemCongInfo> diemCongMap = loadDiemCong();

        System.out.printf("  → xt_nganh_tohop: %d dòng%n", nganhTohopMap.size());
        System.out.printf("  → xt_diemthixettuyen: %d dòng%n", diemThiMap.size());
        System.out.printf("  → xt_diemcongxetuyen: %d dòng%n", diemCongMap.size());

        // Load nguyện vọng chưa tính điểm
        List<XtNguyenVongXetTuyen> pending = loadPendingNguyenVong();
        System.out.printf("  → Nguyện vọng chưa có điểm tổ hợp: %d%n", pending.size());

        int successCount = 0, skipCount = 0, errorCount = 0;
        List<XtNguyenVongXetTuyen> batch = new ArrayList<>(BATCH_SIZE);

        for (XtNguyenVongXetTuyen nv : pending) {
            try {
                // ── Key tra xt_nganh_tohop ─────────────────────────────────
                // tt_thm lưu mã tổ hợp (ví dụ: "D01", "A00")
                String tohopKey = buildNganhTohopKey(nv.getNvMaNganh(), nv.getTtThm());
                NganhTohopInfo tohopInfo = nganhTohopMap.get(tohopKey);

                if (tohopInfo == null) {
                    System.out.printf("  [SKIP] CCCD=%s | Ngành=%s | Tổ hợp=%s: Không tìm thấy trong xt_nganh_tohop%n",
                            nv.getNnCccd(), nv.getNvMaNganh(), nv.getTtThm());
                    skipCount++;
                    continue;
                }

                // ── Điểm thi của thí sinh ─────────────────────────────────
                DiemThiInfo diemThi = diemThiMap.get(nv.getNnCccd());
                if (diemThi == null) {
                    System.out.printf("  [SKIP] CCCD=%s: Không tìm thấy điểm thi%n", nv.getNnCccd());
                    skipCount++;
                    continue;
                }

                // ── Bước 1: Tính ĐTHXT = [(d1×w1 + d2×w2 + d3×w3) / W] × 3 ──
                BigDecimal dthxt = tinhDiemThxt(tohopInfo, diemThi);
                if (dthxt == null) {
                    System.out.printf("  [SKIP] CCCD=%s | Ngành=%s | Tổ hợp=%s: Thiếu điểm môn%n",
                            nv.getNnCccd(), nv.getNvMaNganh(), nv.getTtThm());
                    skipCount++;
                    continue;
                }

                // ── Bước 2: Tính ĐTHGXT = ĐTHXT - do_lech ───────────────
                // do_lech = mức chênh lệch của tổ hợp đăng ký so với tổ hợp gốc ngành
                BigDecimal doLech = tohopInfo.doLech != null ? tohopInfo.doLech : BigDecimal.ZERO;
                BigDecimal dthgxt = dthxt.subtract(doLech).setScale(SCALE, RM);

                // ── Bước 3: Điểm cộng ─────────────────────────────────────
                // Key: cccd|manganh|matohop
                String dcKey = nv.getNnCccd() + "|" + nv.getNvMaNganh() + "|" + nv.getTtThm();
                DiemCongInfo dc = diemCongMap.getOrDefault(dcKey, DiemCongInfo.ZERO);
                BigDecimal mdut = dc.diemUtxt != null ? dc.diemUtxt : BigDecimal.ZERO; // MĐƯT
                BigDecimal diemCong = dc.diemCC != null ? dc.diemCC : BigDecimal.ZERO; // ĐC

                // ── Bước 4: Tính ĐƯT theo điều kiện 22.5 ────────────────
                // Nếu (ĐTHGXT + ĐC) < 22.5 : ĐƯT = MĐƯT
                // Nếu (ĐTHGXT + ĐC) >= 22.5: ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐƯT
                BigDecimal diemUuTien = tinhDiemUuTien(dthgxt, diemCong, mdut);

                // ── Bước 5: ĐXT = ĐTHGXT + ĐC + ĐƯT (tối đa 30) ────────
                BigDecimal diemXetTuyen = dthgxt.add(diemCong).add(diemUuTien)
                        .min(new BigDecimal("30"))
                        .setScale(SCALE, RM);

                // ── Gán vào entity ────────────────────────────────────────
                // diem_thxt lưu ĐTHGXT (điểm tổ hợp gốc đã quy đổi)
                nv.setDiemThxt(dthgxt);
                nv.setDiemUtqd(diemUuTien.setScale(SCALE, RM));
                nv.setDiemCong(diemCong.setScale(SCALE, RM));
                nv.setDiemXetTuyen(diemXetTuyen);

                batch.add(nv);
                successCount++;

                if (batch.size() >= BATCH_SIZE) {
                    flushBatch(batch);
                    batch.clear();
                }

            } catch (Exception e) {
                System.err.printf("  [ERROR] CCCD=%s | Ngành=%s: %s%n",
                        nv.getNnCccd(), nv.getNvMaNganh(), e.getMessage());
                errorCount++;
            }
        }

        if (!batch.isEmpty()) {
            flushBatch(batch);
        }

        System.out.printf("[DiemTohopCalculator] Xong — Thành công: %d | Bỏ qua: %d | Lỗi: %d%n",
                successCount, skipCount, errorCount);
    }

    // ── Tính điểm tổ hợp xét tuyển (ĐTHXT) ─────────────────────────────────

    /**
     * ĐTHXT_THPT = [(d1×w1 + d2×w2 + d3×w3) / W] × 3
     * trong đó W = w1 + w2 + w3
     *
     * N1: lấy max(N1_THI, N1_CC).
     */
    private static BigDecimal tinhDiemThxt(NganhTohopInfo tohop, DiemThiInfo diemThi) {
        BigDecimal d1 = getMonScore(tohop.thMon1, tohop.n1Flag, diemThi);
        BigDecimal d2 = getMonScore(tohop.thMon2, tohop.n1Flag, diemThi);
        BigDecimal d3 = getMonScore(tohop.thMon3, tohop.n1Flag, diemThi);

        if (d1 == null || d2 == null || d3 == null)
            return null;

        BigDecimal w1 = BigDecimal.valueOf(tohop.hsMon1);
        BigDecimal w2 = BigDecimal.valueOf(tohop.hsMon2);
        BigDecimal w3 = BigDecimal.valueOf(tohop.hsMon3);
        BigDecimal W = w1.add(w2).add(w3);

        if (W.compareTo(BigDecimal.ZERO) == 0)
            return null;

        // [(d1×w1 + d2×w2 + d3×w3) / W] × 3
        BigDecimal tong = d1.multiply(w1).add(d2.multiply(w2)).add(d3.multiply(w3));
        return tong.divide(W, 10, RM).multiply(BigDecimal.valueOf(3)).setScale(SCALE, RM);
    }

    // ── Tính điểm ưu tiên (ĐƯT) ─────────────────────────────────────────────

    /**
     * Theo quy định Bộ GD&ĐT:
     * - Nếu (ĐTHGXT + ĐC) < 22.5 : ĐƯT = MĐƯT
     * - Nếu (ĐTHGXT + ĐC) >= 22.5 : ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐƯT
     *
     * @param dthgxt điểm tổ hợp gốc xét tuyển
     * @param dc     điểm cộng
     * @param mdut   mức điểm ưu tiên (từ bảng khu vực + đối tượng)
     */
    private static BigDecimal tinhDiemUuTien(BigDecimal dthgxt, BigDecimal dc, BigDecimal mdut) {
        if (mdut == null || mdut.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        BigDecimal tongCoSo = dthgxt.add(dc);
        BigDecimal nguong = new BigDecimal("22.5");

        if (tongCoSo.compareTo(nguong) < 0) {
            // Dưới 22.5: hưởng toàn bộ ưu tiên
            return mdut.setScale(SCALE, RM);
        } else {
            // Từ 22.5 trở lên: giảm dần theo công thức
            // ĐƯT = [(30 - ĐTHGXT - ĐC) / 7.5] × MĐƯT
            BigDecimal phanCon = new BigDecimal("30").subtract(tongCoSo);
            if (phanCon.compareTo(BigDecimal.ZERO) <= 0)
                return BigDecimal.ZERO;
            return phanCon.divide(new BigDecimal("7.5"), 10, RM)
                    .multiply(mdut)
                    .setScale(SCALE, RM);
        }
    }

    /**
     * Lấy điểm của một môn từ DiemThiInfo.
     * Môn "N1" → lấy max(N1_THI, N1_CC).
     */
    private static BigDecimal getMonScore(String monCode, boolean n1Flag, DiemThiInfo dt) {
        if (monCode == null)
            return null;
        return switch (monCode.toUpperCase()) {
            case "TO" -> dt.to;
            case "VA" -> dt.va;
            case "LI" -> dt.li;
            case "HO" -> dt.ho;
            case "SI" -> dt.si;
            case "SU" -> dt.su;
            case "DI" -> dt.di;
            case "KTPL" -> dt.ktpl;
            case "TI" -> dt.ti;
            case "CNCN" -> dt.cncn;
            case "CNNN" -> dt.cnnn;
            case "N1" -> maxNonNull(dt.n1Thi, dt.n1Cc); // ưu tiên thi thực hoặc CC
            default -> null;
        };
    }

    private static BigDecimal maxNonNull(BigDecimal a, BigDecimal b) {
        if (a == null && b == null)
            return null;
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.compareTo(b) >= 0 ? a : b;
    }

    // ── Load dữ liệu từ DB ───────────────────────────────────────────────────

    /** Load tất cả nguyện vọng chưa có điểm tổ hợp (diem_thxt IS NULL). */
    private static List<XtNguyenVongXetTuyen> loadPendingNguyenVong() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from XtNguyenVongXetTuyen where diemThxt IS NULL and ttPhuongThuc = 'THPT'",
                    XtNguyenVongXetTuyen.class).list();
        }
    }

    /**
     * Load bảng xt_nganh_tohop → Map<"manganh|matohop", NganhTohopInfo>.
     * Dùng native query để truy cập trực tiếp cột DB.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, NganhTohopInfo> loadNganhTohop() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT manganh, matohop, th_mon1, hsmon1, th_mon2, hsmon2, " +
                            "       th_mon3, hsmon3, N1_FLAG, do_lech " +
                            "FROM xt_nganh_tohop")
                    .list();

            Map<String, NganhTohopInfo> map = new HashMap<>(rows.size() * 2);
            for (Object[] r : rows) {
                NganhTohopInfo info = new NganhTohopInfo();
                info.maNganh = str(r[0]);
                info.maToHop = str(r[1]);
                info.thMon1 = str(r[2]);
                info.hsMon1 = toInt(r[3], 1);
                info.thMon2 = str(r[4]);
                info.hsMon2 = toInt(r[5], 1);
                info.thMon3 = str(r[6]);
                info.hsMon3 = toInt(r[7], 1);
                info.n1Flag = toInt(r[8], 0) == 1;
                info.doLech = toBD(r[9]);
                map.put(buildNganhTohopKey(info.maNganh, info.maToHop), info);
            }
            return map;
        }
    }

    /**
     * Load bảng xt_diemthixettuyen → Map<cccd, DiemThiInfo>.
     * Chỉ lấy bản ghi THPT (hoặc nếu cần mở rộng sang VSAT/DGNL thêm điều kiện).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, DiemThiInfo> loadDiemThi() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT cccd, `TO`, LI, HO, SI, SU, DI, VA, N1_THI, N1_CC, " +
                            "       CNCN, CNNN, TI, KTPL " +
                            "FROM xt_diemthixettuyen")
                    .list();

            Map<String, DiemThiInfo> map = new HashMap<>(rows.size() * 2);
            for (Object[] r : rows) {
                DiemThiInfo d = new DiemThiInfo();
                d.cccd = str(r[0]);
                d.to = toBD(r[1]); // TO
                d.li = toBD(r[2]); // LI
                d.ho = toBD(r[3]); // HO
                d.si = toBD(r[4]); // SI
                d.su = toBD(r[5]); // SU
                d.di = toBD(r[6]); // DI
                d.va = toBD(r[7]); // VA
                d.n1Thi = toBD(r[8]);
                d.n1Cc = toBD(r[9]);
                d.cncn = toBD(r[10]);
                d.cnnn = toBD(r[11]);
                d.ti = toBD(r[12]);
                d.ktpl = toBD(r[13]);
                if (d.cccd != null)
                    map.put(d.cccd, d);
            }
            return map;
        }
    }

    /**
     * Load bảng xt_diemcongxetuyen → Map<"cccd|manganh|matohop", DiemCongInfo>.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, DiemCongInfo> loadDiemCong() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createNativeQuery(
                    "SELECT ts_cccd, manganh, matohop, diemCC, diemUtxt " +
                            "FROM xt_diemcongxetuyen")
                    .list();

            Map<String, DiemCongInfo> map = new HashMap<>(rows.size() * 2);
            for (Object[] r : rows) {
                DiemCongInfo dc = new DiemCongInfo();
                dc.cccd = str(r[0]);
                dc.maNganh = str(r[1]);
                dc.maToHop = str(r[2]);
                dc.diemCC = toBD(r[3]);
                dc.diemUtxt = toBD(r[4]);
                String key = dc.cccd + "|" + dc.maNganh + "|" + dc.maToHop;
                map.put(key, dc);
            }
            return map;
        }
    }

    // ── Batch flush ──────────────────────────────────────────────────────────

    private static void flushBatch(List<XtNguyenVongXetTuyen> batch) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < batch.size(); i++) {
                session.merge(batch.get(i));
                if ((i + 1) % BATCH_SIZE == 0) {
                    session.flush();
                    session.clear();
                }
            }
            session.flush();
            tx.commit();
            System.out.printf("  [BATCH] Cập nhật %d bản ghi thành công%n", batch.size());
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            System.err.println("  [BATCH FAIL] " + e.getMessage());
            for (XtNguyenVongXetTuyen nv : batch) {
                System.err.printf("    - Rollback: CCCD=%s | Ngành=%s%n",
                        nv.getNnCccd(), nv.getNvMaNganh());
            }
        }
    }

    // ── Key builders ─────────────────────────────────────────────────────────

    /**
     * Key tra xt_nganh_tohop = manganh + "|" + matohop.
     * tt_thm trong xt_nguyenvongxettuyen lưu mã tổ hợp (D01, A00...).
     */
    private static String buildNganhTohopKey(String maNganh, String maToHop) {
        return (maNganh != null ? maNganh.trim() : "")
                + "|"
                + (maToHop != null ? maToHop.trim().toUpperCase() : "");
    }

    // ── Tiện ích chuyển kiểu ────────────────────────────────────────────────

    private static String str(Object o) {
        return o != null ? o.toString().trim() : null;
    }

    private static int toInt(Object o, int def) {
        if (o == null)
            return def;
        try {
            return ((Number) o).intValue();
        } catch (Exception e) {
            return def;
        }
    }

    private static BigDecimal toBD(Object o) {
        if (o == null)
            return null;
        if (o instanceof BigDecimal bd)
            return bd;
        try {
            return new BigDecimal(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ── Inner DTOs ───────────────────────────────────────────────────────────

    private static class NganhTohopInfo {
        String maNganh, maToHop;
        String thMon1, thMon2, thMon3;
        int hsMon1, hsMon2, hsMon3;
        boolean n1Flag;
        BigDecimal doLech;
    }

    private static class DiemThiInfo {
        String cccd;
        BigDecimal to, va, li, ho, si, su, di;
        BigDecimal n1Thi, n1Cc;
        BigDecimal cncn, cnnn, ti, ktpl;
    }

    private static class DiemCongInfo {
        String cccd, maNganh, maToHop;
        BigDecimal diemCC, diemUtxt;

        static final DiemCongInfo ZERO = new DiemCongInfo();
        static {
            ZERO.diemCC = BigDecimal.ZERO;
            ZERO.diemUtxt = BigDecimal.ZERO;
        }
    }
}