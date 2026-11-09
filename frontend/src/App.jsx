import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/admin/Login'
import AdminHome from './pages/admin/AdminHome'
import Dashboard from './pages/admin/Dashboard'
import Placeholder from './pages/admin/Placeholder'
import MauSac from './pages/admin/MauSac'
import SanPham from './pages/admin/SanPham'
import SanPhamForm from './pages/admin/SanPhamForm'
import NhanVien from './pages/admin/NhanVien'
import KhachHang from './pages/admin/KhachHang'
import AdminLayout from './components/admin/AdminLayout'
import ProtectedRoute from './components/admin/ProtectedRoute'
import UserLayout from './components/user/UserLayout'
import Home from './pages/user/Home'
import ProductDetail from './pages/user/ProductDetail'
import DangNhap from './pages/user/DangNhap'
import DangKy from './pages/user/DangKy'
import GioHang from './pages/user/GioHang'
import ThanhToan from './pages/user/ThanhToan'
import DatHangThanhCong from './pages/user/DatHangThanhCong'
import { ACCESS } from './auth/roles'
import DanhSachSanPham from './pages/user/DanhSachSanPham'
import TaiKhoanLayout from './pages/user/TaiKhoanLayout'
import HoSo from './pages/user/HoSo'
import SoDiaChi from './pages/user/SoDiaChi'
import DoiMatKhau from './pages/user/DoiMatKhau'
import DonHangCuaToi from './pages/user/DonHangCuaToi'
import ChiTietDonHang from './pages/user/ChiTietDonHang'
import HoaDon from './pages/admin/HoaDon'
import PhieuGiamGia from './pages/admin/PhieuGiamGia'
import DotGiamGia from './pages/admin/DotGiamGia'

// Bọc một trang admin bằng kiểm tra vai trò, lấy danh sách vai trò từ auth/roles.js
const guard = (path, element) => <ProtectedRoute roles={ACCESS[path]}>{element}</ProtectedRoute>

export default function App() {
    return (
        <Routes>
            {/* Khách hàng */}
            <Route element={<UserLayout />}>
                <Route path="/" element={<Home />} />
                <Route path="/san-pham" element={<DanhSachSanPham />} />
                <Route path="/san-pham/:id" element={<ProductDetail />} />
                <Route path="/dang-nhap" element={<DangNhap />} />
                <Route path="/dang-ky" element={<DangKy />} />
                <Route path="/gio-hang" element={<GioHang />} />
                <Route path="/thanh-toan" element={<ThanhToan />} />
                <Route path="/dat-hang-thanh-cong/:ma" element={<DatHangThanhCong />} />
                <Route path="/tai-khoan" element={<TaiKhoanLayout />}>
                    <Route index element={<HoSo />} />
                    <Route path="don-hang" element={<DonHangCuaToi />} />
                    <Route path="don-hang/:ma" element={<ChiTietDonHang />} />
                    <Route path="dia-chi" element={<SoDiaChi />} />
                    <Route path="mat-khau" element={<DoiMatKhau />} />
                </Route>
            </Route>

            {/* Admin */}
            <Route path="/admin/login" element={<Login />} />
            <Route path="/admin" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
                <Route index element={<AdminHome />} />
                <Route path="thong-ke" element={guard('/admin/thong-ke', <Dashboard />)} />
                <Route path="hoa-don" element={guard('/admin/hoa-don', <HoaDon />)} />
                <Route path="san-pham" element={guard('/admin/san-pham', <SanPham />)} />
                <Route path="san-pham/them" element={guard('/admin/san-pham', <SanPhamForm />)} />
                <Route path="san-pham/:id" element={guard('/admin/san-pham', <SanPhamForm />)} />
                <Route path="mau-sac" element={guard('/admin/mau-sac', <MauSac />)} />
                <Route path="kich-thuoc" element={guard('/admin/kich-thuoc', <Placeholder title="Kích thước" />)} />
                <Route path="chat-lieu" element={guard('/admin/chat-lieu', <Placeholder title="Chất liệu" />)} />
                <Route path="thuong-hieu" element={guard('/admin/thuong-hieu', <Placeholder title="Thương hiệu" />)} />
                <Route path="phieu-giam-gia" element={guard('/admin/phieu-giam-gia', <PhieuGiamGia />)} />
                <Route path="dot-giam-gia" element={guard('/admin/dot-giam-gia', <DotGiamGia />)} />
                <Route path="khach-hang" element={guard('/admin/khach-hang', <KhachHang />)} />
                <Route path="nhan-vien" element={guard('/admin/nhan-vien', <NhanVien />)} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    )
}