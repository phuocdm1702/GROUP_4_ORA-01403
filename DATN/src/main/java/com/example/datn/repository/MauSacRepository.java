package com.example.datn.repository;

import com.example.datn.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    boolean existsByMaMau(String maMau);
    boolean existsByMaMauAndIdNot(String maMau, Integer id);
}
