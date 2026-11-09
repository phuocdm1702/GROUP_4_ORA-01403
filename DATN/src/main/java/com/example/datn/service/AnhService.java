package com.example.datn.service;

import com.example.datn.dto.AnhResponse;
import com.example.datn.entity.Anh;
import com.example.datn.entity.MauSac;
import com.example.datn.entity.SanPham;
import com.example.datn.repository.AnhRepository;
import com.example.datn.repository.MauSacRepository;
import com.example.datn.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnhService {
    private static final int MAX_PER_GROUP = 8;
    private static final String URL_PREFIX = "/uploads/";

    private final AnhRepository anhRepo;
    private final SanPhamRepository spRepo;
    private final MauSacRepository mauRepo;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<AnhResponse> list(Integer idAo) {
        return anhRepo.findByProduct(idAo).stream().map(this::toResponse).toList();
    }

    @Transactional
    public AnhResponse upload(Integer idAo, Integer idMau, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new RuntimeException("Chưa chọn file ảnh");
        SanPham sp = spRepo.findById(idAo).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        MauSac mau = idMau == null ? null
                : mauRepo.findById(idMau).orElseThrow(() -> new RuntimeException("Màu không tồn tại"));

        List<Anh> group = sameGroup(idAo, idMau);
        if (group.size() >= MAX_PER_GROUP)
            throw new RuntimeException("Mỗi màu tối đa " + MAX_PER_GROUP + " ảnh");

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được file ảnh");
        }
        // Kiểm tra theo nội dung file, không tin vào phần đuôi hay Content-Type do client gửi
        String ext = detectExt(bytes);
        if (ext == null) throw new RuntimeException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP");

        String name = UUID.randomUUID() + ext;     // tên ngẫu nhiên: không trùng, không lộ tên gốc
        try {
            Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Files.write(dir.resolve(name), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Không lưu được file ảnh");
        }

        Anh a = new Anh();
        a.setSanPham(sp);
        a.setMauSac(mau);
        a.setAnhUrl(URL_PREFIX + name);
        a.setThuTu(group.stream().mapToInt(x -> x.getThuTu() == null ? 0 : x.getThuTu()).max().orElse(-1) + 1);
        a.setTrangThai(true);
        return toResponse(anhRepo.save(a));
    }

    // Đưa ảnh được chọn lên đầu nhóm (thành ảnh chính)
    @Transactional
    public void setPrimary(Integer id) {
        Anh chosen = anhRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
        List<Anh> group = sameGroup(chosen.getSanPham().getId(),
                chosen.getMauSac() == null ? null : chosen.getMauSac().getId());

        List<Anh> ordered = new ArrayList<>();
        group.stream().filter(a -> a.getId().equals(id)).findFirst().ifPresent(ordered::add);
        group.stream().filter(a -> !a.getId().equals(id)).forEach(ordered::add);
        for (int i = 0; i < ordered.size(); i++) ordered.get(i).setThuTu(i);
        anhRepo.saveAll(ordered);
    }

    @Transactional
    public void delete(Integer id) {
        Anh a = anhRepo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
        String url = a.getAnhUrl();
        anhRepo.delete(a);
        deleteFile(url);
    }

    // ---------------------------------------------------------------

    private List<Anh> sameGroup(Integer idAo, Integer idMau) {
        return anhRepo.findByProduct(idAo).stream()
                .filter(a -> Objects.equals(a.getMauSac() == null ? null : a.getMauSac().getId(), idMau))
                .toList();
    }

    private AnhResponse toResponse(Anh a) {
        return new AnhResponse(a.getId(),
                a.getMauSac() == null ? null : a.getMauSac().getId(),
                a.getAnhUrl(), a.getThuTu());
    }

    private void deleteFile(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) return;   // bỏ qua URL ngoài
        try {
            String fileName = Path.of(url.substring(URL_PREFIX.length())).getFileName().toString();
            Files.deleteIfExists(Path.of(uploadDir).toAbsolutePath().normalize().resolve(fileName));
        } catch (Exception ignored) {
            // file không xóa được thì bỏ qua, dòng trong database đã xóa
        }
    }

    private String detectExt(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return ".jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return ".png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return ".webp";
        return null;
    }
}
