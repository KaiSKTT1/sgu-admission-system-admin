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
@Table(name = "xt_bangquydoi")
public class XtBangQuyDoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idqd")
    private Integer idQd;

    @Column(name = "d_phuongthuc")
    private String phuongThuc;

    @Column(name = "d_tohop")
    private String toHop;

    @Column(name = "d_mon")
    private String mon;

    @Column(name = "d_diema")
    private BigDecimal diemA;

    @Column(name = "d_diemb")
    private BigDecimal diemB;

    @Column(name = "d_diemc")
    private BigDecimal diemC;

    @Column(name = "d_diemd")
    private BigDecimal diemD;

    @Column(name = "d_maquydoi")
    private String maQuyDoi;

    @Column(name = "d_phanvi")
    private String phanVi;
}
