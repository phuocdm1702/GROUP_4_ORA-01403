package com.example.datn.controller;


import com.example.datn.dto.DanhMucResponse;
import com.example.datn.repository.ChatLieuRepository;
import com.example.datn.repository.KichThuocRepository;
import com.example.datn.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {
    private final MauSacRepository mauRepo;
    private final KichThuocRepository sizeRepo;
    private final ChatLieuRepository clRepo;

    @GetMapping
    public DanhMucResponse get() {
        Sort byId = Sort.by("id");
        return new DanhMucResponse(
                mauRepo.findAll(byId).stream().filter(x -> Boolean.TRUE.equals(x.getTrangThai())).toList(),
                sizeRepo.findAll(byId).stream().filter(x -> Boolean.TRUE.equals(x.getTrangThai())).toList(),
                clRepo.findAll(byId).stream().filter(x -> Boolean.TRUE.equals(x.getTrangThai())).toList());
    }
}
