import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { useAuth } from '../../context/AuthContext'

const PHONE = /^(0|\+84)\d{9}$/

export default function DangKy() {
    const { login } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const [form, setForm] = useState({ hoVaTen: '', soDienThoai: '', email: '', matKhau: '', nhapLai: '' })
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const submit = async e => {
        e.preventDefault()
        setError('')
        if (!PHONE.test(form.soDienThoai.replace(/\s+/g, ''))) return setError('Số điện thoại không hợp lệ')
        if (form.matKhau.length < 6) return setError('Mật khẩu tối thiểu 6 ký tự')
        if (form.matKhau !== form.nhapLai) return setError('Mật khẩu nhập lại không khớp')

        setBusy(true)
        try {
            const { nhapLai, ...body } = form
            const { data } = await api.post('/auth/khach-hang/dang-ky', body)
            login(data, true)
            navigate(location.state?.from ?? '/', { replace: true })
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không đăng ký được. Kiểm tra backend đã chạy chưa.')
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="auth">
            <h1>Đăng ký</h1>
            <form onSubmit={submit}>
                <label className="field">Họ và tên
                    <input value={form.hoVaTen} onChange={set('hoVaTen')} required maxLength={100} autoComplete="name" />
                </label>
                <label className="field">Số điện thoại
                    <input value={form.soDienThoai} onChange={set('soDienThoai')} required maxLength={15} autoComplete="tel" />
                </label>
                <label className="field">Email
                    <input type="email" value={form.email} onChange={set('email')} required maxLength={120} autoComplete="email" />
                </label>
                <label className="field">Mật khẩu
                    <input type="password" value={form.matKhau} onChange={set('matKhau')} required maxLength={72} autoComplete="new-password" />
                </label>
                <label className="field">Nhập lại mật khẩu
                    <input type="password" value={form.nhapLai} onChange={set('nhapLai')} required maxLength={72} autoComplete="new-password" />
                </label>
                {error && <p className="err">{error}</p>}
                <button className="btn full" disabled={busy}>{busy ? 'Đang xử lý...' : 'Tạo tài khoản'}</button>
            </form>
            <p className="muted">Đã có tài khoản? <Link to="/dang-nhap" state={location.state} className="link">Đăng nhập</Link></p>
        </div>
    )
}