package com.example.datn.dto;
import java.math.BigDecimal;
public record ThongKeKpiResponse(BigDecimal tongDoanhThu,          // Tổng tiền các đơn Đã hoàn thành
                                 BigDecimal loiNhuanBanHang,       // Doanh thu thuần = tien_truoc_giam - tien_giam_gia
                                 long tongSoDonHang,               // Tổng số đơn phát sinh trong kỳ
                                 long soDonHoanThanh,              // Số đơn Đã hoàn thành
                                 long soDonHuyHoan,                // Số đơn Đã hủy hoặc Đã hoàn tiền
                                 BigDecimal tienDonHuyHoan,        // Tổng tiền các đơn Đã hủy / Hoàn tiền
                                 BigDecimal giaTriTrungBinhDon,    // AOV = tongDoanhThu / soDonHoanThanh
                                 long tongSanPhamDaBan,            // Tổng số lượng áo đã bán (đơn Đã hoàn thành)
                                 BigDecimal doanhThuHomNay,        // Phục vụ widget thống kê nhanh hôm nay [37:18]
                                 long soDonChoXuLy) {
}
