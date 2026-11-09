import { useEffect, useState } from 'react'
import { App } from 'antd'
import api from '../../api/axios'

const EMPTY = { tinhThanhPho: '', quanHuyen: '', xaPhuong: '', duong: '', macDinh: false }

export default function SoDiaChi() {
    const { message, modal } = App.useApp()
    const [list, setList] = useState(null)
    const [form, setForm] = useState(null)        // null = đang đóng; có id = sửa; không có id = thêm
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)
    const [tick, setTick] = useState(0)

    useEffect(() => {
        api.get('/user/dia-chi').then(({ data }) => setList(data)).catch(() => message.error('Không tải được địa chỉ'))
    }, [tick])

    const reload = () => setTick(t => t + 1)
    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const open = d => {
        setError('')
        setForm(d ? { ...d } : { ...EMPTY, macDinh: list?.length === 0 })
    }

    const submit = async e => {
        e.preventDefault()
        setError('')
        setBusy(true)
        const body = {
            tinhThanhPho: form.tinhThanhPho, quanHuyen: form.quanHuyen,
            xaPhuong: form.xaPhuong, duong: form.duong, macDinh: !!form.macDinh,
        }
        try {
            if (form.id) await api.put(`/user/dia-chi/${form.id}`, body)
            else await api.post('/user/dia-chi', body)
            message.success(form.id ? 'Đã cập nhật địa chỉ' : 'Đã thêm địa chỉ')
            setForm(null)
            reload()
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không lưu được địa chỉ')
        } finally {
            setBusy(false)
        }
    }

    const makeDefault = async id => {
        try { await api.put(`/user/dia-chi/${id}/mac-dinh`); reload() }
        catch (err) { message.error(err.response?.data?.message ?? 'Không đặt được mặc định') }
    }

    const remove = d => modal.confirm({
        title: 'Xóa địa chỉ này?',
        content: `${d.duong}, ${d.xaPhuong}, ${d.quanHuyen}, ${d.tinhThanhPho}`,
        okText: 'Xóa', cancelText: 'Giữ lại', okButtonProps: { danger: true },
        onOk: async () => {
            try { await api.delete(`/user/dia-chi/${d.id}`); message.success('Đã xóa địa chỉ'); reload() }
            catch (err) { message.error(err.response?.data?.message ?? 'Không xóa được địa chỉ') }
        },
    })

    if (!list) return <p className="muted">Đang tải...</p>

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2 className="sec" style={{ margin: 0 }}>Sổ địa chỉ ({list.length}/10)</h2>
                {!form && <button className="btn" onClick={() => open(null)} disabled={list.length >= 10}>Thêm địa chỉ</button>}
            </div>

            {form && (
                <form onSubmit={submit} className="addr-form">
                    <h3 style={{ margin: '0 0 14px', fontWeight: 500 }}>{form.id ? 'Sửa địa chỉ' : 'Địa chỉ mới'}</h3>
                    <div className="form-grid">
                        <label className="field">Tỉnh / Thành phố
                            <input value={form.tinhThanhPho} onChange={set('tinhThanhPho')} required maxLength={45} />
                        </label>
                        <label className="field">Quận / Huyện
                            <input value={form.quanHuyen} onChange={set('quanHuyen')} required maxLength={45} />
                        </label>
                        <label className="field">Phường / Xã
                            <input value={form.xaPhuong} onChange={set('xaPhuong')} required maxLength={45} />
                        </label>
                        <label className="field">Số nhà, tên đường
                            <input value={form.duong} onChange={set('duong')} required maxLength={100} />
                        </label>
                    </div>
                    <label className="check">
                        <input type="checkbox" checked={!!form.macDinh} disabled={form.id && form.macDinh}
                               onChange={e => setForm(f => ({ ...f, macDinh: e.target.checked }))} /> Đặt làm địa chỉ mặc định
                    </label>
                    {error && <p className="err">{error}</p>}
                    <div style={{ display: 'flex', gap: 10 }}>
                        <button className="btn" disabled={busy}>{busy ? 'Đang lưu...' : 'Lưu địa chỉ'}</button>
                        <button type="button" className="btn ghost" onClick={() => setForm(null)}>Hủy</button>
                    </div>
                </form>
            )}

            {list.length === 0 && !form && <p className="muted" style={{ marginTop: 20 }}>Bạn chưa có địa chỉ nào.</p>}

            <div style={{ marginTop: 20 }}>
                {list.map(d => (
                    <div className="addr" key={d.id}>
                        <div>
                            <div>{d.duong}, {d.xaPhuong}, {d.quanHuyen}, {d.tinhThanhPho}</div>
                            {d.macDinh && <span className="badge blue">Mặc định</span>}
                        </div>
                        <div className="addr-act">
                            {!d.macDinh && <button className="rm" onClick={() => makeDefault(d.id)}>Đặt mặc định</button>}
                            <button className="rm" onClick={() => open(d)}>Sửa</button>
                            <button className="rm" onClick={() => remove(d)}>Xóa</button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}