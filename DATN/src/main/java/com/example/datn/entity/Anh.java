package com.example.datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Anh")
@Getter @Setter
public class Anh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ao")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau")
    private MauSac mauSac;          // null = ảnh chung của sản phẩm

    @Column(name = "anh_url")
    private String anhUrl;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "thu_tu")
    private Integer thuTu;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}
