import { useEffect, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { useAuth } from '../../context/AuthContext'
import { useCart } from '../../context/CartContext'
import { imgUrl, vnd } from '../../data/products'
import ProductImage from '../../components/user/ProductImage'

const PHONE = /^(0|\+84)\d{9}$/
const EMPTY = { tenNguoiNhan: '', sdtNguoiNhan: '', tinhThanhPho: '', quanHuyen: '', xaPhuong: '', duong: '', ghiChu: '' }

export default function ThanhToan() {
    const { user } = useAuth()
    const { items, canCheckout, clear, refresh } = useCart()
    const navigate = useNavigate()

    const [form, setForm] = useState(EMPTY)
    const [addresses, setAddresses] = useState([])
    const [saveAddr, setSaveAddr] = useState(true)
    const [codeInput, setCodeInput] = useState('')
    const [applied, setApplied] = useState('')
    const [codeMsg, setCodeMsg] = useState('')
    const [quote, setQuote] = useState(null)
    const [error, setError] = useState('')
    const [busy, setBusy] = useState(false)

    const payload = items.map(i => ({ idBienThe: i.id, soLuong: i.soLuong }))
    const key = JSON.stringify(payload) + '|' + applied

    // Điền sẵn họ tên, số điện thoại và địa chỉ mặc định
    useEffect(() => {
        if (!user) return
        let ignore = false
        Promise.all([api.get('/user/tai-khoan'), api.get('/user/dia-chi')])
            .then(([tk, dc]) => {
                if (ignore) return
                const def = dc.data[0]
                setAddresses(dc.data)
                setForm(f => ({
                    ...f,
                    tenNguoiNhan: tk.data.hoVaTen,
                    sdtNguoiNhan: tk.data.soDienThoai,
                    ...(def && { tinhThanhPho: def.tinhThanhPho, quanHuyen: def.quanHuyen, xaPhuong: def.xaPhuong, duong: def.duong }),
                }))
            })
            .catch(() => {})
        return () => { ignore = true }
    }, [user?.id])

    // Tính tiền ở server (giá, phí vận chuyển, mã giảm giá)
    useEffect(() => {
        if (!user || payload.length === 0) return
        let ignore = false
        api.post('/user/dat-hang/tinh-tien', { items: payload, maGiamGia: applied || null })
            .then(({ data }) => { if (!ignore) { setQuote(data); setError('') } })
            .catch(e => {
                if (ignore) return
                const msg = e.response?.data?.message ?? 'Không tính được đơn hàng'
                if (applied) { setApplied(''); setCodeMsg(msg) } else setError(msg)
            })
        return () => { ignore = true }
    }, [key, user?.id])

    if (!user) return <Navigate to="/dang-nhap" replace state={{ from: '/thanh-toan' }} />

    if (items.length === 0) {
        return (
            <div className="wrap">
                <h1 className="page-title">Thanh toán</h1>
                <p className="muted" style={{ marginBottom: 24 }}>Giỏ hàng của bạn đang trống.</p>
                <Link to="/" className="btn">Tiếp tục mua sắm</Link>
            </div>
        )
    }

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }))

    const pickAddress = e => {
        const a = addresses.find(x => String(x.id) === e.target.value)
        if (a) setForm(f => ({ ...f, tinhThanhPho: a.tinhThanhPho, quanHuyen: a.quanHuyen, xaPhuong: a.xaPhuong, duong: a.duong }))
    }

    const applyCode = () => { setCodeMsg(''); setApplied(codeInput.trim()) }
    const removeCode = () => { setCodeMsg(''); setCodeInput(''); setApplied('') }

    const submit = async e => {
        e.preventDefault()
        setError('')
        const phone = form.sdtNguoiNhan.replace(/\s+/g, '')
        if (!PHONE.test(phone)) return setError('Số điện thoại không hợp lệ')
        if (!canCheckout) return setError('Giỏ hàng có sản phẩm hết hàng hoặc không còn bán, vui lòng kiểm tra lại')

        setBusy(true)
        try {
            const { data } = await api.post('/user/dat-hang', {
                ...form, sdtNguoiNhan: phone, maGiamGia: applied || null,
                phuongThuc: 'COD', luuDiaChi: saveAddr, items: payload,
            })
            clear()
            navigate(`/dat-hang-thanh-cong/${data.maHoaDon}`, { replace: true, state: { tongTien: data.tongTien } })
        } catch (err) {
            setError(err.response?.data?.message ?? 'Không đặt được hàng. Kiểm tra backend đã chạy chưa.')
            refresh()
        } finally {
            setBusy(false)
        }
    }

    return (
        <div className="wrap">
            <h1 className="page-title">Thanh toán</h1>
            <form className="cart-grid" onSubmit={submit}>
                <div>
                    <h2 className="sec">Thông tin giao hàng</h2>
                    {addresses.length > 0 && (
                        <label className="field">Chọn địa chỉ đã lưu
                            <select onChange={pickAddress} defaultValue="">
                                <option value="" disabled>-- Chọn --</option>
                                {addresses.map(a => (
                                    <option key={a.id} value={a.id}>
                                        {a.duong}, {a.xaPhuong}, {a.quanHuyen}, {a.tinhThanhPho}{a.macDinh ? ' (mặc định)' : ''}
                                    </option>
                                ))}
                            </select>
                        </label>
                    )}
                    <div className="form-grid">
                        <label className="field">Họ tên người nhận
                            <input value={form.tenNguoiNhan} onChange={set('tenNguoiNhan')} required maxLength={100} />
                        </label>
                        <label className="field">Số điện thoại
                            <input value={form.sdtNguoiNhan} onChange={set('sdtNguoiNhan')} required maxLength={15} />
                        </label>
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
                        <label className="field wide">Ghi chú (không bắt buộc)
                            <input value={form.ghiChu} onChange={set('ghiChu')} maxLength={255} />
                        </label>
                    </div>
                    <label className="check">
                        <input type="checkbox" checked={saveAddr} onChange={e => setSaveAddr(e.target.checked)} /> Lưu địa chỉ này cho lần sau
                    </label>

                    <h2 className="sec">Phương thức thanh toán</h2>
                    <div className="pay">Thanh toán khi nhận hàng (COD)</div>
                    <p className="muted">Thanh toán trực tuyến sẽ được bổ sung sau.</p>
                </div>

                <aside className="summary">
                    <h2>Đơn hàng</h2>
                    {items.map(({ id, soLuong, d }) => d && (
                        <div className="mini" key={id}>
                            <div className="cthumb sm"><ProductImage src={imgUrl(d.anhUrl)} color={d.mau?.hex} alt="" /></div>
                            <div>
                                <div>{d.tenSanPham ?? 'Sản phẩm không còn bán'}</div>
                                {d.khaDung && <small className="muted">{d.mau.ten} / {d.size} × {soLuong}</small>}
                                {d.khaDung && d.tonKho < soLuong && <div className="warn">Chỉ còn {d.tonKho}</div>}
                            </div>
                            <span>{d.khaDung ? vnd(d.giaBan * soLuong) : ''}</span>
                        </div>
                    ))}

                    <div className="code-row">
                        <input placeholder="Mã giảm giá" value={codeInput} onChange={e => setCodeInput(e.target.value)} maxLength={20}
                               disabled={!!applied} />
                        {applied
                            ? <button type="button" className="btn ghost" onClick={removeCode}>Bỏ mã</button>
                            : <button type="button" className="btn ghost" onClick={applyCode} disabled={!codeInput.trim()}>Áp dụng</button>}
                    </div>
                    {codeMsg && <p className="warn">{codeMsg}</p>}
                    {quote?.maGiamGia && <p className="ok">Đã áp dụng mã {quote.maGiamGia}</p>}

                    {quote && (
                        <>
                            <div className="srow"><span>Tạm tính</span><span>{vnd(quote.tienTruocGiam)}</span></div>
                            {quote.tienGiam > 0 && <div className="srow"><span>Giảm giá</span><span>− {vnd(quote.tienGiam)}</span></div>}
                            <div className="srow"><span>Phí vận chuyển</span><span>{quote.phiGiaoHang > 0 ? vnd(quote.phiGiaoHang) : 'Miễn phí'}</span></div>
                            <div className="srow total"><span>Tổng cộng</span><span>{vnd(quote.tongTien)}</span></div>
                        </>
                    )}

                    {error && <p className="warn">{error}</p>}
                    <button className="btn full" disabled={busy || !canCheckout || !quote}>
                        {busy ? 'Đang đặt hàng...' : 'Đặt hàng'}
                    </button>
                    <p style={{ marginTop: 14 }}><Link to="/gio-hang" className="link">Quay lại giỏ hàng</Link></p>
                </aside>
            </form>
        </div>
    )
}