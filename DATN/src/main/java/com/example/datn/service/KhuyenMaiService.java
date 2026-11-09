package com.example.datn.service;
import com.example.datn.dto.BienTheKhuyenMaiResponse;
import com.example.datn.dto.KhuyenMaiRequest;
import com.example.datn.dto.KhuyenMaiResponse;
import com.example.datn.entity.KhuyenMai;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.KhuyenMaiRepository;
import com.example.datn.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

    private final KhuyenMaiRepository khuyenMaiRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Transactional(readOnly = true)
    public List<KhuyenMaiResponse> getAllAndFilter(String keyword, String trangThaiLoc,
                                                   LocalDate tuNgay, LocalDate denNgay) {
        String cleanKey = keyword != null ? keyword.trim() : null;
        List<KhuyenMai> list = khuyenMaiRepository.searchDotGiamGia(cleanKey, tuNgay, denNgay);
        List<SanPhamChiTiet> allVariants = sanPhamChiTietRepository.findAllForAdmin();

        return list.stream()
                .map(k -> toResponse(k, allVariants))
                .filter(res -> trangThaiLoc == null || trangThaiLoc.isBlank() || res.trangThaiHienThi().equals(trangThaiLoc))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BienTheKhuyenMaiResponse> getAllVariantsForSelection() {
        return sanPhamChiTietRepository.findAllForAdmin().stream()
                .map(v -> new BienTheKhuyenMaiResponse(
                        v.getId(),
                        v.getSanPham().getId(),
                        v.getSanPham().getMaAo(),
                        v.getSanPham().getTenAo(),
                        v.getMaAoChiTiet(),
                        v.getMauSac().getTenMau(),
                        v.getKichThuoc().getTenKichThuoc(),
                        v.getChatLieu() != null ? v.getChatLieu().getTenChatLieu() : "",
                        v.getGiaBanBanDau(),
                        v.getGiaBan(),
                        v.getSoLuong(),
                        v.getKhuyenMai() != null ? v.getKhuyenMai().getMaKhuyenMai() : null
                ))
                .toList();
    }

    @Transactional
    public KhuyenMaiResponse create(KhuyenMaiRequest req) {
        validateRequest(req);
        KhuyenMai entity = new KhuyenMai();

        String ma = (req.maKhuyenMai() == null || req.maKhuyenMai().isBlank())
                ? generateMaKhuyenMai()
                : req.maKhuyenMai().trim().toUpperCase();

        if (khuyenMaiRepository.existsByMaKhuyenMaiIgnoreCase(ma)) {
            throw new IllegalArgumentException("Mã đợt giảm giá '" + ma + "' đã tồn tại!");
        }

        entity.setMaKhuyenMai(ma);
        entity.setSoTienGiam(BigDecimal.ZERO);
        entity.setTrangThai(req.trangThai() != null ? req.trangThai() : true);
        mapRequestToEntity(req, entity);

        KhuyenMai saved = khuyenMaiRepository.save(entity);
        applyPromotionToVariants(saved, req.idSanPhamChiTietList());

        return toResponse(saved, sanPhamChiTietRepository.findAllForAdmin());
    }

    @Transactional
    public KhuyenMaiResponse update(Integer id, KhuyenMaiRequest req) {
        validateRequest(req);
        KhuyenMai entity = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt giảm giá ID: " + id));

        mapRequestToEntity(req, entity);
        if (req.trangThai() != null) {
            entity.setTrangThai(req.trangThai());
        }

        KhuyenMai saved = khuyenMaiRepository.save(entity);
        applyPromotionToVariants(saved, req.idSanPhamChiTietList());

        return toResponse(saved, sanPhamChiTietRepository.findAllForAdmin());
    }

    @Transactional
    public KhuyenMaiResponse toggleStatus(Integer id) {
        KhuyenMai entity = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đợt giảm giá"));
        boolean newStatus = !Boolean.TRUE.equals(entity.getTrangThai());
        entity.setTrangThai(newStatus);
        KhuyenMai saved = khuyenMaiRepository.save(entity);

        List<SanPhamChiTiet> allVariants = sanPhamChiTietRepository.findAllForAdmin();
        for (SanPhamChiTiet v : allVariants) {
            if (v.getKhuyenMai() != null && v.getKhuyenMai().getId().equals(id)) {
                recalculateVariantPrice(v, saved);
                sanPhamChiTietRepository.save(v);
            }
        }
        return toResponse(saved, allVariants);
    }

    private void applyPromotionToVariants(KhuyenMai km, List<Integer> selectedVariantIds) {
        Set<Integer> selectedSet = selectedVariantIds != null ? new HashSet<>(selectedVariantIds) : Set.of();
        List<SanPhamChiTiet> allVariants = sanPhamChiTietRepository.findAllForAdmin();

        for (SanPhamChiTiet v : allVariants) {
            boolean currentlyInThisPromo = v.getKhuyenMai() != null && v.getKhuyenMai().getId().equals(km.getId());
            boolean shouldBeInThisPromo = selectedSet.contains(v.getId());

            if (shouldBeInThisPromo) {
                v.setKhuyenMai(km);
                recalculateVariantPrice(v, km);
                sanPhamChiTietRepository.save(v);
            } else if (currentlyInThisPromo) {
                v.setKhuyenMai(null);
                v.setGiaBan(v.getGiaBanBanDau());
                sanPhamChiTietRepository.save(v);
            }
        }
    }

    private void recalculateVariantPrice(SanPhamChiTiet v, KhuyenMai km) {
        LocalDate today = LocalDate.now();
        boolean isActive = Boolean.TRUE.equals(km.getTrangThai()) && !today.isAfter(km.getNgayKetThuc());

        if (isActive && km.getPhanTramGiam() != null && km.getPhanTramGiam().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = v.getGiaBanBanDau()
                    .multiply(km.getPhanTramGiam())
                    .divide(ONE_HUNDRED, 0, RoundingMode.HALF_UP);
            BigDecimal newPrice = v.getGiaBanBanDau().subtract(discount).max(BigDecimal.ZERO);
            v.setGiaBan(newPrice);
        } else {
            v.setGiaBan(v.getGiaBanBanDau());
        }
    }

    private void mapRequestToEntity(KhuyenMaiRequest req, KhuyenMai entity) {
        entity.setTenKhuyenMai(req.tenKhuyenMai().trim());
        entity.setPhanTramGiam(req.phanTramGiam());
        entity.setSoTienGiam(BigDecimal.ZERO);
        entity.setNgayBatDau(req.ngayBatDau());
        entity.setNgayKetThuc(req.ngayKetThuc());
        entity.setMoTa(req.moTa() != null ? req.moTa().trim() : null);
    }

    private void validateRequest(KhuyenMaiRequest req) {
        if (req.tenKhuyenMai() == null || req.tenKhuyenMai().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đợt giảm giá không được để trống!");
        }
        if (req.phanTramGiam() == null
                || req.phanTramGiam().compareTo(BigDecimal.ONE) < 0
                || req.phanTramGiam().compareTo(ONE_HUNDRED) > 0) {
            throw new IllegalArgumentException("Đợt giảm giá chỉ giảm theo phần trăm từ 1% đến 100%!");
        }
        if (req.ngayBatDau() == null || req.ngayKetThuc() == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc!");
        }
        if (req.ngayKetThuc().isBefore(req.ngayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        }
    }

    private String generateMaKhuyenMai() {
        long count = khuyenMaiRepository.count() + 1;
        String candidate;
        do {
            candidate = String.format("KM%02d", count++);
        } while (khuyenMaiRepository.existsByMaKhuyenMaiIgnoreCase(candidate));
        return candidate;
    }

    private KhuyenMaiResponse toResponse(KhuyenMai k, List<SanPhamChiTiet> allVariants) {
        LocalDate today = LocalDate.now();
        String status;
        if (!Boolean.TRUE.equals(k.getTrangThai())) {
            status = "NGUNG_HOAT_DONG";
        } else if (today.isBefore(k.getNgayBatDau())) {
            status = "SAP_DIEN_RA";
        } else if (today.isAfter(k.getNgayKetThuc())) {
            status = "DA_KET_THUC";
        } else {
            status = "DANG_DIEN_RA";
        }

        List<Integer> appliedIds = allVariants.stream()
                .filter(v -> v.getKhuyenMai() != null && v.getKhuyenMai().getId().equals(k.getId()))
                .map(SanPhamChiTiet::getId)
                .toList();

        return new KhuyenMaiResponse(
                k.getId(),
                k.getMaKhuyenMai(),
                k.getTenKhuyenMai(),
                k.getPhanTramGiam(),
                k.getSoTienGiam(),
                k.getNgayBatDau(),
                k.getNgayKetThuc(),
                k.getMoTa(),
                k.getTrangThai(),
                status,
                appliedIds.size(),
                appliedIds
        );
    }
}