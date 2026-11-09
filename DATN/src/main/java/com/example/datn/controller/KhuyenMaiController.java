package com.example.datn.controller;
import com.example.datn.dto.BienTheKhuyenMaiResponse;
import com.example.datn.dto.KhuyenMaiRequest;
import com.example.datn.dto.KhuyenMaiResponse;
import com.example.datn.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dot-giam-gia")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    @GetMapping
    public ResponseEntity<List<KhuyenMaiResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String trangThaiLoc,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay
    ) {
        return ResponseEntity.ok(khuyenMaiService.getAllAndFilter(keyword, trangThaiLoc, tuNgay, denNgay));
    }

    @GetMapping("/bien-the-san-pham")
    public ResponseEntity<List<BienTheKhuyenMaiResponse>> getAllVariants() {
        return ResponseEntity.ok(khuyenMaiService.getAllVariantsForSelection());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody KhuyenMaiRequest req) {
        try {
            return ResponseEntity.ok(khuyenMaiService.create(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody KhuyenMaiRequest req) {
        try {
            return ResponseEntity.ok(khuyenMaiService.update(id, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @RequestMapping(value = "/{id}/toggle-status", method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(khuyenMaiService.toggleStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
