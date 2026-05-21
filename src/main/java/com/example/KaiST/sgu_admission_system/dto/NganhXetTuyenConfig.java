package com.example.KaiST.sgu_admission_system.dto;

import java.math.BigDecimal;

/**
 * Cấu hình xét tuyển theo ngành: mã ngành, chỉ tiêu, điểm sàn (từ panel Ngành).
 */
public class NganhXetTuyenConfig {
    private final String maNganh;
    private final Integer chiTieu;
    private final BigDecimal diemSan;

    public NganhXetTuyenConfig(String maNganh, Integer chiTieu, BigDecimal diemSan) {
        this.maNganh = maNganh;
        this.chiTieu = chiTieu;
        this.diemSan = diemSan;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public Integer getChiTieu() {
        return chiTieu;
    }

    public BigDecimal getDiemSan() {
        return diemSan;
    }
}
