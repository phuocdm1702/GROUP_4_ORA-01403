package com.example.datn.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "San_pham_chi_tiet")
@Getter @Setter
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ao")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu")
    private ChatLieu chatLieu;

    @Column(name = "ma_ao_chi_tiet")
    private String maAoChiTiet;

    @Column(name = "gia_ban_ban_dau")
    private BigDecimal giaBanBanDau;

    @Column(name = "gia_ban")
    private BigDecimal giaBan;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "anh_url")
    private String anhUrl;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khuyen_mai")
    private KhuyenMai khuyenMai;
}
