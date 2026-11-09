package com.example.datn.dto;

import java.util.List;

public record AuthResponse(
        String token,
        String loai,
        Integer id,
        String hoVaTen,
        String tenVaiTro,      // chỉ có với nhân viên
        List<String> roles
) {}
