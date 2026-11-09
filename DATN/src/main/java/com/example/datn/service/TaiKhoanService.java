package com.example.datn.service;

import com.example.datn.dto.*;
import com.example.datn.entity.DiaChi;
import com.example.datn.entity.KhachHang;
import com.example.datn.repository.DiaChiRepository;
import com.example.datn.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {
    private static final int MAX_DIA_CHI = 10;

    private final KhachHangRepository khRepo;
    private final DiaChiRepository diaChiRepo;
    private final PasswordEncoder encoder;

    // ---------- Hồ sơ ----------

    @Transactional(readOnly = true)
    public TaiKhoanResponse layThongTin(Integer id) {
        return toResponse(find(id));
    }

    @Transactional
    public TaiKhoanResponse capNhat(Integer id, KhachHangRequest r) {
        KhachHang k = find(id);
        String phone = r.soDienThoai().replaceAll("\\s+", "");
        String email = r.email().trim().toLowerCase();
        if (khRepo.existsBySoDienThoaiAndIdNot(phone, id)) throw new RuntimeException("Số điện thoại đã được sử dụng");
        if (khRepo.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new RuntimeException("Email đã được sử dụng");

        k.setHoVaTen(r.hoVaTen().trim());
        k.setGioiTinh(r.gioiTinh());
        k.setNgaySinh(r.ngaySinh());
        k.setSoDienThoai(phone);
        k.setEmail(email);
        khRepo.save(k);
        return toResponse(k);
    }

    @Transactional
    public void doiMatKhau(Integer id, DoiMatKhauRequest r) {
        KhachHang k = find(id);
        if (!encoder.matches(r.matKhauCu(), k.getMatKhau())) throw new RuntimeException("Mật khẩu hiện tại không đúng");
        if (r.matKhauCu().equals(r.matKhauMoi())) throw new RuntimeException("Mật khẩu mới phải khác mật khẩu hiện tại");
        k.setMatKhau(encoder.encode(r.matKhauMoi()));
        khRepo.save(k);
    }

    // ---------- Sổ địa chỉ ----------

    @Transactional(readOnly = true)
    public List<DiaChiResponse> danhSachDiaChi(Integer idKh) {
        return diaChiRepo.findByIdKhachHangOrderByMacDinhDescIdAsc(idKh).stream().map(this::toResponse).toList();
    }

    @Transactional
    public DiaChiResponse themDiaChi(Integer idKh, DiaChiRequest r) {
        long n = diaChiRepo.countByIdKhachHang(idKh);
        if (n >= MAX_DIA_CHI) throw new RuntimeException("Mỗi tài khoản tối đa " + MAX_DIA_CHI + " địa chỉ");
        boolean macDinh = n == 0 || Boolean.TRUE.equals(r.macDinh());   // địa chỉ đầu tiên luôn là mặc định
        if (macDinh) boMacDinhCu(idKh, null);

        DiaChi d = new DiaChi();
        d.setIdKhachHang(idKh);
        apply(d, r);
        d.setMacDinh(macDinh);
        return toResponse(diaChiRepo.save(d));
    }

    @Transactional
    public DiaChiResponse suaDiaChi(Integer idKh, Integer id, DiaChiRequest r) {
        DiaChi d = tim(idKh, id);
        apply(d, r);
        if (Boolean.TRUE.equals(r.macDinh()) && !Boolean.TRUE.equals(d.getMacDinh())) {
            boMacDinhCu(idKh, id);
            d.setMacDinh(true);
        }
        return toResponse(diaChiRepo.save(d));
    }

    @Transactional
    public void datMacDinh(Integer idKh, Integer id) {
        DiaChi d = tim(idKh, id);
        if (Boolean.TRUE.equals(d.getMacDinh())) return;
        boMacDinhCu(idKh, id);
        d.setMacDinh(true);
        diaChiRepo.save(d);
    }

    @Transactional
    public void xoaDiaChi(Integer idKh, Integer id) {
        DiaChi d = tim(idKh, id);
        boolean laMacDinh = Boolean.TRUE.equals(d.getMacDinh());
        diaChiRepo.delete(d);
        diaChiRepo.flush();
        if (laMacDinh) {   // xóa địa chỉ mặc định thì chuyển mặc định sang địa chỉ còn lại đầu tiên
            diaChiRepo.findByIdKhachHangOrderByMacDinhDescIdAsc(idKh).stream().findFirst().ifPresent(first -> {
                first.setMacDinh(true);
                diaChiRepo.save(first);
            });
        }
    }

    // ---------------------------------------------------------------

    private KhachHang find(Integer id) {
        return khRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
    }

    // Luôn kiểm tra địa chỉ thuộc về đúng khách đang đăng nhập
    private DiaChi tim(Integer idKh, Integer id) {
        return diaChiRepo.findByIdAndIdKhachHang(id, idKh)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
    }

    private void boMacDinhCu(Integer idKh, Integer exceptId) {
        List<DiaChi> changed = new java.util.ArrayList<>();
        for (DiaChi x : diaChiRepo.findByIdKhachHangOrderByMacDinhDescIdAsc(idKh)) {
            if (Boolean.TRUE.equals(x.getMacDinh()) && !x.getId().equals(exceptId)) {
                x.setMacDinh(false);
                changed.add(x);
            }
        }
        diaChiRepo.saveAllAndFlush(changed);
    }

    private void apply(DiaChi d, DiaChiRequest r) {
        d.setTinhThanhPho(r.tinhThanhPho().trim());
        d.setQuanHuyen(r.quanHuyen().trim());
        d.setXaPhuong(r.xaPhuong().trim());
        d.setDuong(r.duong().trim());
    }

    private DiaChiResponse toResponse(DiaChi d) {
        return new DiaChiResponse(d.getId(), d.getTinhThanhPho(), d.getQuanHuyen(), d.getXaPhuong(),
                d.getDuong(), d.getMacDinh());
    }

    private TaiKhoanResponse toResponse(KhachHang k) {
        return new TaiKhoanResponse(k.getMaKhachHang(), k.getHoVaTen(), k.getGioiTinh(), k.getNgaySinh(),
                k.getSoDienThoai(), k.getEmail(), k.getNgayTao());
    }
}