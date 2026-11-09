package com.example.datn.dto;


import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record KhachHangRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự") String hoVaTen,
        @Pattern(regexp = "Nam|Nữ|Khác", message = "Giới tính không hợp lệ") String gioiTinh,
        @Past(message = "Ngày sinh phải ở quá khứ") LocalDate ngaySinh,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ") String soDienThoai,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email tối đa 120 ký tự") String email
) {}