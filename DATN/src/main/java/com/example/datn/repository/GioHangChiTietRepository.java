package com.example.datn.repository;

import com.example.datn.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {
    List<GioHangChiTiet> findByIdKhachHang(Integer idKhachHang);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from GioHangChiTiet g where g.idKhachHang = :id")
    void deleteByKhachHang(@Param("id") Integer id);
}
