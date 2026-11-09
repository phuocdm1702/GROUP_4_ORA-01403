package com.example.datn.controller;

import com.example.datn.dto.SanPhamAdminResponse;
import com.example.datn.dto.SanPhamRequest;
import com.example.datn.service.SanPhamAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamAdminController {
    private final SanPhamAdminService service;

    @GetMapping
    public List<SanPhamAdminResponse> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public SanPhamAdminResponse getOne(@PathVariable Integer id) { return service.getOne(id); }

    @PostMapping
    public SanPhamAdminResponse create(@Valid @RequestBody SanPhamRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public SanPhamAdminResponse update(@PathVariable Integer id, @Valid @RequestBody SanPhamRequest r) {
        return service.update(id, r);
    }

    // Dùng PUT (không dùng PATCH) vì CorsConfig hiện chỉ cho GET/POST/PUT/DELETE
    @PutMapping("/{id}/trang-thai")
    public void setStatus(@PathVariable Integer id, @RequestParam boolean value) {
        service.setStatus(id, value);
    }
}
