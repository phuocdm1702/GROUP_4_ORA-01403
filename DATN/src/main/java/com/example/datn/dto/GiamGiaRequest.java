package com.example.datn.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record GiamGiaRequest(
        String maGiamGia,
        String tenGiamGia,
        String loaiGiamGia,          // SO_TIEN | PHAN_TRAM
        BigDecimal giaTriGiam,
        BigDecimal toiDaGiamGia,
        BigDecimal giaTriToiThieu,
        Integer soLuong,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String moTa,
        Boolean trangThai

) {
}
