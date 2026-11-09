package com.example.datn.repository;


import com.example.datn.entity.Anh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AnhRepository extends JpaRepository<Anh, Integer> {

    @Query("""
        select a from Anh a
          join fetch a.sanPham
          left join fetch a.mauSac
        where a.sanPham.id = :idAo and a.trangThai = true
        order by a.thuTu, a.id
        """)
    List<Anh> findByProduct(@Param("idAo") Integer idAo);

    @Query("""
        select a from Anh a
          join fetch a.sanPham
          left join fetch a.mauSac
        where a.trangThai = true
        order by a.sanPham.id, a.thuTu, a.id
        """)
    List<Anh> findAllActive();

    @Query("""
        select a from Anh a
          join fetch a.sanPham
          left join fetch a.mauSac
        where a.trangThai = true and a.sanPham.id in :ids
        order by a.sanPham.id, a.thuTu, a.id
        """)
    List<Anh> findByProductIds(@Param("ids") Collection<Integer> ids);
}
