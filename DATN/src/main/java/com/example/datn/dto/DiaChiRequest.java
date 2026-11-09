package com.example.datn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiaChiRequest(
        @NotBlank(message = "Nhập tỉnh/thành phố") @Size(max = 45, message = "Tỉnh/thành phố tối đa 45 ký tự") String tinhThanhPho,
        @NotBlank(message = "Nhập quận/huyện") @Size(max = 45, message = "Quận/huyện tối đa 45 ký tự") String quanHuyen,
        @NotBlank(message = "Nhập phường/xã") @Size(max = 45, message = "Phường/xã tối đa 45 ký tự") String xaPhuong,
        @NotBlank(message = "Nhập số nhà, tên đường") @Size(max = 100, message = "Địa chỉ tối đa 100 ký tự") String duong,
        Boolean macDinh
) {}