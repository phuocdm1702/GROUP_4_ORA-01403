package com.example.datn.service;

import com.example.datn.dto.CartItemRequest;
import com.example.datn.dto.CartLineResponse;
import com.example.datn.dto.StoreProductResponse;
import com.example.datn.entity.Anh;
import com.example.datn.entity.GioHangChiTiet;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.AnhRepository;
import com.example.datn.repository.GioHangChiTietRepository;
import com.example.datn.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GioHangService {
    private static final int MAX_QTY = 99;

    private final SanPhamChiTietRepository ctRepo;
    private final AnhRepository anhRepo;
    private final GioHangChiTietRepository ghRepo;

    // Gộp các dòng trùng mã, bỏ dòng không hợp lệ, giới hạn số lượng
    static Map<Integer, Integer> gop(List<CartItemRequest> items) {
        Map<Integer, Integer> qty = new LinkedHashMap<>();
        if (items == null) return qty;
        if (items.size() > 100) throw new RuntimeException("Giỏ hàng quá lớn");
        for (CartItemRequest i : items) {
            if (i == null || i.idBienThe() == null || i.soLuong() == null || i.soLuong() < 1) continue;
            qty.merge(i.idBienThe(), i.soLuong(), Integer::sum);
        }
        qty.replaceAll((k, v) -> Math.min(MAX_QTY, v));
        return qty;
    }

    // Trả về thông tin hiện tại (giá, tồn kho, ảnh) của các dòng trong giỏ
    @Transactional(readOnly = true)
    public List<CartLineResponse> xem(List<CartItemRequest> items) {
        Map<Integer, Integer> qty = gop(items);
        if (qty.isEmpty()) return List.of();

        Map<Integer, SanPhamChiTiet> byId = ctRepo.findAllByIdWithDetails(qty.keySet()).stream()
                .collect(Collectors.toMap(SanPhamChiTiet::getId, v -> v));
        Set<Integer> productIds = byId.values().stream().map(v -> v.getSanPham().getId()).collect(Collectors.toSet());
        Map<Integer, List<Anh>> images = productIds.isEmpty() ? Map.of()
                : anhRepo.findByProductIds(productIds).stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));

        List<CartLineResponse> out = new ArrayList<>();
        for (Integer id : qty.keySet()) {
            SanPhamChiTiet v = byId.get(id);
            if (v == null) {
                out.add(new CartLineResponse(id, null, null, null, null, null, null, 0, false, null));
                continue;
            }
            boolean ok = Boolean.TRUE.equals(v.getTrangThai()) && Boolean.TRUE.equals(v.getSanPham().getTrangThai());
            out.add(new CartLineResponse(
                    id, v.getSanPham().getId(), v.getSanPham().getTenAo(),
                    new StoreProductResponse.Mau(v.getMauSac().getMaMau(), v.getMauSac().getTenMau(),
                            v.getMauSac().getMaHex() != null ? v.getMauSac().getMaHex() : "#cccccc"),
                    v.getKichThuoc().getTenKichThuoc(),
                    v.getGiaBanBanDau() != null ? v.getGiaBanBanDau() : v.getGiaBan(),
                    v.getGiaBan(),
                    v.getSoLuong() != null ? v.getSoLuong() : 0,
                    ok,
                    pickImage(images.getOrDefault(v.getSanPham().getId(), List.of()), v.getMauSac().getId())));
        }
        return out;
    }

    // Ảnh của đúng màu, nếu không có thì ảnh chung
    private String pickImage(List<Anh> list, Integer idMau) {
        return list.stream().filter(a -> a.getMauSac() != null && idMau.equals(a.getMauSac().getId()))
                .findFirst()
                .or(() -> list.stream().filter(a -> a.getMauSac() == null).findFirst())
                .map(Anh::getAnhUrl).orElse(null);
    }

    // ---- Giỏ hàng lưu trên server cho khách đã đăng nhập ----

    @Transactional(readOnly = true)
    public List<CartItemRequest> layGioHang(Integer idKhach) {
        return ghRepo.findByIdKhachHang(idKhach).stream()
                .map(g -> new CartItemRequest(g.getIdAoChiTiet(), g.getSoLuong())).toList();
    }

    @Transactional
    public void luuGioHang(Integer idKhach, List<CartItemRequest> items) {
        Map<Integer, Integer> qty = gop(items);
        Set<Integer> valid = qty.isEmpty() ? Set.of()
                : ctRepo.findAllByIdWithDetails(qty.keySet()).stream()
                .filter(v -> Boolean.TRUE.equals(v.getTrangThai()) && Boolean.TRUE.equals(v.getSanPham().getTrangThai()))
                .map(SanPhamChiTiet::getId).collect(Collectors.toSet());

        ghRepo.deleteByKhachHang(idKhach);
        List<GioHangChiTiet> rows = qty.entrySet().stream()
                .filter(e -> valid.contains(e.getKey()))
                .map(e -> {
                    GioHangChiTiet g = new GioHangChiTiet();
                    g.setIdKhachHang(idKhach);
                    g.setIdAoChiTiet(e.getKey());
                    g.setSoLuong(e.getValue());
                    return g;
                }).toList();
        ghRepo.saveAll(rows);
    }
}
