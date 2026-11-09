package com.example.datn.repository;

import com.example.datn.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    // ===== Trang bán hàng (đã có) =====
    @Query("""
        select v from SanPhamChiTiet v
          join fetch v.sanPham sp
          left join fetch sp.thuongHieu
          join fetch v.mauSac m
          join fetch v.kichThuoc k
          left join fetch v.chatLieu
        where v.trangThai = true and sp.trangThai = true
        order by sp.id, m.id, k.id
        """)
    List<SanPhamChiTiet> findAllActive();

    @Query("""
        select v from SanPhamChiTiet v
          join fetch v.sanPham sp
          left join fetch sp.thuongHieu
          join fetch v.mauSac m
          join fetch v.kichThuoc k
          left join fetch v.chatLieu
        where v.trangThai = true and sp.trangThai = true and sp.id = :idAo
        order by m.id, k.id
        """)
    List<SanPhamChiTiet> findActiveByProduct(@Param("idAo") Integer idAo);

    // ===== Trang quản trị (mới): lấy cả biến thể đã ngừng bán =====
    @Query("""
        select v from SanPhamChiTiet v
          join fetch v.sanPham sp
          join fetch v.mauSac m
          join fetch v.kichThuoc k
          left join fetch v.chatLieu
        order by sp.id, m.id, k.id
        """)
    List<SanPhamChiTiet> findAllForAdmin();

    @Query("""
        select v from SanPhamChiTiet v
          join fetch v.sanPham sp
          join fetch v.mauSac m
          join fetch v.kichThuoc k
          left join fetch v.chatLieu
        where sp.id = :idAo
        order by m.id, k.id
        """)
    List<SanPhamChiTiet> findAllByProductId(@Param("idAo") Integer idAo);

    @Query("""
        select v from SanPhamChiTiet v
          join fetch v.sanPham sp
          join fetch v.mauSac
          join fetch v.kichThuoc
        where v.id in :ids
        """)
    List<SanPhamChiTiet> findAllByIdWithDetails(@Param("ids") Collection<Integer> ids);

    // Trừ tồn kho, chỉ thành công khi còn đủ hàng. Trả về 0 nếu không đủ.
    @Modifying
    @Query("update SanPhamChiTiet v set v.soLuong = v.soLuong - :q " +
            "where v.id = :id and v.soLuong >= :q and v.trangThai = true")
    int tryDeduct(@Param("id") Integer id, @Param("q") int q);

    // Cộng trả tồn kho khi hủy đơn
    @Modifying
    @Query("update SanPhamChiTiet v set v.soLuong = v.soLuong + :q where v.id = :id")
    int hoanKho(@Param("id") Integer id, @Param("q") int q);
}