package com.example.datn.dto;
import java.math.BigDecimal;
public record ThongKeSanPhamItem(Integer idSanPham,
                                 String maAo,
                                 String tenAo,
                                 String tenChatLieu,
                                 long soLuongDaBan,
                                 BigDecimal doanhThu,
                                 long tongTonKho,
                                 int soBienThe) {
}
