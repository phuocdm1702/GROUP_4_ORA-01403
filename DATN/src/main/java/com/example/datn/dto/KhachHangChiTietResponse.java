package com.example.datn.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record KhachHangChiTietResponse(
        KhachHangResponse khachHang,
        List<DiaChiInfo> diaChi,
        List<DonInfo> donGanDay
) {
    public record DiaChiInfo(Integer id, String diaChiDayDu, Boolean macDinh) {}

    public record DonInfo(String maHoaDon, LocalDateTime ngayTao, BigDecimal tongTien, String trangThai) {}
}