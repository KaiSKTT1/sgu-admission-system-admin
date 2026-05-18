package com.example.KaiST.sgu_admission_system.dto;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import java.math.BigDecimal;

public class KetQuaTrungTuyenRow {
    private final String cccd;
    private final String hoTen;
    private final String maNganh;
    private final BigDecimal diemXetTuyen;
    private final PhuongThuc phuongThuc;

    public KetQuaTrungTuyenRow(String cccd, String hoTen, String maNganh, BigDecimal diemXetTuyen,
            PhuongThuc phuongThuc) {
        this.cccd = cccd;
        this.hoTen = hoTen;
        this.maNganh = maNganh;
        this.diemXetTuyen = diemXetTuyen;
        this.phuongThuc = phuongThuc;
    }

    public String getCccd() {
        return cccd;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public PhuongThuc getPhuongThuc() {
        return phuongThuc;
    }
}
