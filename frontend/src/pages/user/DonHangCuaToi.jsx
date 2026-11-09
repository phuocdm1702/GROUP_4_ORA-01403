import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import dayjs from 'dayjs'
import api from '../../api/axios'
import { imgUrl, vnd } from '../../data/products'
import ProductImage from '../../components/user/ProductImage'

export const STATUS_COLOR = {
    'Chờ xác nhận': 'orange', 'Đã xác nhận': 'blue', 'Đang giao hàng': 'indigo',
    'Đã hoàn thành': 'green', 'Đã hủy': 'red',
}

const TABS = [
    { v: '', l: 'Tất cả' },
    { v: 'Chờ xác nhận', l: 'Chờ xác nhận' },
    { v: 'Đã xác nhận', l: 'Đã xác nhận' },
    { v: 'Đang giao hàng', l: 'Đang giao' },
    { v: 'Đã hoàn thành', l: 'Hoàn thành' },
    { v: 'Đã hủy', l: 'Đã hủy' },
]

export default function DonHangCuaToi() {
    const [sp, setSp] = useSearchParams()
    const trangThai = sp.get('tt') ?? ''
    const trang = Number(sp.get('trang')) || 1
    const [data, setData] = useState(null)
    const [error, setError] = useState('')

    useEffect(() => {
        let ignore = false
        api.get('/user/don-hang', { params: { trangThai: trangThai || undefined, trang, kichThuoc: 6 } })
            .then(({ data }) => { if (!ignore) { setData(data); setError('') } })
            .catch(() => { if (!ignore) setError('Không tải được danh sách đơn hàng') })
        return () => { ignore = true }
    }, [trangThai, trang])

    const goTab = v => setSp(v ? { tt: v } : {})
    const goPage = p => {
        const next = new URLSearchParams(sp)
        if (p > 1) next.set('trang', p); else next.delete('trang')
        setSp(next)
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    return (
        <div>
            <h2 className="sec" style={{ marginTop: 0 }}>Đơn hàng của tôi</h2>
            <div className="tabs">
                {TABS.map(t => (
                    <button key={t.v} className={`tab ${trangThai === t.v ? 'on' : ''}`} onClick={() => goTab(t.v)}>{t.l}</button>
                ))}
            </div>

            {error && <p className="err">{error}</p>}
            {!data && !error && <p className="muted">Đang tải...</p>}

            {data && data.items.length === 0 && (
                <div className="empty">
                    <p>{trangThai ? 'Không có đơn hàng nào ở trạng thái này.' : 'Bạn chưa có đơn hàng nào.'}</p>
                    <Link to="/san-pham" className="btn">Mua sắm ngay</Link>
                </div>
            )}

            {data?.items.map(o => (
                <Link key={o.maHoaDon} to={`/tai-khoan/don-hang/${o.maHoaDon}`} className="order">
                    <div className="cthumb sm"><ProductImage src={imgUrl(o.anhUrl)} color={o.hexDau} alt="" /></div>
                    <div>
                        <div><b>{o.maHoaDon}</b> <span className={`badge ${STATUS_COLOR[o.trangThai] ?? ''}`}>{o.trangThai}</span></div>
                        <div className="muted">{o.tenSanPhamDau}{o.soDong > 1 ? ` và ${o.soDong - 1} sản phẩm khác` : ''} · {o.soSanPham} sản phẩm</div>
                        <div className="muted">{dayjs(o.ngayTao).format('DD/MM/YYYY HH:mm')}</div>
                    </div>
                    <b>{vnd(o.tongTien)}</b>
                </Link>
            ))}

            {data && data.tongTrang > 1 && (
                <div className="pager">
                    <button className="pg" disabled={data.trang <= 1} onClick={() => goPage(data.trang - 1)}>‹</button>
                    {Array.from({ length: data.tongTrang }, (_, i) => i + 1).map(p => (
                        <button key={p} className={`pg ${p === data.trang ? 'on' : ''}`} onClick={() => goPage(p)}>{p}</button>
                    ))}
                    <button className="pg" disabled={data.trang >= data.tongTrang} onClick={() => goPage(data.trang + 1)}>›</button>
                </div>
            )}
        </div>
    )
}