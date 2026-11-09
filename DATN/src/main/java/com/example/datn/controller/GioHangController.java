package com.example.datn.controller;

import com.example.datn.dto.CartItemRequest;
import com.example.datn.dto.CartLineResponse;
import com.example.datn.service.GioHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GioHangController {
    private final GioHangService service;

    // Công khai: khách vãng lai cũng xem được giỏ hàng của mình
    @PostMapping("/api/public/gio-hang/xem")
    public List<CartLineResponse> xem(@RequestBody List<CartItemRequest> items) {
        return service.xem(items);
    }

    @GetMapping("/api/user/gio-hang")
    public List<CartItemRequest> layGioHang(@AuthenticationPrincipal Jwt jwt) {
        return service.layGioHang(Integer.valueOf(jwt.getSubject()));
    }

    @PutMapping("/api/user/gio-hang")
    public void luuGioHang(@AuthenticationPrincipal Jwt jwt, @RequestBody List<CartItemRequest> items) {
        service.luuGioHang(Integer.valueOf(jwt.getSubject()), items);
    }
}
