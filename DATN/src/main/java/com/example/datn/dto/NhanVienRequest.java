package com.example.datn.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record NhanVienRequest(
        @NotNull(message = "Chưa chọn vai trò") Integer idVaiTro,
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự") String hoVaTen,
        @Pattern(regexp = "Nam|Nữ|Khác", message = "Giới tính không hợp lệ") String gioiTinh,
        @Past(message = "Ngày sinh phải ở quá khứ") LocalDate ngaySinh,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ") String soDienThoai,
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự") String diaChi,
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email tối đa 120 ký tự") String email,
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 4, max = 60, message = "Tên đăng nhập từ 4 đến 60 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Tên đăng nhập chỉ gồm chữ không dấu, số, dấu . _ -") String tenNguoiDung,
        @Size(min = 6, max = 72, message = "Mật khẩu từ 6 đến 72 ký tự") String matKhau,   // sửa: để trống = không đổi
        Boolean trangThai
) {}
