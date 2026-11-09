package com.example.datn.controller;
import com.example.datn.dto.ThongKeDashboardResponse;
import com.example.datn.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/admin/thong-ke", "/api/thong-ke"})
@RequiredArgsConstructor
public class ThongKeController {
    private final ThongKeService thongKeService;

    @GetMapping
    public ResponseEntity<ThongKeDashboardResponse> getDashboard(
            @RequestParam(defaultValue = "ALL") String boLocThoiGian,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "NGAY") String thongKeTheo,
            @RequestParam(required = false) String tuKhoaSanPham,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(defaultValue = "BAN_CHAY") String loaiSapXepSanPham
    ) {
        return ResponseEntity.ok(thongKeService.getThongKe(
                boLocThoiGian, tuNgay, denNgay, thongKeTheo, tuKhoaSanPham, idChatLieu, loaiSapXepSanPham
        ));
    }
}
