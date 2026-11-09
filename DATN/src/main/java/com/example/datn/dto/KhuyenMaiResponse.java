package com.example.datn.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record KhuyenMaiResponse(
        Integer id,
        String maKhuyenMai,
        String tenKhuyenMai,
        BigDecimal phanTramGiam,
        BigDecimal soTienGiam,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String moTa,
        Boolean trangThai,
        String trangThaiHienThi,      // DANG_DIEN_RA | SAP_DIEN_RA | DA_KET_THUC | NGUNG_HOAT_DONG
        int soBienTheApDung,
        List<Integer> idSanPhamChiTietList
) {}