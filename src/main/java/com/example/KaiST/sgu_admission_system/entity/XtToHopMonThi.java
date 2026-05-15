package com.example.KaiST.sgu_admission_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "xt_tohop_monthi")
public class XtToHopMonThi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtohop")
    private Integer idToHop;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "mon1")
    private String mon1;

    @Column(name = "mon2")
    private String mon2;

    @Column(name = "mon3")
    private String mon3;

    @Column(name = "tentohop")
    private String tenToHop;
}
