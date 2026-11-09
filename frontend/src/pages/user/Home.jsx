import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CarOutlined, SwapOutlined, WalletOutlined, ClockCircleOutlined } from '@ant-design/icons'
import { fetchFilters, searchProducts } from '../../api/storeApi'
import ProductCard from '../../components/user/ProductCard'
import { ProductSkeletons } from '../../components/user/Skeleton'
import Shirt from '../../components/user/Shirt'

// Dùng khi chưa tải được màu từ server
const FALLBACK_COLORS = [
    { id: null, ten: 'Xanh navy', hex: '#1f2a44' },
    { id: null, ten: 'Trắng', hex: '#ffffff' },
    { id: null, ten: 'Đen', hex: '#000000' },
    { id: null, ten: 'Đỏ đô', hex: '#800020' },
    { id: null, ten: 'Xám', hex: '#808080' },
]

const FACTS = [
    { icon: <CarOutlined />, title: 'Miễn phí vận chuyển', sub: 'Cho đơn từ 500.000 ₫' },
    { icon: <ClockCircleOutlined />, title: 'Giao hàng 2 đến 4 ngày', sub: 'Toàn quốc' },
    { icon: <SwapOutlined />, title: 'Đổi trả trong 7 ngày', sub: 'Liên hệ cửa hàng để được hỗ trợ' },
    { icon: <WalletOutlined />, title: 'Thanh toán khi nhận hàng', sub: 'Kiểm tra hàng rồi mới trả tiền' },
]

// Sửa danh sách này theo sản phẩm thực tế. "q" là từ khóa tìm kiếm khi bấm vào ô (không cần gõ dấu).
const TILES = [
    { title: 'Cổ điển', sub: 'Vải pique đứng form', q: 'classic', bg: '#1f2a44', shirt: '#3d517f' },
    { title: 'Dáng ôm', sub: 'Gọn gàng, ôm nhẹ', q: 'slim fit', bg: '#3b4350', shirt: '#6b7686' },
    { title: 'Thể thao', sub: 'Nhẹ, nhanh khô', q: 'the thao', bg: '#6f2332', shirt: '#a24556' },
    { title: 'Thoáng khí', sub: 'Mặc thoải mái hằng ngày', q: 'thoang khi', bg: '#2f4a3c', shirt: '#5c8571' },
]

const TABS = [
    { key: 'moi', label: 'Mới nhất', params: { sapXep: 'moi' }, all: '/san-pham' },
    { key: 'best', label: 'Bán chạy', params: { sapXep: 'ban-chay' }, all: '/san-pham?sx=ban-chay' },
    { key: 'sale', label: 'Đang giảm giá', params: { sapXep: 'moi', khuyenMai: true }, all: '/san-pham?km=1' },
]

export default function Home() {
    const [colors, setColors] = useState(null)
    const [tab, setTab] = useState(TABS[0])
    const [list, setList] = useState(null)
    const [error, setError] = useState(false)
    const [sale, setSale] = useState(null)       // { phanTram, soMau }

    useEffect(() => { document.title = 'POLO HOUSE | Áo polo cổ bẻ' }, [])

    useEffect(() => { fetchFilters().then(d => setColors(d.mau)).catch(() => {}) }, [])

    // Tìm mức giảm lớn nhất để hiện trên banner ưu đãi
    useEffect(() => {
        searchProducts({ khuyenMai: true, kichThuoc: 48 })
            .then(d => {
                const max = d.items.flatMap(p => p.bienThe)
                    .reduce((m, v) => Math.max(m, v.giaGoc > v.giaBan ? Math.round((1 - v.giaBan / v.giaGoc) * 100) : 0), 0)
                setSale(d.items.length > 0 && max > 0 ? { phanTram: max, soMau: d.tongSanPham } : null)
            })
            .catch(() => {})
    }, [])

    useEffect(() => {
        let ignore = false
        setList(null)
        setError(false)
        searchProducts({ ...tab.params, trang: 1, kichThuoc: 8 })
            .then(d => { if (!ignore) setList(d.items) })
            .catch(() => { if (!ignore) setError(true) })
        return () => { ignore = true }
    }, [tab])

    const rail = (colors?.length ? colors : FALLBACK_COLORS).slice(0, 5)

    return (
        <>
            <section className="hero">
                <div>
                    <h1>Một kiểu áo, nhiều sắc độ</h1>
                    <p className="sub">Polo cổ bẻ, chất liệu cotton và pique, đủ kích cỡ từ S đến XXL.</p>
                    <div className="hero-cta">
                        <Link to="/san-pham" className="btn">Xem tất cả áo polo</Link>
                        <Link to="/san-pham?km=1" className="btn ghost">Sản phẩm đang giảm giá</Link>
                    </div>
                </div>

                <div>
                    <div className="rail">
                        {rail.map((c, i) => (
                            <Link key={c.id ?? i} to={c.id ? `/san-pham?mau=${c.id}` : '/san-pham'} className="rail-item"
                                  aria-label={`Xem áo polo màu ${c.ten}`}>
                                <Shirt color={c.hex} />
                                <span>{c.ten}</span>
                            </Link>
                        ))}
                        <p className="rail-note">Chọn một màu để xem các mẫu có màu đó.</p>
                    </div>
                </div>
            </section>

            <section className="facts" aria-label="Chính sách cửa hàng">
                {FACTS.map(f => (
                    <div className="fact" key={f.title}>
                        {f.icon}
                        <div><b>{f.title}</b><span className="s">{f.sub}</span></div>
                    </div>
                ))}
            </section>

            <section className="wrap">
                <div className="bar"><h2>Chọn theo phong cách</h2></div>
                <div className="tiles">
                    {TILES.map(t => (
                        <Link key={t.title} to={`/san-pham?q=${encodeURIComponent(t.q)}`} className="tile" style={{ background: t.bg }}>
                            <Shirt color={t.shirt} />
                            <h3>{t.title}</h3>
                            <p>{t.sub}</p>
                        </Link>
                    ))}
                </div>
            </section>

            <section className="wrap" style={{ marginTop: 72 }}>
                <div className="bar">
                    <h2>Sản phẩm</h2>
                    <div className="ftabs" role="tablist">
                        {TABS.map(t => (
                            <button key={t.key} role="tab" aria-selected={tab.key === t.key}
                                    className={`ftab ${tab.key === t.key ? 'on' : ''}`} onClick={() => setTab(t)}>{t.label}</button>
                        ))}
                    </div>
                </div>

                {error && <p className="muted">Không tải được sản phẩm. Kiểm tra backend đã chạy chưa.</p>}
                {!list && !error && <ProductSkeletons n={8} />}
                {list && list.length === 0 && (
                    <div className="empty">
                        <p>Chưa có sản phẩm nào trong mục này.</p>
                        <button className="btn" onClick={() => setTab(TABS[0])}>Xem sản phẩm mới nhất</button>
                    </div>
                )}
                {list && list.length > 0 && <div className="grid">{list.map(sp => <ProductCard key={sp.id} sp={sp} />)}</div>}

                <p style={{ marginTop: 36 }}><Link to={tab.all} className="link">Xem tất cả {tab.label.toLowerCase()}</Link></p>
            </section>

            {sale && (
                <section className="promo">
                    <div className="promo-in">
                        <div>
                            <h2>Giảm đến {sale.phanTram}% cho áo polo chọn lọc</h2>
                            <p>{sale.soMau} mẫu đang được giảm giá.</p>
                        </div>
                        <Link to="/san-pham?km=1" className="btn light">Xem áo giảm giá</Link>
                    </div>
                </section>
            )}
        </>
    )
}