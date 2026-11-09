import { useEffect, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { Modal, Table } from 'antd'
import { CarOutlined, SwapOutlined, WalletOutlined } from '@ant-design/icons'
import { fetchProduct, searchProducts } from '../../api/storeApi'
import { colorsOf, imagesFor, vnd } from '../../data/products'
import { useCart } from '../../context/CartContext'
import ProductImage from '../../components/user/ProductImage'
import ProductCard from '../../components/user/ProductCard'

// Bảng tham khảo. Hãy thay bằng số đo thực tế của cửa hàng.
const SIZE_TABLE = [
    { key: 'S', size: 'S', cao: '160 đến 168 cm', nang: '52 đến 60 kg', nguc: '88 đến 92 cm' },
    { key: 'M', size: 'M', cao: '165 đến 172 cm', nang: '58 đến 66 kg', nguc: '92 đến 96 cm' },
    { key: 'L', size: 'L', cao: '170 đến 177 cm', nang: '64 đến 72 kg', nguc: '96 đến 100 cm' },
    { key: 'XL', size: 'XL', cao: '174 đến 182 cm', nang: '70 đến 80 kg', nguc: '100 đến 106 cm' },
    { key: 'XXL', size: 'XXL', cao: '178 đến 186 cm', nang: '78 đến 90 kg', nguc: '106 đến 112 cm' },
]

export default function ProductDetail() {
    const { id } = useParams()
    const [state, setState] = useState({ id: null, data: null, error: null })

    useEffect(() => {
        let ignore = false
        fetchProduct(id)
            .then(data => { if (!ignore) setState({ id, data, error: null }) })
            .catch(e => {
                if (!ignore) setState({ id, data: null, error: e.response?.status === 404 ? 'notfound' : 'error' })
            })
        return () => { ignore = true }
    }, [id])

    if (state.id !== id) return <p className="wrap muted">Đang tải sản phẩm...</p>
    if (state.error === 'notfound') {
        return (
            <div className="wrap empty">
                <p>Không tìm thấy sản phẩm này. Có thể sản phẩm đã ngừng bán.</p>
                <Link to="/san-pham" className="btn">Xem tất cả áo polo</Link>
            </div>
        )
    }
    if (state.error) return <p className="wrap">Không tải được sản phẩm. Kiểm tra backend đã chạy chưa.</p>
    return <ProductView key={state.data.id} sp={state.data} />
}

function ProductView({ sp }) {
    const [qs] = useSearchParams()
    const colors = colorsOf(sp)
    const [mau, setMau] = useState(colors.find(c => c.ma === qs.get('mau')) ?? colors[0])
    const [size, setSize] = useState(null)
    const [qty, setQty] = useState(1)
    const [idx, setIdx] = useState(0)
    const [guide, setGuide] = useState(false)
    const { add } = useCart()

    useEffect(() => { document.title = `${sp.ten} | POLO HOUSE` }, [sp.ten])

    const imgs = imagesFor(sp, mau.ma)
    const vs = sp.bienThe.filter(v => v.mau.ma === mau.ma)
    const bt = vs.find(v => v.size === size)
    const v0 = vs[0]
    const sale = v0.giaGoc > v0.giaBan ? Math.round((1 - v0.giaBan / v0.giaGoc) * 100) : 0

    return (
        <>
            <nav className="crumb" aria-label="Đường dẫn">
                <Link to="/">Trang chủ</Link><span>/</span><Link to="/san-pham">Sản phẩm</Link><span>/</span>{sp.ten}
            </nav>

            <div className="detail">
                <div>
                    <div className="pic"><ProductImage src={imgs[idx]} color={mau.hex} alt={sp.ten} /></div>
                    {imgs.length > 1 && (
                        <div className="thumbs">
                            {imgs.map((u, i) => (
                                <img key={u} src={u} alt={`Ảnh ${i + 1}`} className={`tn ${i === idx ? 'on' : ''}`} onClick={() => setIdx(i)} />
                            ))}
                        </div>
                    )}
                </div>

                <div>
                    <h1>{sp.ten}</h1>
                    <div className="price">
                        {vnd(v0.giaBan)}
                        {sale > 0 && <><s>{vnd(v0.giaGoc)}</s><span className="off">Giảm {sale}%</span></>}
                    </div>

                    <p className="lbl">Màu: <b>{mau.ten}</b></p>
                    <div className="sws">
                        {colors.map(c => (
                            <span key={c.ma} role="button" tabIndex={0} aria-label={`Màu ${c.ten}`} title={c.ten}
                                  className={`sw lg ${c.ma === mau.ma ? 'on' : ''}`} style={{ background: c.hex }}
                                  onClick={() => { setMau(c); setSize(null); setQty(1); setIdx(0) }}
                                  onKeyDown={e => e.key === 'Enter' && (setMau(c), setSize(null), setQty(1), setIdx(0))} />
                        ))}
                    </div>

                    <div className="lblrow">
                        <p className="lbl">Kích thước</p>
                        <button className="linkbtn" onClick={() => setGuide(true)}>Bảng chọn size</button>
                    </div>
                    <div className="szs">
                        {vs.map(v => (
                            <button key={v.id} disabled={v.soLuong === 0} aria-label={`Size ${v.size}${v.soLuong === 0 ? ', hết hàng' : ''}`}
                                    className={`sz ${v.size === size ? 'on' : ''}`}
                                    onClick={() => { setSize(v.size); setQty(1) }}>{v.size}</button>
                        ))}
                    </div>
                    {bt && (bt.soLuong <= 5
                        ? <p className="warn">Chỉ còn {bt.soLuong} sản phẩm</p>
                        : <p className="muted">Còn hàng</p>)}

                    <div className="qty big">
                        <button aria-label="Giảm số lượng" onClick={() => setQty(Math.max(1, qty - 1))}>−</button>{qty}
                        <button aria-label="Tăng số lượng" disabled={!bt || qty >= bt.soLuong}
                                onClick={() => setQty(Math.min(bt?.soLuong ?? 1, qty + 1))}>+</button>
                    </div>
                    <button className="btn full" disabled={!bt} onClick={() => add(bt.id, qty)}>
                        {bt ? 'Thêm vào giỏ hàng' : 'Chọn kích thước để thêm vào giỏ'}
                    </button>

                    <div className="benefits">
                        <div><CarOutlined /> Miễn phí vận chuyển cho đơn từ 500.000 ₫</div>
                        <div><SwapOutlined /> Đổi trả trong 7 ngày</div>
                        <div><WalletOutlined /> Thanh toán khi nhận hàng</div>
                    </div>

                    <details open><summary>Mô tả</summary><p>{sp.moTa}</p></details>
                    <details><summary>Chất liệu và bảo quản</summary>
                        <p>{sp.chatLieu}. Giặt máy nước lạnh, không dùng chất tẩy, phơi trong bóng râm.</p>
                    </details>
                    <details><summary>Giao hàng và đổi trả</summary>
                        <p>Giao hàng toàn quốc trong 2 đến 4 ngày. Đổi trả trong 7 ngày kể từ khi nhận hàng.</p>
                    </details>
                </div>
            </div>

            <Related currentId={sp.id} />

            <Modal title="Bảng chọn size" open={guide} onCancel={() => setGuide(false)} footer={null} width={620}>
                <Table size="small" pagination={false} dataSource={SIZE_TABLE} rowKey="key"
                       columns={[
                           { title: 'Size', dataIndex: 'size', width: 70 },
                           { title: 'Chiều cao', dataIndex: 'cao' },
                           { title: 'Cân nặng', dataIndex: 'nang' },
                           { title: 'Vòng ngực', dataIndex: 'nguc' },
                       ]} />
                <p style={{ marginTop: 12, color: '#6b7480', fontSize: 13 }}>
                    Bảng chỉ mang tính tham khảo, số đo thực tế có thể lệch 1 đến 2 cm.
                </p>
            </Modal>
        </>
    )
}

function Related({ currentId }) {
    const [items, setItems] = useState([])

    useEffect(() => {
        let ignore = false
        searchProducts({ sapXep: 'ban-chay', kichThuoc: 6 })
            .then(d => { if (!ignore) setItems(d.items.filter(p => p.id !== currentId).slice(0, 4)) })
            .catch(() => {})
        return () => { ignore = true }
    }, [currentId])

    if (items.length === 0) return null
    return (
        <section className="wrap">
            <div className="bar"><h2>Có thể bạn cũng thích</h2></div>
            <div className="grid">{items.map(p => <ProductCard key={p.id} sp={p} />)}</div>
        </section>
    )
}