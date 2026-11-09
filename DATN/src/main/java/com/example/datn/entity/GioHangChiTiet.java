package com.example.datn.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Gio_hang_chi_tiet")
@Getter @Setter
public class GioHangChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    @Column(name = "id_ao_chi_tiet")
    private Integer idAoChiTiet;

    @Column(name = "so_luong")
    private Integer soLuong;
}
