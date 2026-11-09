package com.example.datn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record SanPhamRequest(
        @NotBlank(message = "Mã sản phẩm không được để trống") String maAo,
        @NotBlank(message = "Tên sản phẩm không được để trống") String tenAo,
        String moTa,
        Integer idThuongHieu,   // bỏ trống = dùng thương hiệu đầu tiên
        Integer idChatLieu,
        Boolean trangThai,
        @NotEmpty(message = "Cần ít nhất 1 biến thể (màu + kích thước)")
        @Valid List<BienTheRequest> bienThe
) {
    public record BienTheRequest(
            @NotNull(message = "Thiếu màu") Integer idMau,
            @NotNull(message = "Thiếu kích thước") Integer idKichThuoc,
            @NotNull(message = "Thiếu giá niêm yết") @DecimalMin(value = "0", message = "Giá không được âm") BigDecimal giaBanBanDau,
            @NotNull(message = "Thiếu giá bán") @DecimalMin(value = "0", message = "Giá không được âm") BigDecimal giaBan,
            @NotNull(message = "Thiếu số lượng") @Min(value = 0, message = "Số lượng không được âm") Integer soLuong,
            Boolean trangThai
    ) {}
}
