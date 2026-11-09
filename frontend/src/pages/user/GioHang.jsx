import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../../context/CartContext'
import { imgUrl, vnd } from '../../data/products'
import ProductImage from '../../components/user/ProductImage'

export default function GioHang() {
    const { items, count, total, loading, canCheckout, setQty, remove } = useCart()
    const navigate = useNavigate()

    if (items.length === 0) {
        return (
            <div className="wrap">
                <h1 className="page-title">Giỏ hàng</h1>
                <p className="muted" style={{ marginBottom: 24 }}>Giỏ hàng của bạn đang trống.</p>
                <Link to="/" className="btn">Tiếp tục mua sắm</Link>
            </div>
        )
    }

    return (
        <div className="wrap">
            <h1 className="page-title">Giỏ hàng ({count})</h1>
            <div className="cart-grid">
                <div>
                    {items.map(({ id, soLuong, d }) => d && (
                        <div className="crow" key={id}>
                            <Link to={d.idSanPham ? `/san-pham/${d.idSanPham}?mau=${d.mau?.ma}` : '#'} className="cthumb">
                                <ProductImage src={imgUrl(d.anhUrl)} color={d.mau?.hex} alt={d.tenSanPham ?? ''} />
                            </Link>
                            <div>
                                <div>{d.khaDung ? d.tenSanPham : 'Sản phẩm không còn được bán'}</div>
                                {d.khaDung && <small className="muted">{d.mau.ten} / {d.size}</small>}
                                {d.khaDung && <div className="price">{vnd(d.giaBan)} {d.giaGoc > d.giaBan && <s>{vnd(d.giaGoc)}</s>}</div>}
                                {d.khaDung && d.tonKho === 0 && <div className="warn">Đã hết hàng, vui lòng xóa khỏi giỏ</div>}
                                {d.khaDung && d.tonKho > 0 && d.tonKho <= 5 && <div className="warn">Chỉ còn {d.tonKho} sản phẩm</div>}
                                {!d.khaDung && <div className="warn">Vui lòng xóa khỏi giỏ</div>}
                                <div className="qty" style={{ marginTop: 10 }}>
                                    <button onClick={() => setQty(id, soLuong - 1)}>−</button>{soLuong}
                                    <button disabled={d.khaDung && soLuong >= d.tonKho} onClick={() => setQty(id, soLuong + 1)}>+</button>
                                </div>
                                <div><button className="rm" onClick={() => remove(id)}>Xóa</button></div>
                            </div>
                            <b>{d.khaDung ? vnd(d.giaBan * soLuong) : ''}</b>
                        </div>
                    ))}
                </div>

                <aside className="summary">
                    <h2>Tóm tắt đơn hàng</h2>
                    <div className="srow"><span>Tạm tính</span><b>{vnd(total)}</b></div>
                    <p className="muted">Phí vận chuyển và mã giảm giá được tính ở bước thanh toán. Miễn phí vận chuyển cho đơn từ 500.000 ₫.</p>
                    <button className="btn full" disabled={!canCheckout || loading} onClick={() => navigate('/thanh-toan')}>
                        Tiến hành thanh toán
                    </button>
                    {!canCheckout && !loading && <p className="warn">Vui lòng xử lý các sản phẩm hết hàng hoặc không còn bán.</p>}
                    <p style={{ marginTop: 16 }}><Link to="/" className="link">Tiếp tục mua sắm</Link></p>
                </aside>
            </div>
        </div>
    )
}