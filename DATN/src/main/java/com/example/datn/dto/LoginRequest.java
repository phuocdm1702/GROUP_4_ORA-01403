package com.example.datn.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Vui lòng nhập tài khoản") String tenDangNhap,
        @NotBlank(message = "Vui lòng nhập mật khẩu") String matKhau
) {}
