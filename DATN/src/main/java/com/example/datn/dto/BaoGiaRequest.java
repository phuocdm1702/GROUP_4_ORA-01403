package com.example.datn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BaoGiaRequest(
        @NotEmpty(message = "Giỏ hàng trống") @Size(max = 50, message = "Giỏ hàng quá lớn") @Valid List<CartItemRequest> items,
        @Size(max = 20, message = "Mã giảm giá không hợp lệ") String maGiamGia
) {}
