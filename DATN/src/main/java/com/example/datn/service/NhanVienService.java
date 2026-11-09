package com.example.datn.service;

import com.example.datn.dto.NhanVienRequest;
import com.example.datn.dto.NhanVienResponse;
import com.example.datn.entity.NhanVien;
import com.example.datn.entity.VaiTro;
import com.example.datn.repository.NhanVienRepository;
import com.example.datn.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NhanVienService {
    private static final String ADMIN_ROLE = "Quản trị viên";
    private static final Pattern CODE = Pattern.compile("^NV(\\d+)$");

    private final NhanVienRepository repo;
    private final VaiTroRepository roleRepo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<NhanVienResponse> getAll() {
        return repo.findAllWithRole().stream().map(this::toResponse).toList();
    }

    @Transactional
    public NhanVienResponse create(NhanVienRequest r) {
        if (r.matKhau() == null || r.matKhau().isBlank())
            throw new RuntimeException("Vui lòng nhập mật khẩu");
        checkUnique(r, -1);
        VaiTro role = roleOf(r);

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(nextCode());
        apply(nv, r, role);
        nv.setMatKhau(encoder.encode(r.matKhau()));
        if (r.trangThai() == null) nv.setTrangThai(true);
        return toResponse(repo.save(nv));
    }

    @Transactional
    public NhanVienResponse update(Integer id, NhanVienRequest r) {
        NhanVien nv = find(id);
        checkUnique(r, id);
        VaiTro role = roleOf(r);
        boolean active = r.trangThai() != null ? r.trangThai() : Boolean.TRUE.equals(nv.getTrangThai());
        ensureAdminRemains(nv, active, role);

        apply(nv, r, role);
        if (r.matKhau() != null && !r.matKhau().isBlank()) {
            nv.setMatKhau(encoder.encode(r.matKhau()));
        }
        return toResponse(repo.save(nv));
    }

    @Transactional
    public void setStatus(Integer id, boolean value) {
        NhanVien nv = find(id);
        ensureAdminRemains(nv, value, nv.getVaiTro());
        nv.setTrangThai(value);
        repo.save(nv);
    }

    // ---------------------------------------------------------------

    private NhanVien find(Integer id) {
        return repo.findByIdWithRole(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    private VaiTro roleOf(NhanVienRequest r) {
        return roleRepo.findById(r.idVaiTro()).orElseThrow(() -> new RuntimeException("Vai trò không tồn tại"));
    }

    private void checkUnique(NhanVienRequest r, int excludeId) {
        if (repo.existsByEmailIgnoreCaseAndIdNot(r.email().trim(), excludeId))
            throw new RuntimeException("Email đã được sử dụng");
        if (repo.existsByTenNguoiDungIgnoreCaseAndIdNot(r.tenNguoiDung().trim(), excludeId))
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        if (repo.existsBySoDienThoaiAndIdNot(cleanPhone(r.soDienThoai()), excludeId))
            throw new RuntimeException("Số điện thoại đã được sử dụng");
    }

    // Không để hệ thống mất quản trị viên cuối cùng (bị khóa hoặc bị hạ quyền)
    private void ensureAdminRemains(NhanVien nv, boolean willBeActive, VaiTro newRole) {
        boolean isActiveAdminNow = Boolean.TRUE.equals(nv.getTrangThai())
                && ADMIN_ROLE.equals(nv.getVaiTro().getTenVaiTro());
        boolean staysActiveAdmin = willBeActive && ADMIN_ROLE.equals(newRole.getTenVaiTro());
        if (isActiveAdminNow && !staysActiveAdmin
                && repo.countByVaiTro_TenVaiTroAndTrangThai(ADMIN_ROLE, true) <= 1) {
            throw new RuntimeException("Phải còn ít nhất một quản trị viên đang hoạt động");
        }
    }

    private void apply(NhanVien nv, NhanVienRequest r, VaiTro role) {
        if (r.ngaySinh() != null && Period.between(r.ngaySinh(), LocalDate.now()).getYears() < 18)
            throw new RuntimeException("Nhân viên phải từ 18 tuổi trở lên");
        nv.setVaiTro(role);
        nv.setHoVaTen(r.hoVaTen().trim());
        nv.setGioiTinh(r.gioiTinh());
        nv.setNgaySinh(r.ngaySinh());
        nv.setSoDienThoai(cleanPhone(r.soDienThoai()));
        nv.setDiaChi(r.diaChi() == null || r.diaChi().isBlank() ? null : r.diaChi().trim());
        nv.setEmail(r.email().trim().toLowerCase());
        nv.setTenNguoiDung(r.tenNguoiDung().trim());
        if (r.trangThai() != null) nv.setTrangThai(r.trangThai());
    }

    private String cleanPhone(String s) {
        return s.replaceAll("\\s+", "");
    }

    private String nextCode() {
        int max = repo.findAll().stream()
                .map(NhanVien::getMaNhanVien)
                .map(CODE::matcher)
                .filter(Matcher::matches)
                .mapToInt(m -> Integer.parseInt(m.group(1)))
                .max().orElse(0);
        return String.format("NV%02d", max + 1);
    }

    private NhanVienResponse toResponse(NhanVien n) {
        return new NhanVienResponse(
                n.getId(), n.getMaNhanVien(), n.getHoVaTen(), n.getGioiTinh(), n.getNgaySinh(),
                n.getSoDienThoai(), n.getDiaChi(), n.getEmail(), n.getTenNguoiDung(), n.getTrangThai(),
                n.getVaiTro().getId(), n.getVaiTro().getTenVaiTro());
    }
}
