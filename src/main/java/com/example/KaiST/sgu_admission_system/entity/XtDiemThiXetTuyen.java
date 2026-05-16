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
@Table(name = "xt_diemthixettuyen")
public class XtDiemThiXetTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private Integer idDiemThi;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "sobaodanh")
    private String soBaoDanh;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "`TO`")
    private BigDecimal to;

    @Column(name = "`LI`")
    private BigDecimal li;

    @Column(name = "`HO`")
    private BigDecimal ho;

    @Column(name = "`SI`")
    private BigDecimal si;

    @Column(name = "`SU`")
    private BigDecimal su;

    @Column(name = "`DI`")
    private BigDecimal di;

    @Column(name = "`VA`")
    private BigDecimal va;

    @Column(name = "N1_THI")
    private BigDecimal n1Thi;

    @Column(name = "N1_CC")
    private BigDecimal n1Cc;

    @Column(name = "CNCN")
    private BigDecimal cncn;

    @Column(name = "CNNN")
    private BigDecimal cnnn;

    @Column(name = "`TI`")
    private BigDecimal ti;

    @Column(name = "KTPL")
    private BigDecimal ktpl;

    @Column(name = "NL1")
    private BigDecimal nl1;

    @Column(name = "NK1")
    private BigDecimal nk1;

    @Column(name = "NK2")
    private BigDecimal nk2;
}
