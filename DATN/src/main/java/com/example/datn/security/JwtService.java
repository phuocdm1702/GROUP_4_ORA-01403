package com.example.datn.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    public static final String LOAI_NHAN_VIEN = "NHAN_VIEN";
    public static final String LOAI_KHACH_HANG = "KHACH_HANG";

    private final JwtEncoder encoder;

    // Token chỉ chứa mã người dùng và loại tài khoản. Quyền hạn luôn đọc lại từ database.
    public String issue(String loai, Integer id, long hours) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("polo-store")
                .issuedAt(now)
                .expiresAt(now.plus(hours, ChronoUnit.HOURS))
                .subject(String.valueOf(id))
                .claim("loai", loai)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
