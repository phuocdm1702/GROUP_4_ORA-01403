package com.example.datn.controller;

import com.example.datn.dto.MauSacRequest;
import com.example.datn.entity.MauSac;
import com.example.datn.service.MauSacService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/mau-sac")
@RequiredArgsConstructor
public class MauSacController {
    private final MauSacService service;

    @GetMapping
    public List<MauSac> getAll() { return service.getAll(); }

    @PostMapping
    public MauSac create(@Valid @RequestBody MauSacRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public MauSac update(@PathVariable Integer id, @Valid @RequestBody MauSacRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}