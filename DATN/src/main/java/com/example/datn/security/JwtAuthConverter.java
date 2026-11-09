package com.example.datn.security;

import com.example.datn.entity.KhachHang;
import com.example.datn.entity.NhanVien;
import com.example.datn.repository.KhachHangRepository;
import com.example.datn.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Mỗi request: kiểm tra tài khoản còn tồn tại, còn hoạt động, rồi lấy quyền từ database
@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final NhanVienRepository nvRepo;
    private final KhachHangRepository khRepo;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String loai = jwt.getClaimAsString("loai");
        Integer id;
        try {
            id = Integer.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Token không hợp lệ");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (JwtService.LOAI_NHAN_VIEN.equals(loai)) {
            NhanVien nv = nvRepo.findByIdWithRole(id)
                    .filter(n -> Boolean.TRUE.equals(n.getTrangThai()))
                    .orElseThrow(() -> new BadCredentialsException("Tài khoản không còn hiệu lực"));
            VaiTroCode.rolesOf(nv.getVaiTro().getTenVaiTro())
                    .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        } else if (JwtService.LOAI_KHACH_HANG.equals(loai)) {
            KhachHang kh = khRepo.findById(id)
                    .filter(k -> Boolean.TRUE.equals(k.getTrangThai()))
                    .orElseThrow(() -> new BadCredentialsException("Tài khoản không còn hiệu lực"));
            authorities.add(new SimpleGrantedAuthority("ROLE_KHACH_HANG"));
        } else {
            throw new BadCredentialsException("Token không hợp lệ");
        }
        return new JwtAuthenticationToken(jwt, authorities, loai + ":" + id);
    }
}
