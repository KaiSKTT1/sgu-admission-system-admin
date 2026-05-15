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
@Table(name = "xt_nganh")
public class XtNganh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh")
    private Integer idNganh;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "tennganh")
    private String tenNganh;

    @Column(name = "n_tohopgoc")
    private String toHopGoc;

    @Column(name = "n_chitieu")
    private Integer chiTieu;

    @Column(name = "n_diemsan")
    private BigDecimal diemSan;

    @Column(name = "n_diemtrungtuyen")
    private BigDecimal diemTrungTuyen;

    @Column(name = "n_tuyenthang")
    private String tuyenThang;

    @Column(name = "n_dgnl")
    private String dgnl;

    @Column(name = "n_thpt")
    private String thpt;

    @Column(name = "n_vsat")
    private String vsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt")
    private String slThpt;
}
