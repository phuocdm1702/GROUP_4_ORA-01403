package com.example.datn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoiMatKhauRequest(
        @NotBlank(message = "Nhập mật khẩu hiện tại") String matKhauCu,
        @NotBlank(message = "Nhập mật khẩu mới")
        @Size(min = 6, max = 72, message = "Mật khẩu mới từ 6 đến 72 ký tự") String matKhauMoi
) {}