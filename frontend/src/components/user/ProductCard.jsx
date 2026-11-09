import { useState } from 'react'
import { Link } from 'react-router-dom'
import { colorsOf, imagesFor, vnd } from '../../data/products'
import ProductImage from './ProductImage'

export default function ProductCard({ sp }) {
    const colors = colorsOf(sp)
    const [mau, setMau] = useState(colors[0])
    const [hover, setHover] = useState(false)

    const v = sp.bienThe.reduce((a, b) => (b.giaBan < a.giaBan ? b : a), sp.bienThe[0])
    const sale = v.giaGoc > v.giaBan ? Math.round((1 - v.giaBan / v.giaGoc) * 100) : 0
    const soldOut = sp.bienThe.every(x => x.soLuong === 0)

    const imgs = imagesFor(sp, mau.ma)
    const src = hover && imgs[1] ? imgs[1] : imgs[0]   // rê chuột thì hiện ảnh thứ hai
    const to = `/san-pham/${sp.id}?mau=${mau.ma}`

    return (
        <div className="card">
            <Link to={to} className="img" aria-label={sp.ten}
                  onMouseEnter={() => setHover(true)} onMouseLeave={() => setHover(false)}>
                <ProductImage src={src} color={mau.hex} alt={sp.ten} />
                {soldOut ? <span className="tag dark">Hết hàng</span> : sale > 0 && <span className="tag">-{sale}%</span>}
            </Link>
            <div className="sws">
                {colors.map(c => (
                    <span key={c.ma} title={c.ten} role="button" tabIndex={0} aria-label={`Màu ${c.ten}`}
                          className={`sw ${c.ma === mau.ma ? 'on' : ''}`} style={{ background: c.hex }}
                          onClick={() => setMau(c)} onKeyDown={e => e.key === 'Enter' && setMau(c)} />
                ))}
            </div>
            {sp.chatLieu && <span className="mat">{sp.chatLieu}</span>}
            <Link to={to} className="name">{sp.ten}</Link>
            <div className="price">
                {vnd(v.giaBan)}
                {sale > 0 && <><s>{vnd(v.giaGoc)}</s></>}
            </div>
        </div>
    )
}