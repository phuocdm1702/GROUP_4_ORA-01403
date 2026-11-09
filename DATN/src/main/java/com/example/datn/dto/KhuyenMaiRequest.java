package com.example.datn.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public record KhuyenMaiRequest(String maKhuyenMai,
                               String tenKhuyenMai,
                               BigDecimal phanTramGiam,
                               LocalDate ngayBatDau,
                               LocalDate ngayKetThuc,
                               String moTa,
                               Boolean trangThai,
                               List<Integer> idSanPhamChiTietList // Danh sách ID biến thể sản phẩm được áp dụng đợt giảm giá
                               ) {

}
