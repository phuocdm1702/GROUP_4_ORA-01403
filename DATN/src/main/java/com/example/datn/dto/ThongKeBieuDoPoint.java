package com.example.datn.dto;
import java.math.BigDecimal;
public record ThongKeBieuDoPoint(String mocThoiGian,               // Hiển thị trục X (VD: "02/11", "T11/2026", "2026")
                                 BigDecimal doanhThu,              // Doanh thu tại mốc thời gian đó
                                 long soDonHoanThanh,              // Số đơn hoàn thành
                                 long soSanPhamBan) {
}
