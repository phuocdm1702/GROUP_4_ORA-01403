package com.example.datn.service;

import com.example.datn.dto.*;
import com.example.datn.entity.*;
import com.example.datn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonHangService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String COD = "Thanh toán khi nhận hàng";

    private final SanPhamChiTietRepository ctRepo;
    private final GiamGiaRepository ggRepo;
    private final HoaDonRepository hdRepo;
    private final HoaDonChiTietRepository hdctRepo;
    private final KhachHangRepository khRepo;
    private final DiaChiRepository diaChiRepo;
    private final GioHangChiTietRepository ghRepo;

    @Value("${app.shipping.fee:30000}")
    private BigDecimal shippingFee;
    @Value("${app.shipping.free-from:500000}")
    private BigDecimal freeFrom;

    private record Dong(SanPhamChiTiet v, int soLuong) {}
    private record Tinh(List<Dong> dong, BigDecimal truoc, BigDecimal giam, BigDecimal ship,
                        BigDecimal tong, GiamGia giamGia) {}

    @Transactional(readOnly = true)
    public BaoGiaResponse baoGia(BaoGiaRequest r) {
        Tinh t = tinh(r.items(), r.maGiamGia());
        return new BaoGiaResponse(t.truoc(), t.giam(), t.ship(), t.tong(),
                t.giamGia() == null ? null : t.giamGia().getMaGiamGia());
    }

    @Transactional
    public DatHangResponse dat(Integer idKhach, DatHangRequest r) {
        KhachHang kh = khRepo.findById(idKhach)
                .filter(k -> Boolean.TRUE.equals(k.getTrangThai()))
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        Tinh t = tinh(r.items(), r.maGiamGia());     // kiểm tra tồn kho, mã giảm giá và tính tiền ở server

        // Trừ tồn kho theo thứ tự mã tăng dần (tránh khóa chéo giữa các đơn). Không đủ hàng thì cả đơn bị hủy.
        for (Dong d : t.dong()) {
            if (ctRepo.tryDeduct(d.v().getId(), d.soLuong()) == 0) {
                throw new RuntimeException(ten(d.v()) + " vừa hết hàng, vui lòng kiểm tra lại giỏ hàng");
            }
        }
        if (t.giamGia() != null && ggRepo.tryUse(t.giamGia().getId()) == 0) {
            throw new RuntimeException("Mã giảm giá vừa hết lượt sử dụng");
        }

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(newCode());
        hd.setKhachHang(kh);
        hd.setGiamGia(t.giamGia());
        hd.setTenNguoiNhan(r.tenNguoiNhan().trim());
        hd.setSdtNguoiNhan(r.sdtNguoiNhan().replaceAll("\\s+", ""));
        hd.setDiaChiGiaoHang(String.join(", ", r.duong().trim(), r.xaPhuong().trim(),
                r.quanHuyen().trim(), r.tinhThanhPho().trim()));
        hd.setPhuongThucThanhToan(COD);
        hd.setTrangThaiThanhToan("Chưa thanh toán");
        hd.setTienTruocGiam(t.truoc());
        hd.setTienGiamGia(t.giam());
        hd.setPhiGiaoHang(t.ship());
        hd.setTongTien(t.tong());
        hd.setHinhThucMuaHang("Online");
        hd.setGhiChu(r.ghiChu() == null || r.ghiChu().isBlank() ? null : r.ghiChu().trim());
        hd.setTrangThai("Chờ xác nhận");
        hdRepo.save(hd);

        List<HoaDonChiTiet> chiTiet = new ArrayList<>();
        for (Dong d : t.dong()) {
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hd);
            ct.setSanPhamChiTiet(d.v());
            ct.setSoLuong(d.soLuong());
            ct.setDonGia(d.v().getGiaBan());          // giá tại thời điểm mua
            chiTiet.add(ct);
        }
        hdctRepo.saveAll(chiTiet);

        if (Boolean.TRUE.equals(r.luuDiaChi())) saveAddress(idKhach, r);
        ghRepo.deleteByKhachHang(idKhach);            // đặt xong thì xóa giỏ hàng trên server

        return new DatHangResponse(hd.getMaHoaDon(), t.tong(), t.giam(), t.ship());
    }

    // ---------------------------------------------------------------

    private Tinh tinh(List<CartItemRequest> items, String maGiamGia) {
        if (items == null || items.isEmpty()) throw new RuntimeException("Giỏ hàng trống");

        Map<Integer, Integer> qty = new TreeMap<>();        // TreeMap = đã sắp theo mã
        for (CartItemRequest i : items) qty.merge(i.idBienThe(), i.soLuong(), Integer::sum);

        Map<Integer, SanPhamChiTiet> byId = ctRepo.findAllByIdWithDetails(qty.keySet()).stream()
                .collect(Collectors.toMap(SanPhamChiTiet::getId, v -> v));

        List<Dong> dong = new ArrayList<>();
        BigDecimal truoc = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> e : qty.entrySet()) {
            SanPhamChiTiet v = byId.get(e.getKey());
            if (v == null || !Boolean.TRUE.equals(v.getTrangThai()) || !Boolean.TRUE.equals(v.getSanPham().getTrangThai())) {
                throw new RuntimeException("Có sản phẩm trong giỏ hàng không còn được bán, vui lòng xóa khỏi giỏ");
            }
            if (e.getValue() > 99) throw new RuntimeException("Mỗi sản phẩm tối đa 99 cái");
            int ton = v.getSoLuong() == null ? 0 : v.getSoLuong();
            if (e.getValue() > ton) {
                throw new RuntimeException(ten(v) + (ton == 0 ? " đã hết hàng" : " chỉ còn " + ton + " sản phẩm"));
            }
            dong.add(new Dong(v, e.getValue()));
            truoc = truoc.add(v.getGiaBan().multiply(BigDecimal.valueOf(e.getValue())));
        }

        GiamGia gg = null;
        BigDecimal giam = BigDecimal.ZERO;
        if (maGiamGia != null && !maGiamGia.isBlank()) {
            gg = ggRepo.findByMaGiamGiaIgnoreCase(maGiamGia.trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));
            kiemTraMa(gg, truoc);
            giam = tinhGiam(gg, truoc);
        }

        BigDecimal ship = truoc.compareTo(freeFrom) >= 0 ? BigDecimal.ZERO : shippingFee;
        BigDecimal tong = truoc.subtract(giam).add(ship);
        return new Tinh(dong, s2(truoc), s2(giam), s2(ship), s2(tong), gg);
    }

    private void kiemTraMa(GiamGia g, BigDecimal truoc) {
        LocalDate today = LocalDate.now();
        if (!Boolean.TRUE.equals(g.getTrangThai())) throw new RuntimeException("Mã giảm giá không hợp lệ");
        if (today.isBefore(g.getNgayBatDau())) throw new RuntimeException("Mã giảm giá chưa đến thời gian áp dụng");
        if (today.isAfter(g.getNgayKetThuc())) throw new RuntimeException("Mã giảm giá đã hết hạn");
        if (g.getSoLuongDaDung() >= g.getSoLuong()) throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        if (truoc.compareTo(g.getGiaTriToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng cần tối thiểu "
                    + String.format("%,.0f", g.getGiaTriToiThieu()).replace(',', '.') + " ₫ để dùng mã này");
        }
    }

    private BigDecimal tinhGiam(GiamGia g, BigDecimal truoc) {
        BigDecimal giam = "PHAN_TRAM".equals(g.getLoaiGiamGia())
                ? truoc.multiply(g.getGiaTriGiam()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                : g.getGiaTriGiam();
        if (g.getToiDaGiamGia() != null && giam.compareTo(g.getToiDaGiamGia()) > 0) giam = g.getToiDaGiamGia();
        if (giam.compareTo(truoc) > 0) giam = truoc;
        return giam;
    }

    private void saveAddress(Integer idKhach, DatHangRequest r) {
        List<DiaChi> saved = diaChiRepo.findByIdKhachHangOrderByMacDinhDescIdAsc(idKhach);
        boolean dup = saved.stream().anyMatch(d ->
                eq(d.getTinhThanhPho(), r.tinhThanhPho()) && eq(d.getQuanHuyen(), r.quanHuyen())
                        && eq(d.getXaPhuong(), r.xaPhuong()) && eq(d.getDuong(), r.duong()));
        if (dup) return;
        DiaChi d = new DiaChi();
        d.setIdKhachHang(idKhach);
        d.setTinhThanhPho(r.tinhThanhPho().trim());
        d.setQuanHuyen(r.quanHuyen().trim());
        d.setXaPhuong(r.xaPhuong().trim());
        d.setDuong(r.duong().trim());
        d.setMacDinh(saved.isEmpty());               // địa chỉ đầu tiên làm mặc định
        diaChiRepo.save(d);
    }

    private boolean eq(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private String newCode() {
        String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        for (int i = 0; i < 5; i++) {
            String code = "HD" + day + String.format("%06d", RANDOM.nextInt(1_000_000));
            if (!hdRepo.existsByMaHoaDon(code)) return code;
        }
        throw new RuntimeException("Không tạo được mã đơn hàng, vui lòng thử lại");
    }

    private String ten(SanPhamChiTiet v) {
        return v.getSanPham().getTenAo() + " (" + v.getMauSac().getTenMau() + ", "
                + v.getKichThuoc().getTenKichThuoc() + ")";
    }

    private BigDecimal s2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
