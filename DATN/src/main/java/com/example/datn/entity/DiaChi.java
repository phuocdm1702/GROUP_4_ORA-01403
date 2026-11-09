package com.example.datn.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Dia_chi")
@Getter @Setter
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_khach_hang")
    private Integer idKhachHang;

    @Column(name = "tinh_thanh_pho")
    private String tinhThanhPho;

    @Column(name = "quan_huyen")
    private String quanHuyen;

    @Column(name = "xa_phuong")
    private String xaPhuong;

    @Column(name = "duong")
    private String duong;

    @Column(name = "mac_dinh")
    private Boolean macDinh;
}
