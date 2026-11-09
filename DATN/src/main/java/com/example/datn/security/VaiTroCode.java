package com.example.datn.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Đổi tên vai trò trong bảng Vai_tro thành mã quyền. Nếu đổi tên vai trò trong database thì sửa ở đây.
public final class VaiTroCode {
    private VaiTroCode() {}

    private static final Map<String, String> MAP = Map.of(
            "Quản trị viên", "ADMIN",
            "Quản lý", "MANAGER",
            "Nhân viên bán hàng", "SALES",
            "Nhân viên kho", "WAREHOUSE",
            "Nhân viên chăm sóc khách hàng", "SUPPORT");

    public static List<String> rolesOf(String tenVaiTro) {
        List<String> roles = new ArrayList<>();
        roles.add("STAFF");                       // mọi nhân viên đều có
        String code = MAP.get(tenVaiTro);
        if (code != null) roles.add(code);
        return roles;
    }
}
