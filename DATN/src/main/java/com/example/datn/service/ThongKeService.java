package com.example.datn.service;
import com.example.datn.dto.*;
import com.example.datn.entity.HoaDon;
import com.example.datn.entity.HoaDonChiTiet;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.ChatLieuRepository;
import com.example.datn.repository.HoaDonChiTietRepository;
import com.example.datn.repository.HoaDonRepository;
import com.example.datn.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThongKeService {
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final ChatLieuRepository chatLieuRepository;

    // 5 trạng thái chuẩn theo CK_HoaDon_TrangThai trong SQL Server
    private static final String TT_CHO_XAC_NHAN = "Chờ xác nhận";
    private static final String TT_DA_XAC_NHAN = "Đã xác nhận";
    private static final String TT_DANG_GIAO = "Đang giao hàng";
    private static final String TT_HOAN_THANH = "Đã hoàn thành";
    private static final String TT_DA_HUY = "Đã hủy";
    private static final String TTTT_HOAN_TIEN = "Đã hoàn tiền";

    @Transactional(readOnly = true)
    public ThongKeDashboardResponse getThongKe(
            String boLocThoiGian,      // ALL | HOM_NAY | TUAN_NAY | THANG_NAY | THANG_TRUOC | NAM_NAY | TUY_CHON
            LocalDate tuNgay,
            LocalDate denNgay,
            String thongKeTheo,        // NGAY | THANG | NAM
            String tuKhoaSanPham,      // Tìm theo mã áo hoặc tên sản phẩm
            Integer idChatLieu,        // Lọc theo chất liệu
            String loaiSapXepSanPham   // BAN_CHAY | DOANH_THU_CAO | TON_KHO_THAP | TON_KHO_CAO
    ) {
        // 1. Tính khoảng thời gian lọc [startDateTime, endDateTime)
        LocalDate today = LocalDate.now();
        LocalDate start = null;
        LocalDate end = null;

        String timeMode = (boLocThoiGian == null || boLocThoiGian.isBlank()) ? "ALL" : boLocThoiGian.toUpperCase();
        switch (timeMode) {
            case "HOM_NAY" -> {
                start = today;
                end = today;
            }
            case "TUAN_NAY" -> {
                start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            }
            case "THANG_NAY" -> {
                start = today.withDayOfMonth(1);
                end = today.with(TemporalAdjusters.lastDayOfMonth());
            }
            case "THANG_TRUOC" -> {
                LocalDate prevMonth = today.minusMonths(1);
                start = prevMonth.withDayOfMonth(1);
                end = prevMonth.with(TemporalAdjusters.lastDayOfMonth());
            }
            case "NAM_NAY" -> {
                start = today.withDayOfYear(1);
                end = today.with(TemporalAdjusters.lastDayOfYear());
            }
            case "TUY_CHON" -> {
                start = tuNgay;
                end = denNgay;
            }
            default -> {
                start = null;
                end = null;
            }
        }

        LocalDateTime startDt = (start != null) ? start.atStartOfDay() : null;
        LocalDateTime endDt = (end != null) ? end.plusDays(1).atStartOfDay() : null;

        // 2. Lấy toàn bộ hóa đơn và lọc theo khoảng thời gian
        List<HoaDon> allOrders = hoaDonRepository.findAll();
        List<HoaDon> periodOrders = allOrders.stream()
                .filter(h -> h.getNgayTao() != null)
                .filter(h -> startDt == null || !h.getNgayTao().isBefore(startDt))
                .filter(h -> endDt == null || h.getNgayTao().isBefore(endDt))
                .toList();

        // 3. Lấy chi tiết hóa đơn của các đơn trong kỳ (dùng hàm findByOrderIds có sẵn)
        List<Integer> orderIds = periodOrders.stream().map(HoaDon::getId).toList();
        List<HoaDonChiTiet> allDetailsInPeriod = orderIds.isEmpty()
                ? List.of()
                : hoaDonChiTietRepository.findByOrderIds(orderIds);

        // Lọc chi tiết theo mã/tên sản phẩm hoặc chất liệu (nếu người dùng có chọn)
        String keyword = (tuKhoaSanPham != null) ? tuKhoaSanPham.trim().toLowerCase() : "";
        boolean hasProductFilter = !keyword.isEmpty() || idChatLieu != null;

        List<HoaDonChiTiet> matchedDetails = allDetailsInPeriod.stream()
                .filter(c -> {
                    SanPhamChiTiet v = c.getSanPhamChiTiet();
                    boolean matchKey = keyword.isEmpty()
                            || v.getSanPham().getMaAo().toLowerCase().contains(keyword)
                            || v.getSanPham().getTenAo().toLowerCase().contains(keyword)
                            || v.getMaAoChiTiet().toLowerCase().contains(keyword);
                    boolean matchCl = (idChatLieu == null)
                            || (v.getChatLieu() != null && idChatLieu.equals(v.getChatLieu().getId()));
                    return matchKey && matchCl;
                })
                .toList();

        Map<Integer, List<HoaDonChiTiet>> detailsByOrderId = matchedDetails.stream()
                .collect(Collectors.groupingBy(c -> c.getHoaDon().getId()));

        List<HoaDon> effectiveOrders = hasProductFilter
                ? periodOrders.stream().filter(h -> detailsByOrderId.containsKey(h.getId())).toList()
                : periodOrders;

        // 4. Tính 6 chỉ số KPI (Video [18:17 - 18:25])
        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        BigDecimal loiNhuanBanHang = BigDecimal.ZERO;
        long tongSoDonHang = effectiveOrders.size();
        long soDonHoanThanh = 0;
        long soDonHuyHoan = 0;
        BigDecimal tienDonHuyHoan = BigDecimal.ZERO;
        long tongSanPhamDaBan = 0;

        for (HoaDon h : effectiveOrders) {
            if (TT_HOAN_THANH.equals(h.getTrangThai())) {
                soDonHoanThanh++;
                List<HoaDonChiTiet> items = detailsByOrderId.getOrDefault(h.getId(), List.of());

                if (hasProductFilter) {
                    BigDecimal subTotal = items.stream()
                            .map(c -> c.getDonGia().multiply(BigDecimal.valueOf(c.getSoLuong())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    tongDoanhThu = tongDoanhThu.add(subTotal);
                    loiNhuanBanHang = loiNhuanBanHang.add(subTotal);
                } else {
                    tongDoanhThu = tongDoanhThu.add(h.getTongTien());
                    // Doanh thu thuần (tiền hàng sau giảm giá, không tính phí ship)
                    loiNhuanBanHang = loiNhuanBanHang.add(h.getTienTruocGiam().subtract(h.getTienGiamGia()));
                }

                tongSanPhamDaBan += items.stream().mapToLong(HoaDonChiTiet::getSoLuong).sum();
            } else if (TT_DA_HUY.equals(h.getTrangThai()) || TTTT_HOAN_TIEN.equals(h.getTrangThaiThanhToan())) {
                soDonHuyHoan++;
                tienDonHuyHoan = tienDonHuyHoan.add(h.getTongTien());
            }
        }

        BigDecimal giaTriTrungBinhDon = (soDonHoanThanh > 0)
                ? tongDoanhThu.divide(BigDecimal.valueOf(soDonHoanThanh), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Thống kê nhanh hôm nay cho nhân viên (Video [37:18])
        LocalDateTime startToday = today.atStartOfDay();
        LocalDateTime endToday = today.plusDays(1).atStartOfDay();
        BigDecimal doanhThuHomNay = allOrders.stream()
                .filter(h -> TT_HOAN_THANH.equals(h.getTrangThai())
                        && h.getNgayTao() != null
                        && !h.getNgayTao().isBefore(startToday)
                        && h.getNgayTao().isBefore(endToday))
                .map(HoaDon::getTongTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long soDonChoXuLy = allOrders.stream()
                .filter(h -> TT_CHO_XAC_NHAN.equals(h.getTrangThai()))
                .count();

        ThongKeKpiResponse kpi = new ThongKeKpiResponse(
                tongDoanhThu,
                loiNhuanBanHang,
                tongSoDonHang,
                soDonHoanThanh,
                soDonHuyHoan,
                tienDonHuyHoan,
                giaTriTrungBinhDon,
                tongSanPhamDaBan,
                doanhThuHomNay,
                soDonChoXuLy
        );

        // 5. Xây dựng dữ liệu Biểu đồ theo NGAY | THANG | NAM
        List<ThongKeBieuDoPoint> bieuDo = buildChartSeries(effectiveOrders, detailsByOrderId, thongKeTheo, hasProductFilter);

        // 6. Đếm số đơn theo 5 trạng thái cho khung bên phải của Dashboard.jsx
        Map<String, Long> trangThaiDonHang = new LinkedHashMap<>();
        for (String st : List.of(TT_CHO_XAC_NHAN, TT_DA_XAC_NHAN, TT_DANG_GIAO, TT_HOAN_THANH, TT_DA_HUY)) {
            long count = effectiveOrders.stream().filter(h -> st.equals(h.getTrangThai())).count();
            trangThaiDonHang.put(st, count);
        }

        // 7. Bảng thống kê sản phẩm & tồn kho (4 chế độ lọc: Bán chạy, Doanh thu cao, Tồn kho thấp, Tồn kho cao)
        List<ThongKeSanPhamItem> sanPhamList = buildProductTable(matchedDetails, keyword, idChatLieu, loaiSapXepSanPham);

        // 8. Lấy danh sách chất liệu để đổ vào Select trên giao diện
        List<ThongKeDashboardResponse.ChatLieuOption> dsChatLieu = chatLieuRepository.findAll().stream()
                .map(cl -> new ThongKeDashboardResponse.ChatLieuOption(cl.getId(), cl.getMaChatLieu(), cl.getTenChatLieu()))
                .toList();

        return new ThongKeDashboardResponse(kpi, bieuDo, sanPhamList, trangThaiDonHang, dsChatLieu);
    }

    private List<ThongKeBieuDoPoint> buildChartSeries(
            List<HoaDon> orders,
            Map<Integer, List<HoaDonChiTiet>> detailsByOrderId,
            String thongKeTheo,
            boolean hasProductFilter
    ) {
        String groupMode = (thongKeTheo == null || thongKeTheo.isBlank()) ? "THANG" : thongKeTheo.toUpperCase();

        DateTimeFormatter sortFormatter = switch (groupMode) {
            case "NAM" -> DateTimeFormatter.ofPattern("yyyy");
            case "NGAY" -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
            default -> DateTimeFormatter.ofPattern("yyyy-MM");
        };
        DateTimeFormatter labelFormatter = switch (groupMode) {
            case "NAM" -> DateTimeFormatter.ofPattern("'Năm' yyyy");
            case "NGAY" -> DateTimeFormatter.ofPattern("dd/MM/yyyy");
            default -> DateTimeFormatter.ofPattern("'T'MM/yyyy");
        };

        Map<String, List<HoaDon>> grouped = orders.stream()
                .filter(h -> TT_HOAN_THANH.equals(h.getTrangThai()) && h.getNgayTao() != null)
                .collect(Collectors.groupingBy(
                        h -> h.getNgayTao().format(sortFormatter),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<ThongKeBieuDoPoint> points = new ArrayList<>();
        grouped.forEach((key, list) -> {
            String label = list.get(0).getNgayTao().format(labelFormatter);
            BigDecimal rev = BigDecimal.ZERO;
            long soldQty = 0;

            for (HoaDon h : list) {
                List<HoaDonChiTiet> items = detailsByOrderId.getOrDefault(h.getId(), List.of());
                if (hasProductFilter) {
                    BigDecimal sub = items.stream()
                            .map(c -> c.getDonGia().multiply(BigDecimal.valueOf(c.getSoLuong())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    rev = rev.add(sub);
                } else {
                    rev = rev.add(h.getTongTien());
                }
                soldQty += items.stream().mapToLong(HoaDonChiTiet::getSoLuong).sum();
            }
            points.add(new ThongKeBieuDoPoint(label, rev, list.size(), soldQty));
        });

        return points;
    }

    private List<ThongKeSanPhamItem> buildProductTable(
            List<HoaDonChiTiet> matchedDetails,
            String keyword,
            Integer idChatLieu,
            String loaiSapXepSanPham
    ) {
        // Dùng hàm findAllForAdmin() đã có sẵn trong SanPhamChiTietRepository
        List<SanPhamChiTiet> allVariants = sanPhamChiTietRepository.findAllForAdmin().stream()
                .filter(v -> {
                    boolean matchKey = keyword.isEmpty()
                            || v.getSanPham().getMaAo().toLowerCase().contains(keyword)
                            || v.getSanPham().getTenAo().toLowerCase().contains(keyword)
                            || v.getMaAoChiTiet().toLowerCase().contains(keyword);
                    boolean matchCl = (idChatLieu == null)
                            || (v.getChatLieu() != null && idChatLieu.equals(v.getChatLieu().getId()));
                    return matchKey && matchCl;
                })
                .toList();

        Map<Integer, Long> soldMap = new HashMap<>();
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();

        for (HoaDonChiTiet c : matchedDetails) {
            if (TT_HOAN_THANH.equals(c.getHoaDon().getTrangThai())) {
                Integer productId = c.getSanPhamChiTiet().getSanPham().getId();
                soldMap.merge(productId, (long) c.getSoLuong(), Long::sum);
                BigDecimal lineAmount = c.getDonGia().multiply(BigDecimal.valueOf(c.getSoLuong()));
                revenueMap.merge(productId, lineAmount, BigDecimal::add);
            }
        }

        Map<Integer, List<SanPhamChiTiet>> byProduct = allVariants.stream()
                .collect(Collectors.groupingBy(v -> v.getSanPham().getId(), LinkedHashMap::new, Collectors.toList()));

        List<ThongKeSanPhamItem> result = new ArrayList<>();
        byProduct.forEach((spId, variants) -> {
            SanPhamChiTiet first = variants.get(0);
            long totalStock = variants.stream().mapToLong(SanPhamChiTiet::getSoLuong).sum();
            String chatLieuStr = variants.stream()
                    .map(v -> v.getChatLieu() != null ? v.getChatLieu().getTenChatLieu() : "")
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.joining(", "));

            result.add(new ThongKeSanPhamItem(
                    spId,
                    first.getSanPham().getMaAo(),
                    first.getSanPham().getTenAo(),
                    chatLieuStr,
                    soldMap.getOrDefault(spId, 0L),
                    revenueMap.getOrDefault(spId, BigDecimal.ZERO),
                    totalStock,
                    variants.size()
            ));
        });

        String sort = (loaiSapXepSanPham == null || loaiSapXepSanPham.isBlank())
                ? "BAN_CHAY"
                : loaiSapXepSanPham.toUpperCase();

        Comparator<ThongKeSanPhamItem> comparator = switch (sort) {
            case "DOANH_THU_CAO" -> Comparator.comparing(ThongKeSanPhamItem::doanhThu).reversed();
            case "TON_KHO_THAP" -> Comparator.comparingLong(ThongKeSanPhamItem::tongTonKho);
            case "TON_KHO_CAO" -> Comparator.comparingLong(ThongKeSanPhamItem::tongTonKho).reversed();
            default -> Comparator.comparingLong(ThongKeSanPhamItem::soLuongDaBan).reversed();
        };

        return result.stream().sorted(comparator).toList();
    }
}
