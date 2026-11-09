package com.example.datn.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaiKhoanResponse(
        String maKhachHang,
        String hoVaTen,
        String gioiTinh,
        LocalDate ngaySinh,
        String soDienThoai,
        String email,
        LocalDateTime ngayTao
) {}