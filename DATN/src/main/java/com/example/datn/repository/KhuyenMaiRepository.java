package com.example.datn.repository;

import com.example.datn.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {

    boolean existsByMaKhuyenMaiIgnoreCase(String maKhuyenMai);

    @Query("""
        SELECT k FROM KhuyenMai k
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(k.maKhuyenMai) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(k.tenKhuyenMai) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:tuNgay IS NULL OR k.ngayBatDau >= :tuNgay)
          AND (:denNgay IS NULL OR k.ngayKetThuc <= :denNgay)
        ORDER BY k.id DESC
    """)
    List<KhuyenMai> searchDotGiamGia(
            @Param("keyword") String keyword,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );
}
