package com.example.datn.dto;

import java.time.LocalDate;

// Cố ý KHÔNG có mật khẩu
public record NhanVienResponse(
        Integer id,
        String maNhanVien,
        String hoVaTen,
        String gioiTinh,
        LocalDate ngaySinh,
        String soDienThoai,
        String diaChi,
        String email,
        String tenNguoiDung,
        Boolean trangThai,
        Integer idVaiTro,
        String tenVaiTro
) {}
