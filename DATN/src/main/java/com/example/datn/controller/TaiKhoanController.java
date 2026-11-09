package com.example.datn.controller;

import com.example.datn.dto.*;
import com.example.datn.service.TaiKhoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class TaiKhoanController {
    private final TaiKhoanService service;

    private static Integer id(Jwt jwt) { return Integer.valueOf(jwt.getSubject()); }

    @GetMapping("/tai-khoan")
    public TaiKhoanResponse taiKhoan(@AuthenticationPrincipal Jwt jwt) {
        return service.layThongTin(id(jwt));
    }

    @PutMapping("/tai-khoan")
    public TaiKhoanResponse capNhat(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody KhachHangRequest r) {
        return service.capNhat(id(jwt), r);
    }

    @PutMapping("/doi-mat-khau")
    public void doiMatKhau(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DoiMatKhauRequest r) {
        service.doiMatKhau(id(jwt), r);
    }

    @GetMapping("/dia-chi")
    public List<DiaChiResponse> diaChi(@AuthenticationPrincipal Jwt jwt) {
        return service.danhSachDiaChi(id(jwt));
    }

    @PostMapping("/dia-chi")
    public DiaChiResponse them(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DiaChiRequest r) {
        return service.themDiaChi(id(jwt), r);
    }

    @PutMapping("/dia-chi/{idDiaChi}")
    public DiaChiResponse sua(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer idDiaChi,
                              @Valid @RequestBody DiaChiRequest r) {
        return service.suaDiaChi(id(jwt), idDiaChi, r);
    }

    @PutMapping("/dia-chi/{idDiaChi}/mac-dinh")
    public void datMacDinh(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer idDiaChi) {
        service.datMacDinh(id(jwt), idDiaChi);
    }

    @DeleteMapping("/dia-chi/{idDiaChi}")
    public void xoa(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer idDiaChi) {
        service.xoaDiaChi(id(jwt), idDiaChi);
    }
}