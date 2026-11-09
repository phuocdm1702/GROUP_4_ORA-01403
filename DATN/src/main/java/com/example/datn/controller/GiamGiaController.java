package com.example.datn.controller;
import com.example.datn.dto.GiamGiaRequest;
import com.example.datn.dto.GiamGiaResponse;
import com.example.datn.service.GiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/phieu-giam-gia")
@RequiredArgsConstructor
public class GiamGiaController {
    private final GiamGiaService giamGiaService;

    @GetMapping
    public ResponseEntity<List<GiamGiaResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loaiGiamGia,
            @RequestParam(required = false) String trangThaiLoc,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        return ResponseEntity.ok(giamGiaService.getAllAndFilter(keyword, loaiGiamGia, trangThaiLoc, tuNgay, denNgay));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GiamGiaRequest req) {
        try {
            return ResponseEntity.ok(giamGiaService.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody GiamGiaRequest req) {
        try {
            return ResponseEntity.ok(giamGiaService.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Cho phép cả PUT, POST, PATCH để không bị lỗi 403 CORS preflight
    @RequestMapping(value = "/{id}/toggle-status", method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(giamGiaService.toggleStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/best-vouchers")
    public ResponseEntity<List<GiamGiaResponse>> getBestVouchers(@RequestParam BigDecimal tienTruocGiam) {
        return ResponseEntity.ok(giamGiaService.getBestVouchersForOrder(tienTruocGiam));
    }
}
