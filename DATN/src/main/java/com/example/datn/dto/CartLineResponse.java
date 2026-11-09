package com.example.datn.dto;

import java.math.BigDecimal;

public record CartLineResponse(
        Integer idBienThe,
        Integer idSanPham,
        String tenSanPham,
        StoreProductResponse.Mau mau,
        String size,
        BigDecimal giaGoc,
        BigDecimal giaBan,
        Integer tonKho,
        boolean khaDung,          // false = biến thể không còn bán
        String anhUrl
) {}
