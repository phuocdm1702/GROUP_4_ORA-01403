package com.example.datn.service;

import com.example.datn.dto.DonHangChiTietResponse;
import com.example.datn.dto.DonHangTomTat;
import com.example.datn.dto.PageResponse;
import com.example.datn.entity.Anh;
import com.example.datn.entity.HoaDon;
import com.example.datn.entity.HoaDonChiTiet;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonHangKhachService {
    private static final String CHO_XAC_NHAN = "Chờ xác nhận";
    private static final String DA_HUY = "Đã hủy";
    private static final String DA_THANH_TOAN = "Đã thanh toán";
    private static final String DA_HOAN_TIEN = "Đã hoàn tiền";
    private static final String GHI_CHU_HUY = "Khách hủy đơn";

    private final HoaDonRepository hdRepo;
    private final HoaDonChiTietRepository ctRepo;
    private final SanPhamChiTietRepository spctRepo;
    private final GiamGiaRepository ggRepo;
    private final AnhRepository anhRepo;

    // PageResponse.tongSanPham ở đây là tổng số đơn hàng
    @Transactional(readOnly = true)
    public PageResponse<DonHangTomTat> danhSach(Integer idKh, String trangThai, int trang, int kichThuoc) {
        int size = Math.min(Math.max(kichThuoc, 1), 20);
        int page = Math.max(trang, 1);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<HoaDon> result = (trangThai == null || trangThai.isBlank())
                ? hdRepo.findByKhachHang_Id(idKh, pageable)
                : hdRepo.findByKhachHang_IdAndTrangThai(idKh, trangThai, pageable);

        List<Integer> ids = result.getContent().stream().map(HoaDon::getId).toList();
        Map<Integer, List<HoaDonChiTiet>> lines = ids.isEmpty() ? Map.of()
                : ctRepo.findByOrderIds(ids).stream().collect(Collectors.groupingBy(c -> c.getHoaDon().getId()));
        Map<Integer, List<Anh>> images = anhOf(lines.values().stream().flatMap(List::stream).toList());

        List<DonHangTomTat> items = result.getContent().stream()
                .map(h -> tomTat(h, lines.getOrDefault(h.getId(), List.of()), images)).toList();
        return new PageResponse<>(items, page, size, result.getTotalElements(), Math.max(1, result.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public DonHangChiTietResponse chiTiet(Integer idKh, String ma) {
        HoaDon h = timDon(idKh, ma);
        List<HoaDonChiTiet> lines = ctRepo.findByOrderIds(List.of(h.getId()));
        Map<Integer, List<Anh>> images = anhOf(lines);

        List<DonHangChiTietResponse.Dong> dong = lines.stream().map(c -> {
            SanPhamChiTiet v = c.getSanPhamChiTiet();
            BigDecimal gia = c.getDonGia() == null ? BigDecimal.ZERO : c.getDonGia();
            return new DonHangChiTietResponse.Dong(
                    v.getSanPham().getId(), v.getSanPham().getTenAo(),
                    v.getMauSac().getMaMau(), v.getMauSac().getTenMau(),
                    v.getMauSac().getMaHex() != null ? v.getMauSac().getMaHex() : "#cccccc",
                    v.getKichThuoc().getTenKichThuoc(),
                    c.getSoLuong(), gia, gia.multiply(BigDecimal.valueOf(c.getSoLuong())),
                    pickImage(images.getOrDefault(v.getSanPham().getId(), List.of()), v.getMauSac().getId()));
        }).toList();

        return new DonHangChiTietResponse(
                h.getMaHoaDon(), h.getNgayTao(), h.getTrangThai(), h.getTrangThaiThanhToan(),
                h.getPhuongThucThanhToan(), h.getTenNguoiNhan(), h.getSdtNguoiNhan(), h.getDiaChiGiaoHang(),
                h.getGhiChu(), h.getTienTruocGiam(), h.getTienGiamGia(), h.getPhiGiaoHang(), h.getTongTien(),
                h.getGiamGia() == null ? null : h.getGiamGia().getMaGiamGia(),
                CHO_XAC_NHAN.equals(h.getTrangThai()), dong);
    }

    @Transactional
    public void huy(Integer idKh, String ma) {
        HoaDon h = timDon(idKh, ma);

        // Đọc hết dữ liệu cần dùng TRƯỚC khi chạy lệnh cập nhật (lệnh đó làm sạch bộ nhớ đệm của Hibernate)
        Integer idDon = h.getId();
        Integer idMa = h.getGiamGia() == null ? null : h.getGiamGia().getId();
        List<int[]> hoan = ctRepo.findByOrderIds(List.of(idDon)).stream()
                .map(c -> new int[]{c.getSanPhamChiTiet().getId(), c.getSoLuong()})
                .sorted(Comparator.comparingInt(a -> a[0]))        // thứ tự mã tăng dần, tránh khóa chéo
                .toList();
        String trangThaiHienTai = h.getTrangThai();

        // Chỉ hủy khi đơn VẪN đang "Chờ xác nhận". Nếu admin vừa xác nhận thì lệnh này trả về 0.
        if (hdRepo.doiTrangThai(idDon, CHO_XAC_NHAN, DA_HUY) == 0) {
            throw new RuntimeException("Đơn hàng đang ở trạng thái \"" + trangThaiHienTai
                    + "\" nên không thể hủy. Vui lòng liên hệ cửa hàng.");
        }

        for (int[] x : hoan) spctRepo.hoanKho(x[0], x[1]);
        if (idMa != null) ggRepo.hoanLuot(idMa);

        HoaDon fresh = hdRepo.findById(idDon).orElseThrow();
        fresh.setGhiChu(noiGhiChu(fresh.getGhiChu()));
        if (DA_THANH_TOAN.equals(fresh.getTrangThaiThanhToan())) fresh.setTrangThaiThanhToan(DA_HOAN_TIEN);
        hdRepo.save(fresh);
    }

    // ---------------------------------------------------------------

    private HoaDon timDon(Integer idKh, String ma) {
        return hdRepo.findByMaHoaDonAndKhachHang_Id(ma, idKh)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    private DonHangTomTat tomTat(HoaDon h, List<HoaDonChiTiet> lines, Map<Integer, List<Anh>> images) {
        int soSanPham = lines.stream().mapToInt(HoaDonChiTiet::getSoLuong).sum();
        String ten = null, hex = null, anh = null;
        if (!lines.isEmpty()) {
            SanPhamChiTiet v = lines.get(0).getSanPhamChiTiet();
            ten = v.getSanPham().getTenAo();
            hex = v.getMauSac().getMaHex() != null ? v.getMauSac().getMaHex() : "#cccccc";
            anh = pickImage(images.getOrDefault(v.getSanPham().getId(), List.of()), v.getMauSac().getId());
        }
        return new DonHangTomTat(h.getMaHoaDon(), h.getNgayTao(), h.getTrangThai(), h.getTrangThaiThanhToan(),
                h.getTongTien(), soSanPham, lines.size(), ten, hex, anh);
    }

    private Map<Integer, List<Anh>> anhOf(List<HoaDonChiTiet> lines) {
        Set<Integer> productIds = lines.stream()
                .map(c -> c.getSanPhamChiTiet().getSanPham().getId()).collect(Collectors.toSet());
        return productIds.isEmpty() ? Map.of()
                : anhRepo.findByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));
    }

    private String pickImage(List<Anh> list, Integer idMau) {
        return list.stream().filter(a -> a.getMauSac() != null && idMau.equals(a.getMauSac().getId()))
                .findFirst()
                .or(() -> list.stream().filter(a -> a.getMauSac() == null).findFirst())
                .map(Anh::getAnhUrl).orElse(null);
    }

    // Ghi chú tối đa 255 ký tự: cắt phần ghi chú cũ, giữ nguyên dòng "Khách hủy đơn"
    private String noiGhiChu(String cu) {
        if (cu == null || cu.isBlank()) return GHI_CHU_HUY;
        int keep = 255 - GHI_CHU_HUY.length() - 3;
        return cu.substring(0, Math.min(cu.length(), keep)) + " | " + GHI_CHU_HUY;
    }
}