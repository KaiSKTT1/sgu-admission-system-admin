package com.example.KaiST.sgu_admission_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_diemcongxetuyen")
public class XtDiemCongXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private Integer idDiemCong;

    @Column(name = "ts_cccd")
    private String tsCccd;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    @Column(name = "diemCC")
    private BigDecimal diemCc;

    @Column(name = "diemUtxt")
    private BigDecimal diemUtxt;

    @Column(name = "diemTong")
    private BigDecimal diemTong;

    @Column(name = "ghichu")
    private String ghiChu;

    @Column(name = "dc_keys")
    private String dcKeys;
}
