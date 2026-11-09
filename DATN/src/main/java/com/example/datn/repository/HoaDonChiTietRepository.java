package com.example.datn.repository;

import com.example.datn.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    @Query("""
        select c from HoaDonChiTiet c
          join fetch c.hoaDon
          join fetch c.sanPhamChiTiet v
          join fetch v.sanPham
          join fetch v.mauSac
          join fetch v.kichThuoc
        where c.hoaDon.id in :ids
        order by c.id
        """)
    List<HoaDonChiTiet> findByOrderIds(@Param("ids") Collection<Integer> ids);

    // Tổng số lượng đã bán theo sản phẩm, không tính đơn đã hủy
    @Query("""
        select v.sanPham.id, sum(c.soLuong)
        from HoaDonChiTiet c
          join c.sanPhamChiTiet v
          join c.hoaDon h
        where h.trangThai <> :huy
        group by v.sanPham.id
        """)
    List<Object[]> daBanTheoSanPham(@Param("huy") String huy);
}