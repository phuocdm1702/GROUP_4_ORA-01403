package com.example.datn.dto;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record KhachHangResponse(
        Integer id,
        String maKhachHang,
        String hoVaTen,
        String gioiTinh,
        LocalDate ngaySinh,
        String soDienThoai,
        String email,
        Boolean trangThai,
        LocalDateTime ngayTao,
        Integer soDon,
        BigDecimal tongChiTieu
) {}
