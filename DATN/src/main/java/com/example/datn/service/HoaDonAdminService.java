package com.example.datn.service;
import com.example.datn.dto.*;
import com.example.datn.entity.*;
import com.example.datn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HoaDonAdminService {
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GiamGiaRepository giamGiaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final GiamGiaService giamGiaService;

    // 5 trạng thái chuẩn theo CK_HoaDon_TrangThai trong SQL Server
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DANG_GIAO_HANG = "Đang giao hàng";
    public static final String DA_HOAN_THANH = "Đã hoàn thành";
    public static final String DA_HUY = "Đã hủy";

    private static final Set<String> VALID_STATUSES = Set.of(
            CHO_XAC_NHAN, DA_XAC_NHAN, DANG_GIAO_HANG, DA_HOAN_THANH, DA_HUY
    );

    private static final BigDecimal FREE_SHIP_THRESHOLD = new BigDecimal("10000000");
    private static final DateTimeFormatter LOG_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Danh sách hóa đơn (Mặc định ngày hôm nay nếu không truyền ngày và tất cả=false)
    @Transactional(readOnly = true)
    public List<HoaDonResponse> searchOrders(String keyword, String trangThai, String trangThaiThanhToan,
                                             String hinhThucMuaHang, LocalDate tuNgay, LocalDate denNgay,
                                             boolean tatCaNgay) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (!tatCaNgay) {
            LocalDate from = (tuNgay != null) ? tuNgay : LocalDate.now();
            LocalDate to = (denNgay != null) ? denNgay : (tuNgay != null ? tuNgay : LocalDate.now());
            start = from.atStartOfDay();
            end = to.plusDays(1).atStartOfDay();
        } else {
            if (tuNgay != null) start = tuNgay.atStartOfDay();
            if (denNgay != null) end = denNgay.plusDays(1).atStartOfDay();
        }

        String cleanKey = keyword != null ? keyword.trim() : null;
        List<HoaDon> orders = hoaDonRepository.searchAdminOrders(
                cleanKey, trangThai, trangThaiThanhToan, hinhThucMuaHang, start, end
        );

        return orders.stream().map(h -> toResponse(h, List.of())).toList();
    }

    // Xem chi tiết 1 hóa đơn kèm danh sách sản phẩm
    @Transactional(readOnly = true)
    public HoaDonResponse getDetail(Integer id) {
        HoaDon h = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + id));
        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByOrderIds(List.of(id));
        return toResponse(h, items);
    }

    // Chuyển trạng thái hóa đơn sử dụng tryDeduct & hoanKho nguyên tử của SanPhamChiTietRepository
    @Transactional
    public HoaDonResponse updateStatus(Integer id, CapNhatTrangThaiHoaDonRequest req) {
        String moi = req.trangThaiMoi();
        if (moi == null || !VALID_STATUSES.contains(moi)) {
            throw new RuntimeException("Trạng thái không hợp lệ với CSDL: " + moi);
        }

        HoaDon h = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn ID: " + id));
        String cu = h.getTrangThai();

        if (cu.equals(moi)) return getDetail(id);
        if (DA_HOAN_THANH.equals(cu) || DA_HUY.equals(cu)) {
            throw new RuntimeException("Hóa đơn đã ở trạng thái kết thúc (" + cu + "), không thể chuyển trạng thái!");
        }

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByOrderIds(List.of(id));

        // 1. Chuyển từ "Chờ xác nhận" -> "Đã xác nhận": Dùng tryDeduct nguyên tử
        if (CHO_XAC_NHAN.equals(cu) && DA_XAC_NHAN.equals(moi)) {
            for (HoaDonChiTiet item : items) {
                SanPhamChiTiet spct = item.getSanPhamChiTiet();
                int updated = sanPhamChiTietRepository.tryDeduct(spct.getId(), item.getSoLuong());
                if (updated == 0) {
                    // Rollback toàn bộ @Transactional nếu bất kỳ sản phẩm nào không đủ tồn kho
                    throw new RuntimeException(
                            "Sản phẩm '" + spct.getSanPham().getTenAo() + " (" + spct.getMaAoChiTiet() + ")' " +
                                    "không đủ tồn kho hoặc đã ngừng bán! (Cần: " + item.getSoLuong() + ", Tồn: " + spct.getSoLuong() + ")"
                    );
                }
            }
        }

        // 2. Chuyển sang "Đã hủy": Dùng hoanKho nguyên tử nếu đơn đã từng trừ kho
        if (DA_HUY.equals(moi)) {
            if (DA_XAC_NHAN.equals(cu) || DANG_GIAO_HANG.equals(cu)) {
                for (HoaDonChiTiet item : items) {
                    sanPhamChiTietRepository.hoanKho(item.getSanPhamChiTiet().getId(), item.getSoLuong());
                }
            }
            if (h.getGiamGia() != null) {
                giamGiaRepository.hoanLuot(h.getGiamGia().getId());
            }
        }

        // 3. Chuyển sang "Đã hoàn thành": Tự động đổi sang Đã thanh toán
        if (DA_HOAN_THANH.equals(moi) && "Chưa thanh toán".equals(h.getTrangThaiThanhToan())) {
            h.setTrangThaiThanhToan("Đã thanh toán");
        }

        if (req.idNhanVien() != null && h.getNhanVien() == null) {
            nhanVienRepository.findById(req.idNhanVien()).ifPresent(h::setNhanVien);
        }

        h.setTrangThai(moi);
        appendLog(h, "Đổi TT: " + cu + " -> " + moi + (req.lyDoGhiChu() != null && !req.lyDoGhiChu().isBlank() ? " (" + req.lyDoGhiChu().trim() + ")" : ""));
        hoaDonRepository.save(h);

        return getDetail(id);
    }

    // Sửa thông tin giao hàng & số lượng sản phẩm khi đang "Chờ xác nhận" (Video [27:58 - 28:45])
    @Transactional
    public HoaDonResponse updateOrderInfo(Integer id, CapNhatThongTinHoaDonRequest req) {
        HoaDon h = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + id));

        if (!CHO_XAC_NHAN.equals(h.getTrangThai())) {
            throw new IllegalArgumentException("Chỉ được phép chỉnh sửa hóa đơn khi đang ở trạng thái 'Chờ xác nhận'!");
        }

        StringBuilder logBuilder = new StringBuilder();

        if (req.tenNguoiNhan() != null && !req.tenNguoiNhan().isBlank()) {
            h.setTenNguoiNhan(req.tenNguoiNhan().trim());
        }
        if (req.sdtNguoiNhan() != null && !req.sdtNguoiNhan().isBlank()) {
            h.setSdtNguoiNhan(req.sdtNguoiNhan().trim());
        }
        if (req.diaChiGiaoHang() != null && !req.diaChiGiaoHang().isBlank()
                && !req.diaChiGiaoHang().trim().equals(h.getDiaChiGiaoHang())) {
            h.setDiaChiGiaoHang(req.diaChiGiaoHang().trim());
            logBuilder.append("Đổi địa chỉ giao; ");
        }

        List<HoaDonChiTiet> currentItems = hoaDonChiTietRepository.findByOrderIds(List.of(id));

        // Cập nhật số lượng sản phẩm nếu có truyền lên
        if (req.items() != null && !req.items().isEmpty()) {
            for (CapNhatThongTinHoaDonRequest.ItemUpdate itemReq : req.items()) {
                if (itemReq.soLuongMoi() == null || itemReq.soLuongMoi() < 1) {
                    throw new IllegalArgumentException("Số lượng mỗi sản phẩm trong đơn phải ít nhất là 1!");
                }
                HoaDonChiTiet detail = currentItems.stream()
                        .filter(c -> c.getId().equals(itemReq.idHoaDonChiTiet()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng chi tiết ID: " + itemReq.idHoaDonChiTiet()));

                if (detail.getSanPhamChiTiet().getSoLuong() < itemReq.soLuongMoi()) {
                    throw new IllegalArgumentException("Sản phẩm " + detail.getSanPhamChiTiet().getMaAoChiTiet()
                            + " chỉ còn tồn kho " + detail.getSanPhamChiTiet().getSoLuong());
                }
                if (!detail.getSoLuong().equals(itemReq.soLuongMoi())) {
                    logBuilder.append(String.format("SP %s: SL %d->%d; ",
                            detail.getSanPhamChiTiet().getMaAoChiTiet(), detail.getSoLuong(), itemReq.soLuongMoi()));
                    detail.setSoLuong(itemReq.soLuongMoi());
                    hoaDonChiTietRepository.save(detail);
                }
            }
        }

        // Tính lại tienTruocGiam = SUM(soLuong * donGia)
        BigDecimal tienTruocGiamMoi = currentItems.stream()
                .map(c -> c.getDonGia().multiply(BigDecimal.valueOf(c.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tienTruocGiamCu = h.getTienTruocGiam();
        h.setTienTruocGiam(tienTruocGiamMoi);

        // Tính lại giảm giá theo voucher đang gắn (nếu còn đủ điều kiện tối thiểu)
        BigDecimal tienGiamMoi = BigDecimal.ZERO;
        if (h.getGiamGia() != null) {
            tienGiamMoi = giamGiaService.calculateDiscountAmount(h.getGiamGia(), tienTruocGiamMoi);
        }
        h.setTienGiamGia(tienGiamMoi);

        // Phí giao hàng: Nếu đơn >= 10 triệu thì miễn phí vận chuyển (Video [33:40])
        BigDecimal phiShipCu = h.getPhiGiaoHang();
        BigDecimal phiShipMoi = req.phiGiaoHang() != null ? req.phiGiaoHang() : h.getPhiGiaoHang();
        if (tienTruocGiamMoi.compareTo(FREE_SHIP_THRESHOLD) >= 0) {
            phiShipMoi = BigDecimal.ZERO;
        }
        if (phiShipCu.compareTo(phiShipMoi) != 0) {
            logBuilder.append(String.format("Phí ship: %s->%s; ", phiShipCu.toPlainString(), phiShipMoi.toPlainString()));
        }
        h.setPhiGiaoHang(phiShipMoi);

        // Bắt buộc theo CK_HoaDon_Tien: tong_tien = tien_truoc_giam - tien_giam_gia + phi_giao_hang
        BigDecimal tongTienMoi = tienTruocGiamMoi.subtract(tienGiamMoi).add(phiShipMoi);
        if (tienTruocGiamCu.compareTo(tienTruocGiamMoi) != 0) {
            logBuilder.append(String.format("Tổng tiền hàng: %s->%s; ", tienTruocGiamCu.toPlainString(), tienTruocGiamMoi.toPlainString()));
        }
        h.setTongTien(tongTienMoi);

        if (!logBuilder.isEmpty()) {
            appendLog(h, logBuilder.toString());
        }
        hoaDonRepository.save(h);
        return toResponse(h, currentItems);
    }

    // Xác nhận hoàn phí cho đơn chuyển khoản/VNPay khi giao thất bại (Video [29:31 - 29:59])
    @Transactional
    public HoaDonResponse confirmRefund(Integer id, String ghiChu) {
        HoaDon h = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn ID: " + id));

        List<HoaDonChiTiet> items = hoaDonChiTietRepository.findByOrderIds(List.of(id));

        // Nếu chưa hủy thì hủy và hoàn kho
        if (!DA_HUY.equals(h.getTrangThai())) {
            if (DA_XAC_NHAN.equals(h.getTrangThai()) || DANG_GIAO_HANG.equals(h.getTrangThai())) {
                for (HoaDonChiTiet item : items) {
                    SanPhamChiTiet spct = item.getSanPhamChiTiet();
                    spct.setSoLuong(spct.getSoLuong() + item.getSoLuong());
                    sanPhamChiTietRepository.save(spct);
                }
            }
            h.setTrangThai(DA_HUY);
        }

        // Chuẩn theo CK_HoaDon_TTThanhToan: N'Đã hoàn tiền'
        h.setTrangThaiThanhToan("Đã hoàn tiền");
        appendLog(h, "Đã xác nhận hoàn tiền cho khách" + (ghiChu != null ? " (" + ghiChu + ")" : ""));
        hoaDonRepository.save(h);

        return toResponse(h, items);
    }

    // Nút "Mua lại ngay / Tạo đơn đền bù" khi shipper làm mất hàng - Miễn phí vận chuyển (Video [30:00 - 30:30])
    @Transactional
    public HoaDonResponse recreateCompensateOrder(Integer originalOrderId) {
        HoaDon goc = hoaDonRepository.findDetailById(originalOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn gốc ID: " + originalOrderId));
        List<HoaDonChiTiet> itemsGoc = hoaDonChiTietRepository.findByOrderIds(List.of(originalOrderId));

        if (itemsGoc.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn gốc không có sản phẩm để tạo lại!");
        }

        HoaDon moi = new HoaDon();
        moi.setKhachHang(goc.getKhachHang());
        moi.setNhanVien(goc.getNhanVien());
        moi.setGiamGia(goc.getGiamGia());
        moi.setMaHoaDon(generateMaHoaDon());
        moi.setTenNguoiNhan(goc.getTenNguoiNhan());
        moi.setSdtNguoiNhan(goc.getSdtNguoiNhan());
        moi.setDiaChiGiaoHang(goc.getDiaChiGiaoHang());
        moi.setPhuongThucThanhToan(goc.getPhuongThucThanhToan());
        moi.setPhuongThucThanhToanOnline(goc.getPhuongThucThanhToanOnline());
        moi.setTrangThaiThanhToan(goc.getTrangThaiThanhToan());
        moi.setHinhThucMuaHang(goc.getHinhThucMuaHang());
        moi.setTrangThai(CHO_XAC_NHAN);

        // Miễn phí vận chuyển đền bù cho khách hàng
        BigDecimal tienTruocGiam = goc.getTienTruocGiam();
        BigDecimal tienGiam = goc.getTienGiamGia();
        BigDecimal phiShipDenBu = BigDecimal.ZERO;
        BigDecimal tongTien = tienTruocGiam.subtract(tienGiam).add(phiShipDenBu);

        moi.setTienTruocGiam(tienTruocGiam);
        moi.setTienGiamGia(tienGiam);
        moi.setPhiGiaoHang(phiShipDenBu);
        moi.setTongTien(tongTien);
        moi.setGhiChu("Đơn đền bù miễn phí ship tạo lại từ đơn " + goc.getMaHoaDon());

        HoaDon savedOrder = hoaDonRepository.save(moi);

        for (HoaDonChiTiet itemGoc : itemsGoc) {
            HoaDonChiTiet ctMoi = new HoaDonChiTiet();
            ctMoi.setHoaDon(savedOrder);
            ctMoi.setSanPhamChiTiet(itemGoc.getSanPhamChiTiet());
            ctMoi.setSoLuong(itemGoc.getSoLuong());
            ctMoi.setDonGia(itemGoc.getDonGia());
            hoaDonChiTietRepository.save(ctMoi);
        }

        appendLog(goc, "Đã tạo đơn đền bù mới: " + savedOrder.getMaHoaDon());
        hoaDonRepository.save(goc);

        return getDetail(savedOrder.getId());
    }

    private String generateMaHoaDon() {
        long count = hoaDonRepository.count() + 1;
        String candidate;
        do {
            candidate = String.format("HD%02d", count++);
        } while (hoaDonRepository.existsByMaHoaDon(candidate));
        return candidate;
    }

    // Ghi lịch sử ngắn gọn vào cột ghi_chu (tối đa 255 ký tự trong DB)
    private void appendLog(HoaDon h, String action) {
        String timeStr = LocalDateTime.now().format(LOG_TIME_FMT);
        String entry = "[" + timeStr + "] " + action;
        String current = h.getGhiChu() != null ? h.getGhiChu() : "";
        String combined = current.isEmpty() ? entry : current + " | " + entry;
        if (combined.length() > 250) {
            combined = combined.substring(combined.length() - 250);
        }
        h.setGhiChu(combined);
    }

    private HoaDonResponse toResponse(HoaDon h, List<HoaDonChiTiet> items) {
        KhachHang kh = h.getKhachHang();
        NhanVien nv = h.getNhanVien();
        GiamGia gg = h.getGiamGia();

        List<HoaDonChiTietResponse> itemDtos = items.stream().map(c -> {
            SanPhamChiTiet v = c.getSanPhamChiTiet();
            return new HoaDonChiTietResponse(
                    c.getId(),
                    v.getId(),
                    v.getMaAoChiTiet(),
                    v.getSanPham().getTenAo(),
                    v.getMauSac().getTenMau(),
                    v.getMauSac().getMaHex(),
                    v.getKichThuoc().getTenKichThuoc(),
                    v.getChatLieu().getTenChatLieu(),
                    v.getAnhUrl(),
                    c.getSoLuong(),
                    v.getSoLuong(),
                    c.getDonGia(),
                    c.getDonGia().multiply(BigDecimal.valueOf(c.getSoLuong()))
            );
        }).toList();

        return new HoaDonResponse(
                h.getId(),
                h.getMaHoaDon(),
                kh != null ? kh.getId() : null,
                kh != null ? kh.getMaKhachHang() : null,
                kh != null ? kh.getHoVaTen() : "Khách lẻ",
                kh != null ? kh.getEmail() : null,
                nv != null ? nv.getId() : null,
                nv != null ? nv.getMaNhanVien() : null,
                nv != null ? nv.getHoVaTen() : null,
                gg != null ? gg.getId() : null,
                gg != null ? gg.getMaGiamGia() : null,
                gg != null ? gg.getTenGiamGia() : null,
                gg != null ? gg.getLoaiGiamGia() : null,
                gg != null ? gg.getGiaTriGiam() : null,
                h.getTenNguoiNhan(),
                h.getSdtNguoiNhan(),
                h.getDiaChiGiaoHang(),
                h.getPhuongThucThanhToan(),
                h.getPhuongThucThanhToanOnline(),
                h.getTrangThaiThanhToan(),
                h.getMaGiaoDich(),
                h.getTienTruocGiam(),
                h.getTienGiamGia(),
                h.getPhiGiaoHang(),
                h.getTongTien(),
                h.getHinhThucMuaHang(),
                h.getGhiChu(),
                h.getTrangThai(),
                h.getNgayTao(),
                itemDtos
        );
    }
}
