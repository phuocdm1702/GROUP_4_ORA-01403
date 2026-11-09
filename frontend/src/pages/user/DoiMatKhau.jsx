import { useState } from 'react'
import { App } from 'antd'
import api from '../../api/axios'

export default function DoiMatKhau() {
    const { message } = App.useApp()
    const [form, setForm] = useState({ matKhauCu: '', matKhauMoi: '', nhapLai: '' })
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const submit = async e => {
        e.preventDefault()
        setError('')
        if (form.matKhauMoi.length < 6) return setError('Mật khẩu mới tối thiểu 6 ký tự')
        if (form.matKhauMoi !== form.nhapLai) return setError('Mật khẩu nhập lại không khớp')
        if (form.matKhauMoi === form.matKhauCu) return setError('Mật khẩu mới phải khác mật khẩu hiện tại')

        setBusy(true)
        try {
            await api.put('/user/doi-mat-khau', { matKhauCu: form.matKhauCu, matKhauMoi: form.matKhauMoi })
            message.success('Đã đổi mật khẩu')
            setForm({ matKhauCu: '', matKhauMoi: '', nhapLai: '' })
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không đổi được mật khẩu')
        } finally {
            setBusy(false)
        }
    }

    return (
        <div>
            <h2 className="sec" style={{ marginTop: 0 }}>Đổi mật khẩu</h2>
            <form onSubmit={submit} style={{ maxWidth: 400 }}>
                <label className="field">Mật khẩu hiện tại
                    <input type="password" value={form.matKhauCu} onChange={set('matKhauCu')} required autoComplete="current-password" />
                </label>
                <label className="field">Mật khẩu mới
                    <input type="password" value={form.matKhauMoi} onChange={set('matKhauMoi')} required maxLength={72} autoComplete="new-password" />
                </label>
                <label className="field">Nhập lại mật khẩu mới
                    <input type="password" value={form.nhapLai} onChange={set('nhapLai')} required maxLength={72} autoComplete="new-password" />
                </label>
                {error && <p className="err">{error}</p>}
                <button className="btn" disabled={busy}>{busy ? 'Đang xử lý...' : 'Đổi mật khẩu'}</button>
            </form>
        </div>
    )
}