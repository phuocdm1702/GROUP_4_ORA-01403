package com.example.datn.controller;

import com.example.datn.dto.BoLocResponse;
import com.example.datn.dto.PageResponse;
import com.example.datn.dto.StoreProductResponse;
import com.example.datn.service.StoreProductService;
import com.example.datn.service.TimKiemSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/public/san-pham")
@RequiredArgsConstructor
public class StoreProductController {
    private final StoreProductService service;
    private final TimKiemSanPhamService timKiem;

    @GetMapping
    public List<StoreProductResponse> getAll() {
        return service.getAll();
    }

    // Ví dụ: /tim-kiem?q=polo&mau=1,3&kichCo=2&giaTu=200000&giaDen=400000&sapXep=gia-tang&trang=1&kichThuoc=12
    @GetMapping("/tim-kiem")
    public PageResponse<StoreProductResponse> timKiem(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<Integer> mau,
            @RequestParam(required = false) List<Integer> kichCo,
            @RequestParam(required = false) List<Integer> chatLieu,
            @RequestParam(required = false) BigDecimal giaTu,
            @RequestParam(required = false) BigDecimal giaDen,
            @RequestParam(defaultValue = "false") boolean conHang,
            @RequestParam(defaultValue = "false") boolean khuyenMai,
            @RequestParam(defaultValue = "moi") String sapXep,
            @RequestParam(defaultValue = "1") int trang,
            @RequestParam(defaultValue = "12") int kichThuoc) {
        return timKiem.tim(q, mau, kichCo, chatLieu, giaTu, giaDen, conHang, khuyenMai, sapXep, trang, kichThuoc);
    }

    @GetMapping("/bo-loc")
    public BoLocResponse boLoc() {
        return timKiem.boLoc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreProductResponse> getOne(@PathVariable Integer id) {
        return service.getOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}