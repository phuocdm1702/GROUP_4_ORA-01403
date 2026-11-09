package com.example.datn.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int trang,
        int kichThuoc,
        long tongSanPham,
        int tongTrang
) {}
