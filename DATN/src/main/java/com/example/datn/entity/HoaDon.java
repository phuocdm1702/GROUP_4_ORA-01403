package com.example.datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Hoa_don")
@Getter @Setter
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_giam_gia")
    private GiamGia giamGia;

    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;

    @Column(name = "sdt_nguoi_nhan")
    private String sdtNguoiNhan;

    @Column(name = "dia_chi_giao_hang")
    private String diaChiGiaoHang;

    @Column(name = "phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;

    @Column(name = "phuong_thuc_thanh_toan_online")
    private String phuongThucThanhToanOnline;

    @Column(name = "trang_thai_thanh_toan")
    private String trangThaiThanhToan;

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "tong_tien")
    private BigDecimal tongTien;

    @Column(name = "tien_truoc_giam")
    private BigDecimal tienTruocGiam;

    @Column(name = "tien_giam_gia")
    private BigDecimal tienGiamGia;

    @Column(name = "phi_giao_hang")
    private BigDecimal phiGiaoHang;

    @Column(name = "hinh_thuc_mua_hang")
    private String hinhThucMuaHang;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private String trangThai;

    // Database tự điền (DEFAULT SYSDATETIME)
    @Column(name = "ngay_tao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;
}
