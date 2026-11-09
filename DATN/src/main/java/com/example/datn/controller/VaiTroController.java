package com.example.datn.controller;

import com.example.datn.entity.VaiTro;
import com.example.datn.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vai-tro")
@RequiredArgsConstructor
public class VaiTroController {
    private final VaiTroRepository repo;

    @GetMapping
    public List<VaiTro> getAll() { return repo.findAll(Sort.by("id")); }
}
