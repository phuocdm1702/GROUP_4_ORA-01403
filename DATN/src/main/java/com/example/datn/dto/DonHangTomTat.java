package com.example.datn.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonHangTomTat(
        String maHoaDon,
        LocalDateTime ngayTao,
        String trangThai,
        String trangThaiThanhToan,
        BigDecimal tongTien,
        int soSanPham,          // tổng số lượng
        int soDong,             // số loại sản phẩm
        String tenSanPhamDau,
        String hexDau,
        String anhUrl
) {}