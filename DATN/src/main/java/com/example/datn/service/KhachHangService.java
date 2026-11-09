package com.example.datn.service;

import com.example.datn.dto.KhachHangChiTietResponse;
import com.example.datn.dto.KhachHangRequest;
import com.example.datn.dto.KhachHangResponse;
import com.example.datn.entity.DiaChi;
import com.example.datn.entity.KhachHang;
import com.example.datn.repository.DiaChiRepository;
import com.example.datn.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhachHangService {
    private static final String DA_HOAN_THANH = "Đã hoàn thành";
    private static final String PW_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";   // bỏ ký tự dễ nhầm
    private static final SecureRandom RANDOM = new SecureRandom();

    private final KhachHangRepository repo;
    private final DiaChiRepository diaChiRepo;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<KhachHangResponse> getAll() {
        Map<Integer, KhachHangRepository.ThongKe> stats = repo.thongKe(DA_HOAN_THANH).stream()
                .collect(Collectors.toMap(KhachHangRepository.ThongKe::getIdKhachHang, Function.identity()));
        return repo.findAllByOrderByIdDesc().stream()
                .map(k -> toResponse(k, stats.get(k.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public KhachHangChiTietResponse getOne(Integer id) {
        KhachHang k = find(id);
        KhachHangRepository.ThongKe stat = repo.thongKe(DA_HOAN_THANH).stream()
                .filter(s -> Objects.equals(s.getIdKhachHang(), id)).findFirst().orElse(null);

        List<KhachHangChiTietResponse.DiaChiInfo> diaChi = diaChiRepo
                .findByIdKhachHangOrderByMacDinhDescIdAsc(id).stream()
                .map(d -> new KhachHangChiTietResponse.DiaChiInfo(d.getId(), fullAddress(d), d.getMacDinh()))
                .toList();

        List<KhachHangChiTietResponse.DonInfo> don = repo.donGanDay(id).stream()
                .map(d -> new KhachHangChiTietResponse.DonInfo(
                        d.getMaHoaDon(), d.getNgayTao(), d.getTongTien(), d.getTrangThai()))
                .toList();

        return new KhachHangChiTietResponse(toResponse(k, stat), diaChi, don);
    }

    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest r) {
        KhachHang k = find(id);
        String phone = r.soDienThoai().replaceAll("\\s+", "");
        String email = r.email().trim().toLowerCase();
        if (repo.existsBySoDienThoaiAndIdNot(phone, id)) throw new RuntimeException("Số điện thoại đã được sử dụng");
        if (repo.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new RuntimeException("Email đã được sử dụng");

        k.setHoVaTen(r.hoVaTen().trim());
        k.setGioiTinh(r.gioiTinh());
        k.setNgaySinh(r.ngaySinh());
        k.setSoDienThoai(phone);
        k.setEmail(email);
        repo.save(k);
        return toResponse(k, null);   // danh sách sẽ được tải lại, không cần số liệu thống kê ở đây
    }

    @Transactional
    public void setStatus(Integer id, boolean value) {
        KhachHang k = find(id);
        k.setTrangThai(value);
        repo.save(k);
    }

    // Sinh mật khẩu tạm, chỉ trả về một lần. Trong database chỉ lưu bản băm.
    @Transactional
    public String resetPassword(Integer id) {
        KhachHang k = find(id);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(PW_CHARS.charAt(RANDOM.nextInt(PW_CHARS.length())));
        String raw = sb.toString();
        k.setMatKhau(encoder.encode(raw));
        repo.save(k);
        return raw;
    }

    // ---------------------------------------------------------------

    private KhachHang find(Integer id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
    }

    private String fullAddress(DiaChi d) {
        return String.join(", ", d.getDuong(), d.getXaPhuong(), d.getQuanHuyen(), d.getTinhThanhPho());
    }

    private KhachHangResponse toResponse(KhachHang k, KhachHangRepository.ThongKe s) {
        return new KhachHangResponse(
                k.getId(), k.getMaKhachHang(), k.getHoVaTen(), k.getGioiTinh(), k.getNgaySinh(),
                k.getSoDienThoai(), k.getEmail(), k.getTrangThai(), k.getNgayTao(),
                s == null ? 0 : s.getSoDon(),
                s == null ? BigDecimal.ZERO : s.getTongChiTieu());
    }
}