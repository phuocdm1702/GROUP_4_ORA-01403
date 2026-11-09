package com.example.datn.controller;

import com.example.datn.dto.NhanVienRequest;
import com.example.datn.dto.NhanVienResponse;
import com.example.datn.service.NhanVienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/nhan-vien")
@RequiredArgsConstructor
public class NhanVienController {
    private final NhanVienService service;

    @GetMapping
    public List<NhanVienResponse> getAll() { return service.getAll(); }

    @PostMapping
    public NhanVienResponse create(@Valid @RequestBody NhanVienRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public NhanVienResponse update(@PathVariable Integer id, @Valid @RequestBody NhanVienRequest r) {
        return service.update(id, r);
    }

    @PutMapping("/{id}/trang-thai")
    public void setStatus(@PathVariable Integer id, @RequestParam boolean value) {
        service.setStatus(id, value);
    }
}
