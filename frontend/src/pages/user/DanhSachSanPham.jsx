import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { CloseOutlined } from '@ant-design/icons'
import { fetchFilters, searchProducts } from '../../api/storeApi'
import { vnd } from '../../data/products'
import ProductCard from '../../components/user/ProductCard'
import { ProductSkeletons } from '../../components/user/Skeleton'

const PAGE_SIZE = 12
const SORTS = [
    { value: 'moi', label: 'Mới nhất' },
    { value: 'ban-chay', label: 'Bán chạy' },
    { value: 'gia-tang', label: 'Giá tăng dần' },
    { value: 'gia-giam', label: 'Giá giảm dần' },
    { value: 'ten', label: 'Tên A–Z' },
]

const toList = s => (s ?? '').split(',').filter(Boolean).map(Number).filter(Number.isFinite)
const toggle = (arr, id) => (arr.includes(id) ? arr.filter(x => x !== id) : [...arr, id])

// 1 2 ... 5 6 7 ... 20
const pageList = (cur, total) => {
    const set = new Set([1, total, cur, cur - 1, cur + 1, cur - 2, cur + 2].filter(p => p >= 1 && p <= total))
    const arr = [...set].sort((a, b) => a - b)
    const out = []
    arr.forEach((p, i) => {
        if (i && p - arr[i - 1] > 1) out.push(`...${p}`)
        out.push(p)
    })
    return out
}

export default function DanhSachSanPham() {
    const [sp, setSp] = useSearchParams()
    const query = sp.toString()

    useEffect(() => { document.title = 'Áo polo | POLO HOUSE' }, [])
    const [options, setOptions] = useState(null)
    const [data, setData] = useState(null)
    const [status, setStatus] = useState('loading')      // loading | ok | refreshing | error
    const [showFilters, setShowFilters] = useState(false)
    const [kw, setKw] = useState(sp.get('q') ?? '')
    const [price, setPrice] = useState({ tu: sp.get('giaTu') ?? '', den: sp.get('giaDen') ?? '' })
    const [priceErr, setPriceErr] = useState('')

    const q = sp.get('q') ?? ''
    const mau = toList(sp.get('mau'))
    const kichCo = toList(sp.get('kichCo'))
    const chatLieu = toList(sp.get('chatLieu'))
    const giaTu = sp.get('giaTu')
    const giaDen = sp.get('giaDen')
    const conHang = sp.get('conHang') === '1'
    const khuyenMai = sp.get('km') === '1'
    const sapXep = sp.get('sx') ?? 'moi'

    // Cập nhật bộ lọc trên đường dẫn. Mặc định quay về trang 1.
    const update = (patch, keepPage = false) => {
        const next = new URLSearchParams(sp)
        Object.entries(patch).forEach(([k, v]) => {
            const empty = v == null || v === '' || v === false || (Array.isArray(v) && v.length === 0)
            if (empty) next.delete(k)
            else next.set(k, Array.isArray(v) ? v.join(',') : v === true ? '1' : String(v))
        })
        if (!keepPage) next.delete('trang')
        setSp(next)
    }

    const clearAll = () => setSp(new URLSearchParams())

    useEffect(() => { fetchFilters().then(setOptions).catch(() => {}) }, [])

    // Ô tìm kiếm và ô giá đồng bộ lại khi đường dẫn đổi từ bên ngoài (bấm menu, nút Back)
    useEffect(() => {
        const p = new URLSearchParams(query)
        setKw(p.get('q') ?? '')
        setPrice({ tu: p.get('giaTu') ?? '', den: p.get('giaDen') ?? '' })
        setPriceErr('')
    }, [query])

    useEffect(() => {
        let ignore = false
        setStatus(s => (s === 'ok' || s === 'refreshing' ? 'refreshing' : 'loading'))
        const p = new URLSearchParams(query)
        searchProducts({
            q: p.get('q') || undefined,
            mau: p.get('mau') || undefined,
            kichCo: p.get('kichCo') || undefined,
            chatLieu: p.get('chatLieu') || undefined,
            giaTu: p.get('giaTu') || undefined,
            giaDen: p.get('giaDen') || undefined,
            conHang: p.get('conHang') === '1' || undefined,
            khuyenMai: p.get('km') === '1' || undefined,
            sapXep: p.get('sx') || undefined,
            trang: Number(p.get('trang')) || 1,
            kichThuoc: PAGE_SIZE,
        })
            .then(d => { if (!ignore) { setData(d); setStatus('ok') } })
            .catch(() => { if (!ignore) setStatus('error') })
        return () => { ignore = true }
    }, [query])

    const submitSearch = e => {
        e.preventDefault()
        update({ q: kw.trim() })
    }

    const applyPrice = e => {
        e.preventDefault()
        const tu = price.tu === '' ? null : Number(price.tu)
        const den = price.den === '' ? null : Number(price.den)
        if ((tu != null && (Number.isNaN(tu) || tu < 0)) || (den != null && (Number.isNaN(den) || den < 0))) {
            return setPriceErr('Giá không hợp lệ')
        }
        if (tu != null && den != null && tu > den) return setPriceErr('Giá từ phải nhỏ hơn hoặc bằng giá đến')
        setPriceErr('')
        update({ giaTu: tu ?? '', giaDen: den ?? '' })
    }

    const goPage = p => {
        update({ trang: p > 1 ? p : '' }, true)
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    // Các thẻ "bộ lọc đang chọn", bấm để bỏ từng cái
    const chips = []
    if (q) chips.push({ key: 'q', label: `Từ khóa: ${q}`, clear: () => update({ q: '' }) })
    mau.forEach(id => {
        const o = options?.mau.find(x => x.id === id)
        if (o) chips.push({ key: `m${id}`, label: o.ten, clear: () => update({ mau: mau.filter(x => x !== id) }) })
    })
    kichCo.forEach(id => {
        const o = options?.kichCo.find(x => x.id === id)
        if (o) chips.push({ key: `k${id}`, label: `Size ${o.ten}`, clear: () => update({ kichCo: kichCo.filter(x => x !== id) }) })
    })
    chatLieu.forEach(id => {
        const o = options?.chatLieu.find(x => x.id === id)
        if (o) chips.push({ key: `c${id}`, label: o.ten, clear: () => update({ chatLieu: chatLieu.filter(x => x !== id) }) })
    })
    if (giaTu || giaDen) {
        chips.push({
            key: 'gia',
            label: `Giá: ${giaTu ? vnd(giaTu) : '0'} – ${giaDen ? vnd(giaDen) : 'không giới hạn'}`,
            clear: () => update({ giaTu: '', giaDen: '' }),
        })
    }
    if (conHang) chips.push({ key: 'ch', label: 'Còn hàng', clear: () => update({ conHang: false }) })
    if (khuyenMai) chips.push({ key: 'km', label: 'Đang giảm giá', clear: () => update({ km: false }) })

    return (
        <div className="wrap">
            <h1 className="page-title">Áo polo</h1>

            <div className="toolbar">
                <form className="tsearch" onSubmit={submitSearch}>
                    <input placeholder="Tìm theo tên, màu, chất liệu..." value={kw} maxLength={100}
                           onChange={e => setKw(e.target.value)} />
                    <button className="btn">Tìm</button>
                </form>
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <button className="btn ghost only-m" type="button" onClick={() => setShowFilters(s => !s)}>
                        {showFilters ? 'Ẩn bộ lọc' : 'Bộ lọc'}
                    </button>
                    <select value={sapXep} onChange={e => update({ sx: e.target.value === 'moi' ? '' : e.target.value })}>
                        {SORTS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
                    </select>
                </div>
            </div>

            <div className="plp">
                <aside className={`filters ${showFilters ? 'open' : ''}`}>
                    <div className="fg">
                        <h3>Màu sắc</h3>
                        <div className="fsw">
                            {options?.mau.map(c => (
                                <span key={c.id} title={c.ten} className={`sw lg ${mau.includes(c.id) ? 'on' : ''}`}
                                      style={{ background: c.hex }} onClick={() => update({ mau: toggle(mau, c.id) })} />
                            ))}
                        </div>
                    </div>

                    <div className="fg">
                        <h3>Kích thước</h3>
                        <div className="fsz">
                            {options?.kichCo.map(k => (
                                <button key={k.id} type="button" className={`fbtn ${kichCo.includes(k.id) ? 'on' : ''}`}
                                        onClick={() => update({ kichCo: toggle(kichCo, k.id) })}>{k.ten}</button>
                            ))}
                        </div>
                    </div>

                    <div className="fg">
                        <h3>Chất liệu</h3>
                        {options?.chatLieu.map(c => (
                            <label key={c.id} className="fchk">
                                <input type="checkbox" checked={chatLieu.includes(c.id)}
                                       onChange={() => update({ chatLieu: toggle(chatLieu, c.id) })} /> {c.ten}
                            </label>
                        ))}
                    </div>

                    <div className="fg">
                        <h3>Khoảng giá (₫)</h3>
                        <form onSubmit={applyPrice}>
                            <div className="prange">
                                <input type="number" min="0" step="10000" placeholder={options ? String(options.giaMin) : 'Từ'}
                                       value={price.tu} onChange={e => setPrice(p => ({ ...p, tu: e.target.value }))} />
                                <span>–</span>
                                <input type="number" min="0" step="10000" placeholder={options ? String(options.giaMax) : 'Đến'}
                                       value={price.den} onChange={e => setPrice(p => ({ ...p, den: e.target.value }))} />
                            </div>
                            {priceErr && <p className="warn">{priceErr}</p>}
                            <button className="btn ghost" style={{ marginTop: 10, padding: '9px 16px' }}>Áp dụng</button>
                        </form>
                    </div>

                    <div className="fg">
                        <label className="fchk">
                            <input type="checkbox" checked={conHang} onChange={e => update({ conHang: e.target.checked })} /> Chỉ hiện còn hàng
                        </label>
                        <label className="fchk">
                            <input type="checkbox" checked={khuyenMai} onChange={e => update({ km: e.target.checked })} /> Đang giảm giá
                        </label>
                    </div>

                    {chips.length > 0 && (
                        <button type="button" className="rm" style={{ marginTop: 14 }} onClick={clearAll}>Xóa tất cả bộ lọc</button>
                    )}
                </aside>

                <section>
                    {chips.length > 0 && (
                        <div className="chips">
                            {chips.map(c => (
                                <span key={c.key} className="chip" onClick={c.clear}>{c.label} <CloseOutlined style={{ fontSize: 10 }} /></span>
                            ))}
                        </div>
                    )}

                    {status === 'loading' && <ProductSkeletons n={PAGE_SIZE} />}
                    {status === 'error' && <p className="muted">Không tải được sản phẩm. Kiểm tra backend đã chạy chưa.</p>}

                    {data && status !== 'error' && (
                        <>
                            <p className="muted" style={{ marginBottom: 20 }}>{data.tongSanPham} sản phẩm</p>

                            {data.items.length === 0 ? (
                                <div className="empty">
                                    <p>Không tìm thấy sản phẩm phù hợp.</p>
                                    {chips.length > 0 && <button className="btn" onClick={clearAll}>Xóa bộ lọc</button>}
                                </div>
                            ) : (
                                <div className={`grid ${status === 'refreshing' ? 'fade' : ''}`}>
                                    {data.items.map(p => <ProductCard key={`${p.id}-${query}`} sp={p} />)}
                                </div>
                            )}

                            {data.tongTrang > 1 && (
                                <div className="pager">
                                    <button className="pg" disabled={data.trang <= 1} onClick={() => goPage(data.trang - 1)}>‹</button>
                                    {pageList(data.trang, data.tongTrang).map(p => (typeof p === 'string'
                                        ? <span key={p} className="dots">…</span>
                                        : <button key={p} className={`pg ${p === data.trang ? 'on' : ''}`} onClick={() => goPage(p)}>{p}</button>))}
                                    <button className="pg" disabled={data.trang >= data.tongTrang} onClick={() => goPage(data.trang + 1)}>›</button>
                                </div>
                            )}
                        </>
                    )}
                </section>
            </div>
        </div>
    )
}