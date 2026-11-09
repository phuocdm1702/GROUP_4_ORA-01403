package com.example.datn.service;
import com.example.datn.dto.GiamGiaRequest;
import com.example.datn.dto.GiamGiaResponse;
import com.example.datn.entity.GiamGia;
import com.example.datn.repository.GiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiamGiaService {
    private final GiamGiaRepository giamGiaRepository;
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public List<GiamGiaResponse> getAllAndFilter(String keyword, String loaiGiamGia, String trangThaiLoc,
                                                 LocalDate tuNgay, LocalDate denNgay) {
        String cleanKeyword = keyword != null ? keyword.trim() : null;
        List<GiamGia> list = giamGiaRepository.searchPhieuGiamGia(cleanKeyword, loaiGiamGia, tuNgay, denNgay);

        return list.stream()
                .map(g -> toResponse(g, null))
                .filter(res -> trangThaiLoc == null || trangThaiLoc.isBlank() || res.trangThaiHienThi().equals(trangThaiLoc))
                .toList();
    }

    @Transactional
    public GiamGiaResponse create(GiamGiaRequest req) {
        validateRequest(req);
        GiamGia entity = new GiamGia();

        String ma = (req.maGiamGia() == null || req.maGiamGia().isBlank())
                ? generateMaGiamGia()
                : req.maGiamGia().trim().toUpperCase();

        if (giamGiaRepository.existsByMaGiamGiaIgnoreCase(ma)) {
            throw new IllegalArgumentException("Mã giảm giá '" + ma + "' đã tồn tại!");
        }

        entity.setMaGiamGia(ma);
        entity.setSoLuongDaDung(0);
        entity.setTrangThai(req.trangThai() != null ? req.trangThai() : true);
        mapRequestToEntity(req, entity);

        return toResponse(giamGiaRepository.save(entity), null);
    }

    @Transactional
    public GiamGiaResponse update(Integer id, GiamGiaRequest req) {
        validateRequest(req);
        GiamGia entity = giamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá ID: " + id));

        if (req.soLuong() < entity.getSoLuongDaDung()) {
            throw new IllegalArgumentException("Số lượng mới không được nhỏ hơn số lượng đã sử dụng (" + entity.getSoLuongDaDung() + ")!");
        }

        mapRequestToEntity(req, entity);
        if (req.trangThai() != null) {
            entity.setTrangThai(req.trangThai());
        }
        return toResponse(giamGiaRepository.save(entity), null);
    }

    @Transactional
    public GiamGiaResponse toggleStatus(Integer id) {
        GiamGia entity = giamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));
        entity.setTrangThai(!Boolean.TRUE.equals(entity.getTrangThai()));
        return toResponse(giamGiaRepository.save(entity), null);
    }

    public List<GiamGiaResponse> getBestVouchersForOrder(BigDecimal tienTruocGiam) {
        if (tienTruocGiam == null || tienTruocGiam.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        List<GiamGia> available = giamGiaRepository.findAvailableVouchers(tienTruocGiam, LocalDate.now());
        return available.stream()
                .map(g -> toResponse(g, calculateDiscountAmount(g, tienTruocGiam)))
                .sorted(Comparator.comparing(GiamGiaResponse::soTienGiamDuKien).reversed())
                .toList();
    }

    public BigDecimal calculateDiscountAmount(GiamGia g, BigDecimal tienTruocGiam) {
        if (g == null || tienTruocGiam == null || tienTruocGiam.compareTo(g.getGiaTriToiThieu()) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("PHAN_TRAM".equalsIgnoreCase(g.getLoaiGiamGia())) {
            discount = tienTruocGiam.multiply(g.getGiaTriGiam())
                    .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
            if (g.getToiDaGiamGia() != null && discount.compareTo(g.getToiDaGiamGia()) > 0) {
                discount = g.getToiDaGiamGia();
            }
        } else {
            discount = g.getGiaTriGiam();
        }
        return discount.min(tienTruocGiam);
    }

    private void mapRequestToEntity(GiamGiaRequest req, GiamGia entity) {
        entity.setTenGiamGia(req.tenGiamGia().trim());
        entity.setLoaiGiamGia(req.loaiGiamGia());
        entity.setGiaTriGiam(req.giaTriGiam());
        entity.setToiDaGiamGia("SO_TIEN".equals(req.loaiGiamGia()) && req.toiDaGiamGia() == null
                ? req.giaTriGiam() : req.toiDaGiamGia());
        entity.setGiaTriToiThieu(req.giaTriToiThieu());
        entity.setSoLuong(req.soLuong());
        entity.setNgayBatDau(req.ngayBatDau());
        entity.setNgayKetThuc(req.ngayKetThuc());
        entity.setMoTa(req.moTa() != null ? req.moTa().trim() : null);
    }

    private void validateRequest(GiamGiaRequest req) {
        if (req.tenGiamGia() == null || req.tenGiamGia().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phiếu giảm giá không được để trống!");
        }
        if (!"SO_TIEN".equals(req.loaiGiamGia()) && !"PHAN_TRAM".equals(req.loaiGiamGia())) {
            throw new IllegalArgumentException("Loại giảm giá chỉ chấp nhận SO_TIEN hoặc PHAN_TRAM!");
        }
        if (req.soLuong() == null || req.soLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phát hành phải lớn hơn 0!");
        }
        if (req.giaTriToiThieu() == null || req.giaTriToiThieu().compareTo(MIN_AMOUNT) < 0) {
            throw new IllegalArgumentException("Đơn hàng tối thiểu phải từ 1.000đ trở lên!");
        }
        if ("PHAN_TRAM".equals(req.loaiGiamGia())) {
            if (req.giaTriGiam() == null || req.giaTriGiam().compareTo(BigDecimal.ONE) < 0
                    || req.giaTriGiam().compareTo(ONE_HUNDRED) > 0) {
                throw new IllegalArgumentException("Mức giảm phần trăm phải từ 1% đến 100%!");
            }
            if (req.toiDaGiamGia() == null || req.toiDaGiamGia().compareTo(MIN_AMOUNT) < 0) {
                throw new IllegalArgumentException("Mức giảm tối đa phải từ 1.000đ trở lên!");
            }
        } else {
            if (req.giaTriGiam() == null || req.giaTriGiam().compareTo(MIN_AMOUNT) < 0) {
                throw new IllegalArgumentException("Số tiền giảm phải từ 1.000đ trở lên!");
            }
        }
        if (req.ngayBatDau() == null || req.ngayKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");
        }
        if (req.ngayKetThuc().isBefore(req.ngayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }
    }

    private String generateMaGiamGia() {
        long count = giamGiaRepository.count() + 1;
        String candidate;
        do {
            candidate = String.format("GG%02d", count++);
        } while (giamGiaRepository.existsByMaGiamGiaIgnoreCase(candidate));
        return candidate;
    }

    public GiamGiaResponse toResponse(GiamGia g, BigDecimal soTienGiamDuKien) {
        LocalDate today = LocalDate.now();
        String status;
        if (!Boolean.TRUE.equals(g.getTrangThai())) {
            status = "NGUNG_HOAT_DONG";
        } else if (today.isBefore(g.getNgayBatDau())) {
            status = "SAP_DIEN_RA";
        } else if (today.isAfter(g.getNgayKetThuc()) || g.getSoLuongDaDung() >= g.getSoLuong()) {
            status = "DA_KET_THUC";
        } else {
            status = "DANG_HOAT_DONG";
        }

        return new GiamGiaResponse(
                g.getId(),
                g.getMaGiamGia(),
                g.getTenGiamGia(),
                g.getLoaiGiamGia(),
                g.getGiaTriGiam(),
                g.getToiDaGiamGia(),
                g.getGiaTriToiThieu(),
                g.getSoLuong(),
                g.getSoLuongDaDung(),
                g.getNgayBatDau(),
                g.getNgayKetThuc(),
                g.getMoTa(),
                g.getTrangThai(),
                status,
                soTienGiamDuKien
        );
    }
}
