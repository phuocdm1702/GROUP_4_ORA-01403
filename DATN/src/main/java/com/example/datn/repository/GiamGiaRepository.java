package com.example.datn.repository;


import com.example.datn.entity.GiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GiamGiaRepository extends JpaRepository<GiamGia, Integer> {
    Optional<GiamGia> findByMaGiamGiaIgnoreCase(String maGiamGia);

    // Tăng số lượt dùng, chỉ thành công khi còn lượt (an toàn khi nhiều người dùng cùng lúc)
    @Modifying
    @Query("update GiamGia g set g.soLuongDaDung = g.soLuongDaDung + 1 " +
            "where g.id = :id and g.soLuongDaDung < g.soLuong")
    int tryUse(@Param("id") Integer id);

    // Trả lại một lượt dùng mã khi hủy đơn
    @Modifying
    @Query("update GiamGia g set g.soLuongDaDung = g.soLuongDaDung - 1 " +
            "where g.id = :id and g.soLuongDaDung > 0")
    int hoanLuot(@Param("id") Integer id);

    // Bộ lọc màn hình Quản lý Phiếu giảm giá
    @Query("""
        SELECT g FROM GiamGia g
        WHERE (:keyword IS NULL OR :keyword = '' 
               OR LOWER(g.maGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(g.tenGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:loaiGiamGia IS NULL OR :loaiGiamGia = '' OR g.loaiGiamGia = :loaiGiamGia)
          AND (:tuNgay IS NULL OR g.ngayBatDau >= :tuNgay)
          AND (:denNgay IS NULL OR g.ngayKetThuc <= :denNgay)
        ORDER BY g.id DESC
    """)
    List<GiamGia> searchPhieuGiamGia(
            @Param("keyword") String keyword,
            @Param("loaiGiamGia") String loaiGiamGia,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    // Lấy danh sách các voucher đang có thể áp dụng cho đơn hàng có tổng tiền hàng = :tienTruocGiam
    @Query("""
        SELECT g FROM GiamGia g
        WHERE g.trangThai = true
          AND g.soLuongDaDung < g.soLuong
          AND :today BETWEEN g.ngayBatDau AND g.ngayKetThuc
          AND g.giaTriToiThieu <= :tienTruocGiam
    """)
    List<GiamGia> findAvailableVouchers(
            @Param("tienTruocGiam") BigDecimal tienTruocGiam,
            @Param("today") LocalDate today
    );

    // Spring Data JPA tự động sinh câu lệnh kiểm tra tồn tại theo tên hàm này
    boolean existsByMaGiamGiaIgnoreCase(String ma);
}
