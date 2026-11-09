package com.example.datn.dto;
import java.math.BigDecimal;
import java.util.List;
public record CapNhatThongTinHoaDonRequest(
        String tenNguoiNhan,
        String sdtNguoiNhan,
        String diaChiGiaoHang,
        BigDecimal phiGiaoHang,
        String ghiChuThem,
        List<ItemUpdate> items
) {
    public record ItemUpdate(
            Integer idHoaDonChiTiet,
            Integer soLuongMoi
    ) {}
}