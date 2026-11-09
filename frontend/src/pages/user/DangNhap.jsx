import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { useAuth } from '../../context/AuthContext'

export default function DangNhap() {
    const { login } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const [form, setForm] = useState({ tenDangNhap: '', matKhau: '' })
    const [remember, setRemember] = useState(true)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const submit = async e => {
        e.preventDefault()
        setError('')
        setBusy(true)
        try {
            const { data } = await api.post('/auth/khach-hang/dang-nhap', form)
            login(data, remember)
            navigate(location.state?.from ?? '/', { replace: true })
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không đăng nhập được. Kiểm tra backend đã chạy chưa.')
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="auth">
            <h1>Đăng nhập</h1>
            <form onSubmit={submit}>
                <label className="field">Số điện thoại hoặc email
                    <input value={form.tenDangNhap} onChange={set('tenDangNhap')} required autoComplete="username" />
                </label>
                <label className="field">Mật khẩu
                    <input type="password" value={form.matKhau} onChange={set('matKhau')} required autoComplete="current-password" />
                </label>
                <label className="check">
                    <input type="checkbox" checked={remember} onChange={e => setRemember(e.target.checked)} /> Duy trì đăng nhập
                </label>
                {error && <p className="err">{error}</p>}
                <button className="btn full" disabled={busy}>{busy ? 'Đang xử lý...' : 'Đăng nhập'}</button>
            </form>
            <p className="muted">Chưa có tài khoản? <Link to="/dang-ky" state={location.state} className="link">Đăng ký</Link></p>
        </div>
    )
}