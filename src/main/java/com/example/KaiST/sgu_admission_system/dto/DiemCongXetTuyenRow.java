package com.example.KaiST.sgu_admission_system.dto;

import java.math.BigDecimal;

public class DiemCongXetTuyenRow {
    private final Integer idDiemCong;
    private final String tsCccd;
    private final String maNganh;
    private final String nvKeys;
    private final Integer nvTt;
    private final String tenToHop;
    private final String maToHop;
    private final BigDecimal diemCc;
    private final BigDecimal diemUtxt;
    private final BigDecimal diemTong;

    public DiemCongXetTuyenRow(Integer idDiemCong, String tsCccd, String maNganh, String nvKeys, Integer nvTt,
            String tenToHop, String maToHop, BigDecimal diemCc, BigDecimal diemUtxt, BigDecimal diemTong) {
        this.idDiemCong = idDiemCong;
        this.tsCccd = tsCccd;
        this.maNganh = maNganh;
        this.nvKeys = nvKeys;
        this.nvTt = nvTt;
        this.tenToHop = tenToHop;
        this.maToHop = maToHop;
        this.diemCc = diemCc;
        this.diemUtxt = diemUtxt;
        this.diemTong = diemTong;
    }

    public Integer getIdDiemCong() {
        return idDiemCong;
    }

    public String getTsCccd() {
        return tsCccd;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public String getNvKeys() {
        return nvKeys;
    }

    public Integer getNvTt() {
        return nvTt;
    }

    public String getTenToHop() {
        return tenToHop;
    }

    public String getMaToHop() {
        return maToHop;
    }

    public BigDecimal getDiemCc() {
        return diemCc;
    }

    public BigDecimal getDiemUtxt() {
        return diemUtxt;
    }

    public BigDecimal getDiemTong() {
        return diemTong;
    }
}
