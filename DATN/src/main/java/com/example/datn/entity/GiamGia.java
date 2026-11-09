package com.example.datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Giam_gia")
@Getter @Setter
public class GiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_giam_gia")
    private String maGiamGia;

    @Column(name = "ten_giam_gia")
    private String tenGiamGia;

    @Column(name = "loai_giam_gia")
    private String loaiGiamGia;          // SO_TIEN | PHAN_TRAM

    @Column(name = "gia_tri_giam")
    private BigDecimal giaTriGiam;

    @Column(name = "toi_da_giam_gia")
    private BigDecimal toiDaGiamGia;

    @Column(name = "gia_tri_toi_thieu")
    private BigDecimal giaTriToiThieu;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung;

    @Column(name = "ngay_bat_dau")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}