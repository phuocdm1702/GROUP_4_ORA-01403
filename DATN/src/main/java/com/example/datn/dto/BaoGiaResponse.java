package com.example.datn.dto;

import java.math.BigDecimal;

public record BaoGiaResponse(
        BigDecimal tienTruocGiam,
        BigDecimal tienGiam,
        BigDecimal phiGiaoHang,
        BigDecimal tongTien,
        String maGiamGia
) {}
