package com.example.KaiST.sgu_admission_system.dto;

import com.example.KaiST.sgu_admission_system.commen.PhuongThuc;
import java.math.BigDecimal;

public class DiemXetTuyenRow {
    private final String cccd;
    private final String hoTen;
    private final String maNganh;
    private final Integer nvTt;
    private final PhuongThuc phuongThuc;
    private final BigDecimal diemThmMax;
    private final BigDecimal diemThm;
    private final BigDecimal diemCong;
    private final BigDecimal diemUuTien;
    private final BigDecimal diemXetTuyen;

    public DiemXetTuyenRow(String cccd, String hoTen, String maNganh, Integer nvTt, PhuongThuc phuongThuc,
            BigDecimal diemThmMax, BigDecimal diemThm, BigDecimal diemCong, BigDecimal diemUuTien,
            BigDecimal diemXetTuyen) {
        this.cccd = cccd;
        this.hoTen = hoTen;
        this.maNganh = maNganh;
        this.nvTt = nvTt;
        this.phuongThuc = phuongThuc;
        this.diemThmMax = diemThmMax;
        this.diemThm = diemThm;
        this.diemCong = diemCong;
        this.diemUuTien = diemUuTien;
        this.diemXetTuyen = diemXetTuyen;
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

    public Integer getNvTt() {
        return nvTt;
    }

    public PhuongThuc getPhuongThuc() {
        return phuongThuc;
    }

    public BigDecimal getDiemThmMax() {
        return diemThmMax;
    }

    public BigDecimal getDiemThm() {
        return diemThm;
    }

    public BigDecimal getDiemCong() {
        return diemCong;
    }

    public BigDecimal getDiemUuTien() {
        return diemUuTien;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }
}
