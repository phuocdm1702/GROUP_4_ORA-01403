package com.example.datn.service;

import com.example.datn.dto.StoreProductResponse;
import com.example.datn.entity.Anh;
import com.example.datn.entity.ChatLieu;
import com.example.datn.entity.SanPham;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.AnhRepository;
import com.example.datn.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreProductService {
    private final SanPhamChiTietRepository repo;
    private final AnhRepository anhRepo;

    @Transactional(readOnly = true)
    public List<StoreProductResponse> getAll() {
        Map<Integer, List<Anh>> anh = anhRepo.findAllActive().stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));
        return group(repo.findAllActive(), anh);
    }

    @Transactional(readOnly = true)
    public Optional<StoreProductResponse> getOne(Integer id) {
        Map<Integer, List<Anh>> anh = Map.of(id, anhRepo.findByProduct(id));
        return group(repo.findActiveByProduct(id), anh).stream().findFirst();
    }

    private List<StoreProductResponse> group(List<SanPhamChiTiet> rows, Map<Integer, List<Anh>> anhBy) {
        Map<Integer, List<SanPhamChiTiet>> byProduct = new LinkedHashMap<>();
        for (SanPhamChiTiet v : rows) {
            byProduct.computeIfAbsent(v.getSanPham().getId(), k -> new ArrayList<>()).add(v);
        }
        return byProduct.entrySet().stream()
                .map(e -> toResponse(e.getValue(), anhBy.getOrDefault(e.getKey(), List.of())))
                .toList();
    }

     StoreProductResponse toResponse(List<SanPhamChiTiet> vs, List<Anh> images) {
        SanPham sp = vs.get(0).getSanPham();
        ChatLieu cl = vs.get(0).getChatLieu();

        List<StoreProductResponse.Variant> bienThe = vs.stream().map(v -> new StoreProductResponse.Variant(
                v.getId(),
                new StoreProductResponse.Mau(
                        v.getMauSac().getMaMau(),
                        v.getMauSac().getTenMau(),
                        v.getMauSac().getMaHex() != null ? v.getMauSac().getMaHex() : "#cccccc"),
                v.getKichThuoc().getTenKichThuoc(),
                v.getGiaBanBanDau() != null ? v.getGiaBanBanDau() : v.getGiaBan(),
                v.getGiaBan(),
                v.getSoLuong() != null ? v.getSoLuong() : 0,
                v.getAnhUrl()
        )).toList();

        // images đã sắp theo thu_tu từ câu truy vấn
        List<StoreProductResponse.Hinh> hinhAnh = images.stream()
                .filter(a -> a.getAnhUrl() != null && !a.getAnhUrl().isBlank())
                .map(a -> new StoreProductResponse.Hinh(
                        a.getMauSac() != null ? a.getMauSac().getMaMau() : null, a.getAnhUrl()))
                .toList();

        return new StoreProductResponse(
                sp.getId(),
                sp.getTenAo(),
                sp.getMoTa(),
                cl != null ? cl.getTenChatLieu() : "Đang cập nhật",
                sp.getThuongHieu() != null ? sp.getThuongHieu().getTenThuongHieu() : null,
                bienThe,
                hinhAnh);
    }
}