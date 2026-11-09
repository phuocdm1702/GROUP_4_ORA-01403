package com.example.datn.dto;

import java.math.BigDecimal;
import java.util.List;

public record SanPhamAdminResponse(
        Integer id,
        String maAo,
        String tenAo,
        String moTa,
        Boolean trangThai,
        Integer idThuongHieu,
        String thuongHieu,
        Integer idChatLieu,
        String chatLieu,
        int soBienThe,
        int tongTon,
        BigDecimal giaTu,
        BigDecimal giaDen,
        List<MauInfo> mau,
        List<BienThe> bienThe      // chỉ có khi xem chi tiết, danh sách thì để rỗng
) {
    public record MauInfo(Integer id, String ten, String hex) {}

    public record BienThe(
            Integer id,
            Integer idMau, String tenMau, String hex,
            Integer idKichThuoc, String tenKichThuoc,
            BigDecimal giaBanBanDau, BigDecimal giaBan,
            Integer soLuong, Boolean trangThai, String maAoChiTiet
    ) {}
}
