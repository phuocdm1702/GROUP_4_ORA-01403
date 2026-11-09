package com.example.datn.controller;

import com.example.datn.dto.KhachHangChiTietResponse;
import com.example.datn.dto.KhachHangRequest;
import com.example.datn.dto.KhachHangResponse;
import com.example.datn.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {
    private final KhachHangService service;

    @GetMapping
    public List<KhachHangResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public KhachHangChiTietResponse getOne(@PathVariable Integer id) { return service.getOne(id); }

    @PutMapping("/{id}")
    public KhachHangResponse update(@PathVariable Integer id, @Valid @RequestBody KhachHangRequest r) {
        return service.update(id, r);
    }

    @PutMapping("/{id}/trang-thai")
    public void setStatus(@PathVariable Integer id, @RequestParam boolean value) {
        service.setStatus(id, value);
    }

    @PostMapping("/{id}/dat-lai-mat-khau")
    public Map<String, String> resetPassword(@PathVariable Integer id) {
        return Map.of("matKhauMoi", service.resetPassword(id));
    }
}
