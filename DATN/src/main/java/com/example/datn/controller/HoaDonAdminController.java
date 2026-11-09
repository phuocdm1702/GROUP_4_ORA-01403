package com.example.datn.controller;
import com.example.datn.dto.CapNhatThongTinHoaDonRequest;
import com.example.datn.dto.CapNhatTrangThaiHoaDonRequest;
import com.example.datn.dto.HoaDonResponse;
import com.example.datn.service.HoaDonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonAdminController {
    private final HoaDonAdminService hoaDonAdminService;

    @GetMapping
    public ResponseEntity<List<HoaDonResponse>> searchOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String trangThaiThanhToan,
            @RequestParam(required = false) String hinhThucMuaHang,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "false") boolean tatCaNgay
    ) {
        return ResponseEntity.ok(hoaDonAdminService.searchOrders(
                keyword, trangThai, trangThaiThanhToan, hinhThucMuaHang, tuNgay, denNgay, tatCaNgay
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(hoaDonAdminService.getDetail(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Cho phép cả PUT và PATCH để không bị chặn bởi CORS preflight 403
    @RequestMapping(value = "/{id}/trang-thai", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody CapNhatTrangThaiHoaDonRequest req) {
        try {
            return ResponseEntity.ok(hoaDonAdminService.updateStatus(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/thong-tin")
    public ResponseEntity<?> updateOrderInfo(@PathVariable Integer id, @RequestBody CapNhatThongTinHoaDonRequest req) {
        try {
            return ResponseEntity.ok(hoaDonAdminService.updateOrderInfo(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/hoan-phi")
    public ResponseEntity<?> confirmRefund(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body) {
        try {
            String ghiChu = body != null ? body.get("ghiChu") : null;
            return ResponseEntity.ok(hoaDonAdminService.confirmRefund(id, ghiChu));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/mua-lai-ngay")
    public ResponseEntity<?> recreateCompensateOrder(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(hoaDonAdminService.recreateCompensateOrder(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
