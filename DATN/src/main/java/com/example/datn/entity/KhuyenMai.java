package com.example.datn.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Khuyen_mai")
@Getter @Setter
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai")
    private String maKhuyenMai;

    @Column(name = "ten_khuyen_mai")
    private String tenKhuyenMai;

    @Column(name = "phan_tram_giam")
    private BigDecimal phanTramGiam;

    @Column(name = "so_tien_giam")
    private BigDecimal soTienGiam;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}
