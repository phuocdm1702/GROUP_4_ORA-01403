package com.example.datn.repository;

import com.example.datn.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByIdKhachHangOrderByMacDinhDescIdAsc(Integer idKhachHang);

    Optional<DiaChi> findByIdAndIdKhachHang(Integer id, Integer idKhachHang);
    long countByIdKhachHang(Integer idKhachHang);
}
