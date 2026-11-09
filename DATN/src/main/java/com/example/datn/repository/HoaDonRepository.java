package com.example.datn.repository;


import com.example.datn.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    boolean existsByMaHoaDon(String maHoaDon);

    Page<HoaDon> findByKhachHang_Id(Integer idKhachHang, Pageable pageable);

    Page<HoaDon> findByKhachHang_IdAndTrangThai(Integer idKhachHang, String trangThai, Pageable pageable);

    Optional<HoaDon> findByMaHoaDonAndKhachHang_Id(String maHoaDon, Integer idKhachHang);

    // Đổi trạng thái chỉ khi đơn đang ở trạng thái "cu". Trả về 0 nếu đơn đã bị đổi bởi người khác.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update HoaDon h set h.trangThai = :moi where h.id = :id and h.trangThai = :cu")
    int doiTrangThai(@Param("id") Integer id, @Param("cu") String cu, @Param("moi") String moi);

    // Bộ lọc Quản lý Hóa đơn cho Admin
    // Tận dụng Index IX_HoaDon_NgayTao và IX_HoaDon_TrangThai
    @Query("""
        SELECT h FROM HoaDon h
        LEFT JOIN FETCH h.khachHang kh
        LEFT JOIN FETCH h.nhanVien nv
        LEFT JOIN FETCH h.giamGia gg
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(h.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(h.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR h.sdtNguoiNhan LIKE CONCAT('%', :keyword, '%'))
          AND (:trangThai IS NULL OR :trangThai = '' OR h.trangThai = :trangThai)
          AND (:trangThaiThanhToan IS NULL OR :trangThaiThanhToan = '' OR h.trangThaiThanhToan = :trangThaiThanhToan)
          AND (:hinhThucMuaHang IS NULL OR :hinhThucMuaHang = '' OR h.hinhThucMuaHang = :hinhThucMuaHang)
          AND (:tuNgay IS NULL OR h.ngayTao >= :tuNgay)
          AND (:denNgay IS NULL OR h.ngayTao < :denNgay)
        ORDER BY h.ngayTao DESC, h.id DESC
    """)
    List<HoaDon> searchAdminOrders(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("trangThaiThanhToan") String trangThaiThanhToan,
            @Param("hinhThucMuaHang") String hinhThucMuaHang,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay
    );

    @Query("""
        SELECT h FROM HoaDon h
        LEFT JOIN FETCH h.khachHang kh
        LEFT JOIN FETCH h.nhanVien nv
        LEFT JOIN FETCH h.giamGia gg
        WHERE h.id = :id
    """)
    Optional<HoaDon> findDetailById(@Param("id") Integer id);
}