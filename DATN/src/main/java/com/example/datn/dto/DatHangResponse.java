package com.example.datn.dto;

import java.math.BigDecimal;

public record DatHangResponse(String maHoaDon, BigDecimal tongTien, BigDecimal tienGiam, BigDecimal phiGiaoHang) {}