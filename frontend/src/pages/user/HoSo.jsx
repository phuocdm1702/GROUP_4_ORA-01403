import { useEffect, useState } from 'react'
import { App } from 'antd'
import dayjs from 'dayjs'
import api from '../../api/axios'
import { useAuth } from '../../context/AuthContext'

const PHONE = /^(0|\+84)\d{9}$/

export default function HoSo() {
    const { message } = App.useApp()
    const { rename } = useAuth()
    const [info, setInfo] = useState(null)
    const [form, setForm] = useState(null)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const fill = data => {
        setInfo(data)
        setForm({
            hoVaTen: data.hoVaTen ?? '', gioiTinh: data.gioiTinh ?? '', ngaySinh: data.ngaySinh ?? '',
            soDienThoai: data.soDienThoai ?? '', email: data.email ?? '',
        })
    }

    useEffect(() => {
        api.get('/user/tai-khoan').then(({ data }) => fill(data)).catch(() => setError('Không tải được thông tin tài khoản'))
    }, [])

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const submit = async e => {
        e.preventDefault()
        setError('')
        if (!PHONE.test(form.soDienThoai.replace(/\s+/g, ''))) return setError('Số điện thoại không hợp lệ')
        if (form.ngaySinh && dayjs(form.ngaySinh).isAfter(dayjs())) return setError('Ngày sinh không hợp lệ')

        setBusy(true)
        try {
            const { data } = await api.put('/user/tai-khoan', {
                ...form, gioiTinh: form.gioiTinh || null, ngaySinh: form.ngaySinh || null,
            })
            fill(data)
            rename(data.hoVaTen)
            message.success('Đã cập nhật hồ sơ')
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không cập nhật được hồ sơ')
        } finally {
            setBusy(false)
        }
    }

    if (!form) return error ? <p className="err">{error}</p> : <p className="muted">Đang tải...</p>

    return (
        <div>
            <h2 className="sec" style={{ marginTop: 0 }}>Hồ sơ của tôi</h2>
            <p className="muted" style={{ marginBottom: 20 }}>
                Mã khách hàng: <b>{info.maKhachHang}</b> · Tham gia từ {info.ngayTao ? dayjs(info.ngayTao).format('DD/MM/YYYY') : '—'}
            </p>
            <form onSubmit={submit} style={{ maxWidth: 560 }}>
                <div className="form-grid">
                    <label className="field wide">Họ và tên
                        <input value={form.hoVaTen} onChange={set('hoVaTen')} required maxLength={100} />
                    </label>
                    <label className="field">Giới tính
                        <select value={form.gioiTinh} onChange={set('gioiTinh')}>
                            <option value="">Không muốn nêu</option>
                            <option value="Nam">Nam</option>
                            <option value="Nữ">Nữ</option>
                            <option value="Khác">Khác</option>
                        </select>
                    </label>
                    <label className="field">Ngày sinh
                        <input type="date" value={form.ngaySinh} onChange={set('ngaySinh')} max={dayjs().format('YYYY-MM-DD')} />
                    </label>
                    <label className="field">Số điện thoại
                        <input value={form.soDienThoai} onChange={set('soDienThoai')} required maxLength={15} />
                    </label>
                    <label className="field">Email
                        <input type="email" value={form.email} onChange={set('email')} required maxLength={120} />
                    </label>
                </div>
                <p className="muted" style={{ marginBottom: 14 }}>Số điện thoại và email cũng là tên đăng nhập của bạn.</p>
                {error && <p className="err">{error}</p>}
                <button className="btn" disabled={busy}>{busy ? 'Đang lưu...' : 'Lưu thay đổi'}</button>
            </form>
        </div>
    )
}