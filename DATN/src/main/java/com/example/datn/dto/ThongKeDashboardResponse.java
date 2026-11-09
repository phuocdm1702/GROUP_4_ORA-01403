package com.example.datn.dto;
import java.util.List;
import java.util.Map;
public record ThongKeDashboardResponse(
        ThongKeKpiResponse kpi,
        List<ThongKeBieuDoPoint> bieuDo,
        List<ThongKeSanPhamItem> sanPhamList,
        Map<String, Long> trangThaiDonHang,
        List<ChatLieuOption> danhSachChatLieu
) {
    public record ChatLieuOption(Integer id, String maChatLieu, String tenChatLieu) {}
}
