package com.example.datn.service;

import com.example.datn.dto.AuthResponse;
import com.example.datn.dto.LoginRequest;
import com.example.datn.dto.RegisterRequest;
import com.example.datn.entity.KhachHang;
import com.example.datn.entity.NhanVien;
import com.example.datn.repository.KhachHangRepository;
import com.example.datn.repository.NhanVienRepository;
import com.example.datn.security.JwtService;
import com.example.datn.security.VaiTroCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    // Một mã băm hợp lệ để so sánh khi không có tài khoản, giúp thời gian phản hồi không lộ tài khoản có tồn tại hay không
    private static final String DUMMY_HASH = "$2b$10$BkZLUthz/ZhbwqtwdPCA.OT4n.gJGIOdHyKwLNq2IestVY9UCc4wC";
    private static final Pattern CODE = Pattern.compile("^KH(\\d+)$");

    private final NhanVienRepository nvRepo;
    private final KhachHangRepository khRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Value("${app.jwt.staff-hours:8}")
    private long staffHours;
    @Value("${app.jwt.customer-hours:72}")
    private long customerHours;

    @Transactional(readOnly = true)
    public AuthResponse loginStaff(LoginRequest r) {
        String key = r.tenDangNhap().replaceAll("\\s+", "");
        List<NhanVien> found = nvRepo.findForLogin(key);
        NhanVien nv = found.stream().filter(n -> key.equalsIgnoreCase(n.getTenNguoiDung())).findFirst()
                .orElse(found.isEmpty() ? null : found.get(0));

        checkPassword(r.matKhau(), nv == null ? null : nv.getMatKhau());
        if (!Boolean.TRUE.equals(nv.getTrangThai())) throw new RuntimeException("Tài khoản đã bị khóa");

        return new AuthResponse(
                jwt.issue(JwtService.LOAI_NHAN_VIEN, nv.getId(), staffHours),
                JwtService.LOAI_NHAN_VIEN, nv.getId(), nv.getHoVaTen(),
                nv.getVaiTro().getTenVaiTro(),
                VaiTroCode.rolesOf(nv.getVaiTro().getTenVaiTro()));
    }

    @Transactional(readOnly = true)
    public AuthResponse loginCustomer(LoginRequest r) {
        String key = r.tenDangNhap().replaceAll("\\s+", "");
        List<KhachHang> found = khRepo.findForLogin(key);
        KhachHang k = found.isEmpty() ? null : found.get(0);

        checkPassword(r.matKhau(), k == null ? null : k.getMatKhau());
        if (!Boolean.TRUE.equals(k.getTrangThai())) throw new RuntimeException("Tài khoản đã bị khóa");
        return customerResponse(k);
    }

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String phone = r.soDienThoai().replaceAll("\\s+", "");
        String email = r.email().trim().toLowerCase();
        if (khRepo.existsBySoDienThoaiAndIdNot(phone, -1)) throw new RuntimeException("Số điện thoại đã được đăng ký");
        if (khRepo.existsByEmailIgnoreCaseAndIdNot(email, -1)) throw new RuntimeException("Email đã được đăng ký");

        KhachHang k = new KhachHang();
        k.setMaKhachHang(nextCode());
        k.setHoVaTen(r.hoVaTen().trim());
        k.setSoDienThoai(phone);
        k.setEmail(email);
        k.setMatKhau(encoder.encode(r.matKhau()));
        k.setTrangThai(true);
        khRepo.save(k);
        return customerResponse(k);       // đăng ký xong đăng nhập luôn
    }

    // ---------------------------------------------------------------

    private void checkPassword(String raw, String hash) {
        boolean ok = encoder.matches(raw, hash == null ? DUMMY_HASH : hash);
        if (hash == null || !ok) throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
    }

    private AuthResponse customerResponse(KhachHang k) {
        return new AuthResponse(
                jwt.issue(JwtService.LOAI_KHACH_HANG, k.getId(), customerHours),
                JwtService.LOAI_KHACH_HANG, k.getId(), k.getHoVaTen(), null, List.of("KHACH_HANG"));
    }

    private String nextCode() {
        int max = khRepo.findAll().stream()
                .map(KhachHang::getMaKhachHang)
                .map(CODE::matcher)
                .filter(Matcher::matches)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .max().orElse(0);
        return String.format("KH%02d", max + 1);
    }
}
