import { NavLink, Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const LINKS = [
    { to: '/tai-khoan', label: 'Hồ sơ của tôi', end: true },
    { to: '/tai-khoan/don-hang', label: 'Đơn hàng của tôi' },
    { to: '/tai-khoan/dia-chi', label: 'Sổ địa chỉ' },
    { to: '/tai-khoan/mat-khau', label: 'Đổi mật khẩu' },
]

export default function TaiKhoanLayout() {
    const { user, logout } = useAuth()
    const location = useLocation()
    if (!user) return <Navigate to="/dang-nhap" replace state={{ from: location.pathname }} />

    return (
        <div className="wrap">
            <h1 className="page-title">Tài khoản của {user.name.split(' ').pop()}</h1>
            <div className="acc">
                <nav className="acc-nav">
                    {LINKS.map(l => <NavLink key={l.to} to={l.to} end={l.end}>{l.label}</NavLink>)}
                    <button className="rm" onClick={logout}>Đăng xuất</button>
                </nav>
                <div><Outlet /></div>
            </div>
        </div>
    )
}