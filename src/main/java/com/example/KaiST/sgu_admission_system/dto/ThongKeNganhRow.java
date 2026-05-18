package com.example.KaiST.sgu_admission_system.dto;

public class ThongKeNganhRow {
    private final String maNganh;
    private final int soLuongTrungTuyen;

    public ThongKeNganhRow(String maNganh, int soLuongTrungTuyen) {
        this.maNganh = maNganh;
        this.soLuongTrungTuyen = soLuongTrungTuyen;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public int getSoLuongTrungTuyen() {
        return soLuongTrungTuyen;
    }
}
