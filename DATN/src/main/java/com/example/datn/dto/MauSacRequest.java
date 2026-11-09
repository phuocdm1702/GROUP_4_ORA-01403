package com.example.datn.dto;

import jakarta.validation.constraints.NotBlank;

public record MauSacRequest(
        @NotBlank(message = "Mã màu không được để trống") String maMau,
        @NotBlank(message = "Tên màu không được để trống") String tenMau,
        String maHex,
        String moTa,
        Boolean trangThai
) {}
