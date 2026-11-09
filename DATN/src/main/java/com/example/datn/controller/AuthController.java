package com.example.datn.controller;

import com.example.datn.dto.AuthResponse;
import com.example.datn.dto.LoginRequest;
import com.example.datn.dto.RegisterRequest;
import com.example.datn.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/admin/dang-nhap")
    public AuthResponse loginAdmin(@Valid @RequestBody LoginRequest r) { return service.loginStaff(r); }

    @PostMapping("/khach-hang/dang-nhap")
    public AuthResponse loginCustomer(@Valid @RequestBody LoginRequest r) { return service.loginCustomer(r); }

    @PostMapping("/khach-hang/dang-ky")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) { return service.register(r); }
}
