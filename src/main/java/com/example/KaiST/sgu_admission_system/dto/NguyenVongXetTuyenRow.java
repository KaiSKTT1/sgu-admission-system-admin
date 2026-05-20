package com.example.KaiST.sgu_admission_system.dto;

import java.math.BigDecimal;

public class NguyenVongXetTuyenRow {
    private final Integer idNv;
    private final String nnCccd;
    private final String tenThiSinh;
    private final String nvMaNganh;
    private final Integer nvTt;
    private final String ttPhuongThuc;
    private final String ttThm;
    private final BigDecimal diemThxt;
    private final BigDecimal diemUtqd;
    private final BigDecimal diemCong;
    private final BigDecimal diemXetTuyen;
    private final String nvKetQua;
    private final String nvKeys;

    public NguyenVongXetTuyenRow(Integer idNv, String nnCccd, String tenThiSinh, String nvMaNganh,
            Integer nvTt, String ttPhuongThuc, String ttThm, BigDecimal diemThxt, 
            BigDecimal diemUtqd, BigDecimal diemCong, BigDecimal diemXetTuyen, 
            String nvKetQua, String nvKeys) {
        this.idNv = idNv;
        this.nnCccd = nnCccd;
        this.tenThiSinh = tenThiSinh;
        this.nvMaNganh = nvMaNganh;
        this.nvTt = nvTt;
        this.ttPhuongThuc = ttPhuongThuc;
        this.ttThm = ttThm;
        this.diemThxt = diemThxt;
        this.diemUtqd = diemUtqd;
        this.diemCong = diemCong;
        this.diemXetTuyen = diemXetTuyen;
        this.nvKetQua = nvKetQua;
        this.nvKeys = nvKeys;
    }

    public Integer getIdNv() {
        return idNv;
    }

    public String getNnCccd() {
        return nnCccd;
    }

    public String getTenThiSinh() {
        return tenThiSinh;
    }

    public String getNvMaNganh() {
        return nvMaNganh;
    }

    public Integer getNvTt() {
        return nvTt;
    }

    public String getTtPhuongThuc() {
        return ttPhuongThuc;
    }

    public String getTtThm() {
        return ttThm;
    }

    public BigDecimal getDiemThxt() {
        return diemThxt;
    }

    public BigDecimal getDiemUtqd() {
        return diemUtqd;
    }

    public BigDecimal getDiemCong() {
        return diemCong;
    }

    public BigDecimal getDiemXetTuyen() {
        return diemXetTuyen;
    }

    public String getNvKetQua() {
        return nvKetQua;
    }

    public String getNvKeys() {
        return nvKeys;
    }
}
