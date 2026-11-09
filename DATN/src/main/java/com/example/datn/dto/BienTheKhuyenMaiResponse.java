package com.example.datn.dto;
import java.math.BigDecimal;

public record BienTheKhuyenMaiResponse(
        Integer id,
        Integer idSanPham,
        String maAo,
        String tenAo,
        String maAoChiTiet,
        String tenMau,
        String tenKichThuoc,
        String tenChatLieu,
        BigDecimal giaBanBanDau,
        BigDecimal giaBanHienTai,
        Integer soLuong,
        String maKhuyenMaiHienTai
) {}