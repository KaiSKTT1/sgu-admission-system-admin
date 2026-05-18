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
@Table(name = "xt_user")
public class XtUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Integer idUser;

    @Column(name = "cccd")
    private String cccd;

    @Column(name = "hoten")
    private String hoTen;

    @Column(name = "ngaysinh")
    private String ngaySinh;

    @Column(name = "email")
    private String email;

    @Column(name = "dienthoai")
    private String dienThoai;

    @Column(name = "role")
    private String role;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "password")
    private String password;
}
