package com.example.datn.repository;

import com.example.datn.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    @Query("select n from NhanVien n join fetch n.vaiTro order by n.id desc")
    List<NhanVien> findAllWithRole();

    @Query("select n from NhanVien n join fetch n.vaiTro where n.id = :id")
    Optional<NhanVien> findByIdWithRole(@Param("id") Integer id);

    @Query("select n from NhanVien n join fetch n.vaiTro " +
            "where lower(n.tenNguoiDung) = lower(:key) or n.soDienThoai = :key")
    List<NhanVien> findForLogin(@Param("key") String key);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);
    boolean existsByTenNguoiDungIgnoreCaseAndIdNot(String tenNguoiDung, Integer id);
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    long countByVaiTro_TenVaiTroAndTrangThai(String tenVaiTro, Boolean trangThai);
}
