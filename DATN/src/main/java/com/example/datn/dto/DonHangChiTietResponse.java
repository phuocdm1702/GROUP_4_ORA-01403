package com.example.datn.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DonHangChiTietResponse(
        String maHoaDon,
        LocalDateTime ngayTao,
        String trangThai,
        String trangThaiThanhToan,
        String phuongThucThanhToan,
        String tenNguoiNhan,
        String sdtNguoiNhan,
        String diaChiGiaoHang,
        String ghiChu,
        BigDecimal tienTruocGiam,
        BigDecimal tienGiam,
        BigDecimal phiGiaoHang,
        BigDecimal tongTien,
        String maGiamGia,
        boolean coTheHuy,
        List<Dong> dong
) {
    public record Dong(
            Integer idSanPham,
            String tenSanPham,
            String maMau,
            String tenMau,
            String hex,
            String size,
            int soLuong,
            BigDecimal donGia,
            BigDecimal thanhTien,
            String anhUrl
    ) {}
}