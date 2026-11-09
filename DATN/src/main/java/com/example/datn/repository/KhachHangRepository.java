package com.example.datn.repository;

import com.example.datn.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    List<KhachHang> findAllByOrderByIdDesc();

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    interface ThongKe {
        Integer getIdKhachHang();
        Integer getSoDon();
        BigDecimal getTongChiTieu();
    }

    interface DonGanDay {
        String getMaHoaDon();
        LocalDateTime getNgayTao();
        BigDecimal getTongTien();
        String getTrangThai();
    }

    // Số đơn (mọi trạng thái) và tổng chi tiêu (chỉ đơn đã hoàn thành) của từng khách
    @Query(value = """
            select id_khach_hang as idKhachHang,
                   count(*) as soDon,
                   coalesce(sum(case when trang_thai = :done then tong_tien else 0 end), 0) as tongChiTieu
            from Hoa_don
            where id_khach_hang is not null
            group by id_khach_hang
            """, nativeQuery = true)
    List<ThongKe> thongKe(@Param("done") String done);

    @Query(value = """
            select top 5 ma_hoa_don as maHoaDon, ngay_tao as ngayTao,
                   tong_tien as tongTien, trang_thai as trangThai
            from Hoa_don
            where id_khach_hang = :id
            order by ngay_tao desc, id desc
            """, nativeQuery = true)
    List<DonGanDay> donGanDay(@Param("id") Integer id);

    @Query("select k from KhachHang k where k.soDienThoai = :key or lower(k.email) = lower(:key)")
    List<KhachHang> findForLogin(@Param("key") String key);
}