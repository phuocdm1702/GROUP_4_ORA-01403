package com.example.datn.repository;

import com.example.datn.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    boolean existsByMaAo(String maAo);
    boolean existsByMaAoAndIdNot(String maAo, Integer id);

    @Query("select sp from SanPham sp left join fetch sp.thuongHieu order by sp.id desc")
    List<SanPham> findAllForAdmin();
}
