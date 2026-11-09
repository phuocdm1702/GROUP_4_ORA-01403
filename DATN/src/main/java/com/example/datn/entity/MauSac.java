package com.example.datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Mau_sac")
@Getter @Setter
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_mau", unique = true, nullable = false, length = 20)
    private String maMau;

    @Column(name = "ten_mau", nullable = false, columnDefinition = "NVARCHAR(60)")
    private String tenMau;

    @Column(name = "ma_hex", length = 20)
    private String maHex;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;
}
