package com.example.KaiST.sgu_admission_system.utils;

import com.example.KaiST.sgu_admission_system.utils.NguyenVongImporter.ImportResult;

/**
 * Pipeline tổng hợp: Import nguyện vọng → Tính điểm tổ hợp.
 *
 * Gọi từ UI hoặc scheduled job:
 * NguyenVongScoreUpdater.run("path/to/candidate1.xlsx");
 *
 * Bước 1 – NguyenVongImporter.importFromExcel()
 * → Đọc file Excel Sheet2, insert vào xt_nguyenvongxettuyen
 * (nn_cccd, nv_tt, nv_manganh, tt_phuongthuc = "THPT").
 *
 * Bước 2 – DiemTohopCalculator.calculateAndUpdate()
 * → Với mọi bản ghi có diem_thxt IS NULL:
 * 1. Tra xt_nganh_tohop (manganh + tt_thm) → hệ số môn + độ lệch.
 * 2. Tra xt_diemthixettuyen (cccd) → điểm từng môn.
 * 3. diem_thxt = Σ(điểm_môn × hệ_số) + do_lech
 * 4. Tra xt_diemcongxetuyen → diem_utqd, diem_cong.
 * 5. diem_xettuyen = diem_thxt + diem_utqd + diem_cong / 2
 * 6. UPDATE xt_nguyenvongxettuyen.
 *
 * Lưu ý về tt_thm:
 * NguyenVongImporter để tt_thm = null. Bước tính điểm cần tt_thm (mã tổ hợp).
 * Do đó TRƯỚC KHI gọi calculateAndUpdate(), cần đảm bảo tt_thm đã được điền.
 *
 * Có hai cách:
 * A) File Excel cung cấp thêm cột mã tổ hợp → đọc trong NguyenVongImporter.
 * B) Dùng bảng xt_nganh lấy n_tohopgoc (tổ hợp gốc mặc định của ngành)
 * rồi cập nhật tt_thm trước khi tính.
 *
 * Class này cung cấp overload run(filePath, autoFillTtThm) để tự động
 * điền tt_thm từ n_tohopgoc nếu chưa có.
 */
public class NguyenVongScoreUpdater {

    /**
     * Chạy toàn bộ pipeline. tt_thm sẽ được tự động điền từ n_tohopgoc.
     */
    public static void run(String filePath) {
        run(filePath, true);
    }

    /**
     * @param filePath      Đường dẫn file Excel nguyện vọng.
     * @param autoFillTtThm true = tự điền tt_thm = n_tohopgoc nếu null.
     */
    public static void run(String filePath, boolean autoFillTtThm) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  SGU Admission – NguyenVongScoreUpdater          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        // ── Bước 1: Import nguyện vọng ───────────────────────────────────────
        System.out.println("\n[Bước 1] Import nguyện vọng từ Excel...");
        ImportResult importResult = NguyenVongImporter.importFromExcel(filePath);
        System.out.printf("         → Thành công: %d | Trùng: %d | Lỗi: %d%n",
                importResult.totalSuccess,
                importResult.totalSkipDuplicate,
                importResult.totalSkipError);
        System.out.println("         → Log: " + importResult.logPath);

        // ── Bước 2: Điền tt_thm nếu cần ─────────────────────────────────────
        if (autoFillTtThm) {
            System.out.println("\n[Bước 2] Tự động điền tt_thm từ n_tohopgoc...");
            int filled = fillTtThmFromNganhGoc();
            System.out.printf("         → Đã cập nhật %d bản ghi%n", filled);
        }

        // ── Bước 3: Tính điểm tổ hợp ────────────────────────────────────────
        System.out.println("\n[Bước 3] Tính điểm tổ hợp môn...");
        DiemTohopCalculator.calculateAndUpdate();

        System.out.println("\n[Xong] Pipeline hoàn thành.");
    }

    /**
     * Điền tt_thm = n_tohopgoc (tổ hợp gốc của ngành) cho các bản ghi
     * xt_nguyenvongxettuyen có tt_thm IS NULL.
     *
     * SQL tương đương:
     * UPDATE xt_nguyenvongxettuyen nv
     * JOIN xt_nganh n ON n.manganh = nv.nv_manganh
     * SET nv.tt_thm = n.n_tohopgoc
     * WHERE nv.tt_thm IS NULL;
     *
     * @return số bản ghi được cập nhật
     */
    public static int fillTtThmFromNganhGoc() {
        try (var session = com.example.KaiST.sgu_admission_system.config.HibernateUtil
                .getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            int updated = session.createNativeMutationQuery(
                    "UPDATE xt_nguyenvongxettuyen nv " +
                            "JOIN xt_nganh n ON n.manganh = nv.nv_manganh " +
                            "SET nv.tt_thm = n.n_tohopgoc " +
                            "WHERE nv.tt_thm IS NULL")
                    .executeUpdate();
            tx.commit();
            return updated;
        } catch (Exception e) {
            System.err.println("[fillTtThmFromNganhGoc] Lỗi: " + e.getMessage());
            return 0;
        }
    }
}