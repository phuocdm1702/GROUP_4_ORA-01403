package com.example.datn.controller;

import com.example.datn.dto.BaoGiaRequest;
import com.example.datn.dto.BaoGiaResponse;
import com.example.datn.dto.DatHangRequest;
import com.example.datn.dto.DatHangResponse;
import com.example.datn.service.DonHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/dat-hang")
@RequiredArgsConstructor
public class DatHangController {
    private final DonHangService service;

    @PostMapping("/tinh-tien")
    public BaoGiaResponse baoGia(@Valid @RequestBody BaoGiaRequest r) {
        return service.baoGia(r);
    }

    @PostMapping
    public DatHangResponse dat(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DatHangRequest r) {
        return service.dat(Integer.valueOf(jwt.getSubject()), r);
    }
}
