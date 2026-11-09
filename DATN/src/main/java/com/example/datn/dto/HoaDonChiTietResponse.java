package com.example.datn.dto;
import java.math.BigDecimal;
public record HoaDonChiTietResponse(Integer id,
                                    Integer idAoChiTiet,
                                    String maAoChiTiet,
                                    String tenAo,
                                    String tenMau,
                                    String maHex,
                                    String tenKichThuoc,
                                    String tenChatLieu,
                                    String anhUrl,
                                    Integer soLuong,
                                    Integer tonKhoHienTai,
                                    BigDecimal donGia,
                                    BigDecimal thanhTien) {
}
