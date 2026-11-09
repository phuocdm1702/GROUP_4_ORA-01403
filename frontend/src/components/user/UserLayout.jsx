import { useState } from 'react'
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Dropdown } from 'antd'
import {
    ShoppingOutlined, UserOutlined, SearchOutlined, CloseOutlined, MenuOutlined,
} from '@ant-design/icons'
import { useCart } from '../../context/CartContext'
import { useAuth } from '../../context/AuthContext'
import { imgUrl, vnd } from '../../data/products'
import ProductImage from './ProductImage'
import './store.css'

const NAV = [
    { key: 'home', label: 'Trang chủ', to: '/' },
    { key: 'all', label: 'Sản phẩm', to: '/san-pham' },
    { key: 'best', label: 'Bán chạy', to: '/san-pham?sx=ban-chay' },
    { key: 'sale', label: 'Khuyến mãi', to: '/san-pham?km=1' },
]

// Mục menu nào đang được chọn, dựa vào đường dẫn và tham số lọc
function isOn(key, path, q) {
    const list = path === '/san-pham'
    if (key === 'home') return path === '/'
    if (key === 'sale') return list && q.get('km') === '1'
    if (key === 'best') return list && q.get('sx') === 'ban-chay' && q.get('km') !== '1'
    return path.startsWith('/san-pham') && !(list && (q.get('km') === '1' || q.get('sx') === 'ban-chay'))
}

export default function UserLayout() {
    const { items, count, total, canCheckout, open, setOpen, setQty } = useCart()
    const { user, logout } = useAuth()
    const navigate = useNavigate()
    const location = useLocation()
    const q = new URLSearchParams(location.search)
    const [menu, setMenu] = useState(false)
    const [kw, setKw] = useState('')

    const go = path => { setOpen(false); navigate(path) }

    const submitSearch = e => {
        e.preventDefault()
        const text = kw.trim()
        navigate(text ? `/san-pham?q=${encodeURIComponent(text)}` : '/san-pham')
    }

    const userMenu = {
        items: [
            { key: 'acc', label: <Link to="/tai-khoan">Hồ sơ của tôi</Link> },
            { key: 'orders', label: <Link to="/tai-khoan/don-hang">Đơn hàng của tôi</Link> },
            { type: 'divider' },
            { key: 'out', label: 'Đăng xuất', onClick: logout },
        ],
    }

    return (
        <div className="store">
            <div className="top">Miễn phí vận chuyển cho đơn từ 500.000 ₫</div>

            <header className="head">
                <button className="burger" aria-label="Mở menu" onClick={() => setMenu(true)}><MenuOutlined /></button>
                <Link to="/" className="logo">POLO HOUSE</Link>

                <nav className="nav" aria-label="Menu chính">
                    {NAV.map(n => (
                        <Link key={n.key} to={n.to} className={isOn(n.key, location.pathname, q) ? 'on' : ''}>{n.label}</Link>
                    ))}
                </nav>

                <div className="hright">
                    <form className="hsearch" onSubmit={submitSearch} role="search">
                        <SearchOutlined />
                        <input placeholder="Tìm áo polo" value={kw} maxLength={100} aria-label="Tìm sản phẩm"
                               onChange={e => setKw(e.target.value)} />
                    </form>
                    <Link to="/san-pham" className="msearch" aria-label="Tìm sản phẩm"><SearchOutlined /></Link>

                    {user ? (
                        <Dropdown menu={userMenu} placement="bottomRight">
                            <Link to="/tai-khoan" className="uicon" aria-label="Tài khoản">
                                <UserOutlined /><span className="uname">{user.name.split(' ').pop()}</span>
                            </Link>
                        </Dropdown>
                    ) : (
                        <Link to="/dang-nhap" className="uicon" aria-label="Đăng nhập"><UserOutlined /></Link>
                    )}

                    <button className="bag" aria-label={`Giỏ hàng, ${count} sản phẩm`} onClick={() => setOpen(true)}
                            style={{ background: 'none', border: 0, padding: 0, color: 'inherit', fontSize: 'inherit' }}>
                        <ShoppingOutlined />{count > 0 && <b>{count}</b>}
                    </button>
                </div>
            </header>

            {/* Menu trượt trên điện thoại */}
            {menu && <div className="overlay" onClick={() => setMenu(false)} />}
            <aside className={`mnav ${menu ? 'open' : ''}`} aria-hidden={!menu}>
                <div className="mhead">
                    <span className="logo" style={{ fontSize: 20 }}>POLO HOUSE</span>
                    <CloseOutlined onClick={() => setMenu(false)} style={{ cursor: 'pointer' }} />
                </div>
                {NAV.map(n => (
                    <Link key={n.key} to={n.to} onClick={() => setMenu(false)}
                          className={isOn(n.key, location.pathname, q) ? 'on' : ''}>{n.label}</Link>
                ))}
                {user ? (
                    <>
                        <Link to="/tai-khoan" onClick={() => setMenu(false)}>Hồ sơ của tôi</Link>
                        <Link to="/tai-khoan/don-hang" onClick={() => setMenu(false)}>Đơn hàng của tôi</Link>
                        <button className="mlink" onClick={() => { setMenu(false); logout() }}>Đăng xuất</button>
                    </>
                ) : (
                    <>
                        <Link to="/dang-nhap" onClick={() => setMenu(false)}>Đăng nhập</Link>
                        <Link to="/dang-ky" onClick={() => setMenu(false)}>Đăng ký</Link>
                    </>
                )}
            </aside>

            <Outlet />

            <footer className="foot">
                <div className="foot-grid">
                    <div>
                        <p className="fbrand">POLO HOUSE</p>
                        <p>Áo polo cổ bẻ, đủ màu và đủ kích cỡ từ S đến XXL.</p>
                    </div>
                    <div>
                        <h4>Mua sắm</h4>
                        <Link to="/san-pham">Tất cả áo polo</Link>
                        <Link to="/san-pham?sx=ban-chay">Bán chạy</Link>
                        <Link to="/san-pham?km=1">Đang giảm giá</Link>
                    </div>
                    <div>
                        <h4>Tài khoản</h4>
                        <Link to={user ? '/tai-khoan' : '/dang-nhap'}>{user ? 'Hồ sơ của tôi' : 'Đăng nhập'}</Link>
                        <Link to="/tai-khoan/don-hang">Đơn hàng của tôi</Link>
                        <Link to="/gio-hang">Giỏ hàng</Link>
                    </div>
                    <div>
                        <h4>Hỗ trợ</h4>
                        <p>Giao hàng toàn quốc trong 2 đến 4 ngày</p>
                        <p>Đổi trả trong 7 ngày</p>
                        <p>support@polohouse.vn</p>
                    </div>
                </div>
                <div className="foot-bot">© {new Date().getFullYear()} POLO HOUSE. Đồ án tốt nghiệp.</div>
            </footer>

            {open && <div className="overlay" onClick={() => setOpen(false)} />}
            <aside className={`drawer ${open ? 'open' : ''}`} aria-hidden={!open}>
                <div className="dh"><span>Giỏ hàng ({count})</span><CloseOutlined onClick={() => setOpen(false)} /></div>
                <div className="di">
                    {items.length === 0 && (
                        <div className="empty" style={{ padding: '40px 0' }}>
                            <p className="muted">Giỏ hàng của bạn đang trống.</p>
                            <button className="btn" onClick={() => go('/san-pham')}>Xem áo polo</button>
                        </div>
                    )}
                    {items.map(({ id, soLuong, d }) => d && (
                        <div className="row" key={id}>
                            <div className="thumb">
                                <ProductImage src={imgUrl(d.anhUrl)} color={d.mau?.hex} alt={d.tenSanPham ?? ''} />
                            </div>
                            <div>
                                <div>{d.khaDung ? d.tenSanPham : 'Sản phẩm không còn được bán'}</div>
                                {d.khaDung && <small className="muted">{d.mau.ten}, size {d.size}</small>}
                                {d.khaDung && d.tonKho === 0 && <div className="warn">Đã hết hàng</div>}
                                <div className="qty">
                                    <button aria-label="Giảm" onClick={() => setQty(id, soLuong - 1)}>−</button>{soLuong}
                                    <button aria-label="Tăng" disabled={d.khaDung && soLuong >= d.tonKho}
                                            onClick={() => setQty(id, soLuong + 1)}>+</button>
                                </div>
                            </div>
                            <b>{d.khaDung ? vnd(d.giaBan * soLuong) : ''}</b>
                        </div>
                    ))}
                </div>
                {items.length > 0 && (
                    <div className="df">
                        <div className="sum"><span>Tạm tính</span><b>{vnd(total)}</b></div>
                        <button className="btn full ghost" onClick={() => go('/gio-hang')}>Xem giỏ hàng</button>
                        <button className="btn full" style={{ marginTop: 8 }} disabled={!canCheckout} onClick={() => go('/thanh-toan')}>
                            Thanh toán
                        </button>
                    </div>
                )}
            </aside>
        </div>
    )
}