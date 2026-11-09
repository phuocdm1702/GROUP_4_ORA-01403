package com.example.datn.dto;

import java.math.BigDecimal;
import java.util.List;

public record StoreProductResponse(
        Integer id,
        String ten,
        String moTa,
        String chatLieu,
        String thuongHieu,
        List<Variant> bienThe,
        List<Hinh> hinhAnh
) {
    public record Mau(String ma, String ten, String hex) {}

    // maMau = null nghĩa là ảnh chung
    public record Hinh(String maMau, String url) {}

    public record Variant(
            Integer id,
            Mau mau,
            String size,
            BigDecimal giaGoc,
            BigDecimal giaBan,
            Integer soLuong,
            String anhUrl
    ) {}
}