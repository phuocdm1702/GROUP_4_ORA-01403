package com.example.datn.controller;

import com.example.datn.dto.DonHangChiTietResponse;
import com.example.datn.dto.DonHangTomTat;
import com.example.datn.dto.PageResponse;
import com.example.datn.service.DonHangKhachService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/don-hang")
@RequiredArgsConstructor
public class DonHangKhachController {
    private final DonHangKhachService service;

    @GetMapping
    public PageResponse<DonHangTomTat> danhSach(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(required = false) String trangThai,
                                                @RequestParam(defaultValue = "1") int trang,
                                                @RequestParam(defaultValue = "6") int kichThuoc) {
        return service.danhSach(Integer.valueOf(jwt.getSubject()), trangThai, trang, kichThuoc);
    }

    @GetMapping("/{ma}")
    public DonHangChiTietResponse chiTiet(@AuthenticationPrincipal Jwt jwt, @PathVariable String ma) {
        return service.chiTiet(Integer.valueOf(jwt.getSubject()), ma);
    }

    @PostMapping("/{ma}/huy")
    public void huy(@AuthenticationPrincipal Jwt jwt, @PathVariable String ma) {
        service.huy(Integer.valueOf(jwt.getSubject()), ma);
    }
}