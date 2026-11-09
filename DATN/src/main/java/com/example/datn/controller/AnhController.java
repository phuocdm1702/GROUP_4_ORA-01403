package com.example.datn.controller;

import com.example.datn.dto.AnhResponse;
import com.example.datn.service.AnhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AnhController {
    private final AnhService service;

    @GetMapping("/api/admin/san-pham/{id}/anh")
    public List<AnhResponse> list(@PathVariable Integer id) {
        return service.list(id);
    }

    @PostMapping(value = "/api/admin/san-pham/{id}/anh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnhResponse upload(@PathVariable Integer id,
                              @RequestParam(required = false) Integer idMau,   // bỏ trống = ảnh chung
                              @RequestParam("file") MultipartFile file) {
        return service.upload(id, idMau, file);
    }

    @PutMapping("/api/admin/anh/{id}/chinh")
    public void setPrimary(@PathVariable Integer id) {
        service.setPrimary(id);
    }

    @DeleteMapping("/api/admin/anh/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
