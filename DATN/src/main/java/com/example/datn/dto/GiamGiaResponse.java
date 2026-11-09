package com.example.datn.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record GiamGiaResponse(
        Integer id,
        String maGiamGia,
        String tenGiamGia,
        String loaiGiamGia,
        BigDecimal giaTriGiam,
        BigDecimal toiDaGiamGia,
        BigDecimal giaTriToiThieu,
        Integer soLuong,
        Integer soLuongDaDung,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String moTa,
        Boolean trangThai,
        String trangThaiHienThi,     // DANG_HOAT_DONG | SAP_DIEN_RA | DA_KET_THUC | NGUNG_HOAT_DONG
        BigDecimal soTienGiamDuKien  // Dùng khi gợi ý mã giảm giá tốt nhất cho đơn hàng
) {
}
