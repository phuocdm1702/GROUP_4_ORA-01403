package com.example.datn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record DatHangRequest(
        @NotBlank(message = "Nhập họ tên người nhận")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự") String tenNguoiNhan,
        @NotBlank(message = "Nhập số điện thoại người nhận")
        @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ") String sdtNguoiNhan,
        @NotBlank(message = "Nhập tỉnh/thành phố") @Size(max = 45, message = "Tỉnh/thành phố tối đa 45 ký tự") String tinhThanhPho,
        @NotBlank(message = "Nhập quận/huyện") @Size(max = 45, message = "Quận/huyện tối đa 45 ký tự") String quanHuyen,
        @NotBlank(message = "Nhập phường/xã") @Size(max = 45, message = "Phường/xã tối đa 45 ký tự") String xaPhuong,
        @NotBlank(message = "Nhập số nhà, tên đường") @Size(max = 100, message = "Địa chỉ tối đa 100 ký tự") String duong,
        @Size(max = 255, message = "Ghi chú tối đa 255 ký tự") String ghiChu,
        @Size(max = 20, message = "Mã giảm giá không hợp lệ") String maGiamGia,
        @NotBlank(message = "Chọn phương thức thanh toán")
        @Pattern(regexp = "COD", message = "Phương thức thanh toán chưa được hỗ trợ") String phuongThuc,
        Boolean luuDiaChi,
        @NotEmpty(message = "Giỏ hàng trống") @Size(max = 50, message = "Giỏ hàng quá lớn") @Valid List<CartItemRequest> items
) {}
