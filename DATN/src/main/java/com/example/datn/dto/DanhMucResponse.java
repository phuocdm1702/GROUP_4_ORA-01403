package com.example.datn.dto;

import com.example.datn.entity.ChatLieu;
import com.example.datn.entity.KichThuoc;
import com.example.datn.entity.MauSac;

import java.util.List;

public record DanhMucResponse(
        List<MauSac> mauSac,
        List<KichThuoc> kichThuoc,
        List<ChatLieu> chatLieu
) {}
