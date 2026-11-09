package com.example.datn.dto;

import java.math.BigDecimal;
import java.util.List;

public record BoLocResponse(
        List<MauOpt> mau,
        List<Opt> kichCo,
        List<Opt> chatLieu,
        BigDecimal giaMin,
        BigDecimal giaMax
) {
    public record MauOpt(Integer id, String ma, String ten, String hex) {}

    public record Opt(Integer id, String ten) {}
}