package com.example.datn.service;


import com.example.datn.dto.BoLocResponse;
import com.example.datn.dto.PageResponse;
import com.example.datn.dto.StoreProductResponse;
import com.example.datn.entity.Anh;
import com.example.datn.entity.MauSac;
import com.example.datn.entity.SanPhamChiTiet;
import com.example.datn.repository.AnhRepository;
import com.example.datn.repository.SanPhamChiTietRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.datn.repository.HoaDonChiTietRepository;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimKiemSanPhamService {
    private static final int MAX_PAGE_SIZE = 48;
    private static final int MAX_VALUES = 50;

    @PersistenceContext
    private EntityManager em;

    private final AnhRepository anhRepo;
    private final SanPhamChiTietRepository ctRepo;
    private final StoreProductService store;
    private final HoaDonChiTietRepository hdctRepo;

    @Transactional(readOnly = true)
    public PageResponse<StoreProductResponse> tim(
            String q, List<Integer> mau, List<Integer> kichCo, List<Integer> chatLieu,
            BigDecimal giaTu, BigDecimal giaDen, boolean conHang, boolean khuyenMai,
            String sapXep, int trang, int kichThuoc) {

        // 1) Lọc biến thể bằng SQL
        StringBuilder jpql = new StringBuilder("""
                select v from SanPhamChiTiet v
                  join fetch v.sanPham sp
                  left join fetch sp.thuongHieu
                  join fetch v.mauSac m
                  join fetch v.kichThuoc k
                  left join fetch v.chatLieu cl
                where v.trangThai = true and sp.trangThai = true
                """);
        Map<String, Object> params = new HashMap<>();

        List<Integer> mauIds = clean(mau);
        List<Integer> sizeIds = clean(kichCo);
        List<Integer> matIds = clean(chatLieu);
        if (!mauIds.isEmpty()) { jpql.append(" and m.id in :mau"); params.put("mau", mauIds); }
        if (!sizeIds.isEmpty()) { jpql.append(" and k.id in :kichCo"); params.put("kichCo", sizeIds); }
        if (!matIds.isEmpty()) { jpql.append(" and cl.id in :chatLieu"); params.put("chatLieu", matIds); }
        if (giaTu != null) { jpql.append(" and v.giaBan >= :giaTu"); params.put("giaTu", giaTu); }
        if (giaDen != null) { jpql.append(" and v.giaBan <= :giaDen"); params.put("giaDen", giaDen); }
        if (conHang) jpql.append(" and v.soLuong > 0");
        if (khuyenMai) jpql.append(" and v.giaBan < v.giaBanBanDau");
        jpql.append(" order by sp.id, m.id, k.id");

        TypedQuery<SanPhamChiTiet> query = em.createQuery(jpql.toString(), SanPhamChiTiet.class);
        for (Map.Entry<String, Object> e : params.entrySet()) query.setParameter(e.getKey(), e.getValue());
        List<SanPhamChiTiet> rows = query.getResultList();

        // 2) Tìm theo từ khóa, không phân biệt dấu và hoa thường
        String keyword = fold(q == null ? "" : q.substring(0, Math.min(q.length(), 100)));
        if (!keyword.isBlank()) {
            String[] tokens = keyword.split("\\s+");
            rows = rows.stream().filter(v -> {
                String hay = fold(v.getSanPham().getTenAo() + " " + v.getSanPham().getMaAo() + " "
                        + v.getMauSac().getTenMau() + " "
                        + (v.getChatLieu() == null ? "" : v.getChatLieu().getTenChatLieu()));
                return Arrays.stream(tokens).allMatch(hay::contains);
            }).toList();
        }

        // 3) Gom theo sản phẩm và sắp xếp
        Map<Integer, List<SanPhamChiTiet>> byProduct = new LinkedHashMap<>();
        for (SanPhamChiTiet v : rows) {
            byProduct.computeIfAbsent(v.getSanPham().getId(), x -> new ArrayList<>()).add(v);
        }
        List<List<SanPhamChiTiet>> groups = new ArrayList<>(byProduct.values());

        switch (sapXep == null ? "moi" : sapXep) {
            case "gia-tang" -> groups.sort((a, b) -> {
                int cmp = minPrice(a).compareTo(minPrice(b));
                return cmp != 0 ? cmp : id(b) - id(a);
            });
            case "gia-giam" -> groups.sort((a, b) -> {
                int cmp = minPrice(b).compareTo(minPrice(a));
                return cmp != 0 ? cmp : id(b) - id(a);
            });
            case "ban-chay" -> {
                Map<Integer, Long> sold = new HashMap<>();
                for (Object[] r : hdctRepo.daBanTheoSanPham("Đã hủy")) {
                    sold.put((Integer) r[0], ((Number) r[1]).longValue());
                }
                groups.sort((a, b) -> {
                    int cmp = Long.compare(sold.getOrDefault(id(b), 0L), sold.getOrDefault(id(a), 0L));
                    return cmp != 0 ? cmp : id(b) - id(a);     // bằng nhau thì mẫu mới hơn lên trước
                });
            }
            case "ten" -> {
                Collator collator = Collator.getInstance(Locale.forLanguageTag("vi"));
                groups.sort((a, b) -> collator.compare(name(a), name(b)));
            }
            default -> groups.sort((a, b) -> id(b) - id(a));    // mới nhất
        }

        // 4) Cắt trang
        int size = Math.min(Math.max(kichThuoc, 1), MAX_PAGE_SIZE);
        int total = groups.size();
        int pages = Math.max(1, (int) Math.ceil(total / (double) size));
        int page = Math.min(Math.max(trang, 1), pages);
        List<List<SanPhamChiTiet>> slice = groups.subList((page - 1) * size, Math.min(total, page * size));

        // 5) Chỉ tải ảnh của các sản phẩm trong trang này
        Set<Integer> ids = slice.stream().map(this::id).collect(Collectors.toSet());
        Map<Integer, List<Anh>> images = ids.isEmpty() ? Map.of()
                : anhRepo.findByProductIds(ids).stream()
                .collect(Collectors.groupingBy(a -> a.getSanPham().getId()));

        List<StoreProductResponse> items = slice.stream()
                .map(g -> store.toResponse(g, images.getOrDefault(id(g), List.of())))
                .toList();
        return new PageResponse<>(items, page, size, total, pages);
    }

    // Các lựa chọn cho bộ lọc: chỉ gồm màu, size, chất liệu đang có hàng bán
    @Transactional(readOnly = true)
    public BoLocResponse boLoc() {
        Map<Integer, BoLocResponse.MauOpt> colors = new TreeMap<>();
        Map<Integer, BoLocResponse.Opt> sizes = new TreeMap<>();
        Map<Integer, BoLocResponse.Opt> materials = new TreeMap<>();
        BigDecimal min = null, max = null;

        for (SanPhamChiTiet v : ctRepo.findAllActive()) {
            MauSac m = v.getMauSac();
            colors.putIfAbsent(m.getId(), new BoLocResponse.MauOpt(
                    m.getId(), m.getMaMau(), m.getTenMau(), m.getMaHex() != null ? m.getMaHex() : "#cccccc"));
            sizes.putIfAbsent(v.getKichThuoc().getId(),
                    new BoLocResponse.Opt(v.getKichThuoc().getId(), v.getKichThuoc().getTenKichThuoc()));
            if (v.getChatLieu() != null) {
                materials.putIfAbsent(v.getChatLieu().getId(),
                        new BoLocResponse.Opt(v.getChatLieu().getId(), v.getChatLieu().getTenChatLieu()));
            }
            BigDecimal p = v.getGiaBan();
            if (p != null) {
                if (min == null || p.compareTo(min) < 0) min = p;
                if (max == null || p.compareTo(max) > 0) max = p;
            }
        }
        return new BoLocResponse(new ArrayList<>(colors.values()), new ArrayList<>(sizes.values()),
                new ArrayList<>(materials.values()), min, max);
    }

    // ---------------------------------------------------------------

    private int id(List<SanPhamChiTiet> g) { return g.get(0).getSanPham().getId(); }

    private String name(List<SanPhamChiTiet> g) { return g.get(0).getSanPham().getTenAo(); }

    private BigDecimal minPrice(List<SanPhamChiTiet> g) {
        return g.stream().map(SanPhamChiTiet::getGiaBan).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
    }

    private static List<Integer> clean(List<Integer> list) {
        return list == null ? List.of()
                : list.stream().filter(Objects::nonNull).distinct().limit(MAX_VALUES).toList();
    }

    // "Áo Polo Đen" -> "ao polo den"
    private static String fold(String s) {
        String n = Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return n.replace('đ', 'd').trim();
    }
}
