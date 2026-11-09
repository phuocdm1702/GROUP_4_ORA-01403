package com.example.datn.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "Thiếu mã sản phẩm") Integer idBienThe,
        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 99, message = "Số lượng tối đa là 99") Integer soLuong
) {}
