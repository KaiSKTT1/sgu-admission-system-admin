package com.example.KaiST.sgu_admission_system.dto;

import java.math.BigDecimal;

public class XetTuyenAdmittedRow {
    private final String cccd;
    private final String hoTen;
    private final String maNganh;
    private final String phuongThuc;
    private final BigDecimal diemXetTuyen;
    private final Integer chiTieu;
    private final BigDecimal diemChuan;

    public XetTuyenAdmittedRow(String cccd, String hoTen, String maNganh, String phuongThuc,
            BigDecimal diemXetTuyen, Integer chiTieu, BigDecimal diemChuan) {
        this.cccd = cccd;
        this.hoTen = hoTen;
        this.maNganh = maNganh;
        this.phuongThuc = phuongThuc;
        this.diemXetTuyen = diemXetTuyen;
        this.chiTieu = chiTieu;
        this.diemChuan = diemChuan;
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

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public Integer getChiTieu() {
        return chiTieu;
    }

    public BigDecimal getDiemChuan() {
        return diemChuan;
    }
}
