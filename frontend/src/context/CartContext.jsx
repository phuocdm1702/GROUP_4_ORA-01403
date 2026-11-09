import { createContext, useContext, useEffect, useRef, useState } from 'react'
import api from '../api/axios'
import { useAuth } from './AuthContext'

const Ctx = createContext(null)
export const useCart = () => useContext(Ctx)

const KEY = 'cart'
const MAX = 99

const load = () => {
    try {
        const v = JSON.parse(localStorage.getItem(KEY))
        return Array.isArray(v) ? v.filter(l => l?.id && l?.q > 0) : []
    } catch {
        return []
    }
}

// Gộp giỏ trong máy với giỏ trên server: mỗi món lấy số lượng lớn hơn
const merge = (local, server) => {
    const m = new Map(local.map(l => [l.id, l.q]))
    server.forEach(s => m.set(s.idBienThe, Math.max(m.get(s.idBienThe) ?? 0, s.soLuong)))
    return [...m].map(([id, q]) => ({ id, q: Math.min(MAX, q) }))
}

export function CartProvider({ children }) {
    const { user } = useAuth()
    const [lines, setLines] = useState(load)       // [{ id: mã biến thể, q: số lượng }]
    const [details, setDetails] = useState({})      // mã biến thể -> thông tin mới nhất từ server
    const [loading, setLoading] = useState(false)
    const [open, setOpen] = useState(false)
    const [tick, setTick] = useState(0)
    const synced = useRef(false)
    const hadUser = useRef(false)

    // Đăng nhập: gộp giỏ trên server. Đăng xuất: xóa giỏ trong máy.
    useEffect(() => {
        synced.current = false
        if (!user) {
            if (hadUser.current) setLines([])
            hadUser.current = false
            return
        }
        hadUser.current = true
        let ignore = false
        api.get('/user/gio-hang')
            .then(({ data }) => {
                if (ignore) return
                setLines(local => merge(local, data))
                synced.current = true
            })
            .catch(() => {})
        return () => { ignore = true }
    }, [user?.id])

    // Lưu vào máy, và đẩy lên server nếu đã đăng nhập
    useEffect(() => {
        localStorage.setItem(KEY, JSON.stringify(lines))
        if (!user || !synced.current) return
        const t = setTimeout(() => {
            api.put('/user/gio-hang', lines.map(l => ({ idBienThe: l.id, soLuong: l.q }))).catch(() => {})
        }, 600)
        return () => clearTimeout(t)
    }, [lines, user?.id])

    // Hỏi server giá, tồn kho, ảnh hiện tại của các món trong giỏ
    useEffect(() => {
        if (lines.length === 0) { setDetails({}); return }
        let ignore = false
        setLoading(true)
        api.post('/public/gio-hang/xem', lines.map(l => ({ idBienThe: l.id, soLuong: l.q })))
            .then(({ data }) => { if (!ignore) setDetails(Object.fromEntries(data.map(d => [d.idBienThe, d]))) })
            .catch(() => {})
            .finally(() => { if (!ignore) setLoading(false) })
        return () => { ignore = true }
    }, [lines, tick])

    // Số lượng vượt tồn kho thì hạ xuống bằng tồn kho
    useEffect(() => {
        setLines(cur => {
            let changed = false
            const next = cur.map(l => {
                const d = details[l.id]
                if (d?.khaDung && d.tonKho > 0 && l.q > d.tonKho) { changed = true; return { ...l, q: d.tonKho } }
                return l
            })
            return changed ? next : cur
        })
    }, [details])

    const add = (id, n = 1) => {
        setLines(cur => cur.some(l => l.id === id)
            ? cur.map(l => (l.id === id ? { ...l, q: Math.min(MAX, l.q + n) } : l))
            : [...cur, { id, q: Math.min(MAX, n) }])
        setOpen(true)
    }
    const setQty = (id, q) => setLines(cur => (q <= 0
        ? cur.filter(l => l.id !== id)
        : cur.map(l => (l.id === id ? { ...l, q: Math.min(MAX, q) } : l))))
    const remove = id => setLines(cur => cur.filter(l => l.id !== id))
    const clear = () => setLines([])
    const refresh = () => setTick(t => t + 1)

    const items = lines.map(l => ({ id: l.id, soLuong: l.q, d: details[l.id] ?? null }))
    const count = lines.reduce((s, l) => s + l.q, 0)
    const total = items.reduce((s, i) => (i.d?.khaDung ? s + i.d.giaBan * i.soLuong : s), 0)
    const canCheckout = items.length > 0
        && items.every(i => i.d?.khaDung && i.d.tonKho > 0 && i.soLuong <= i.d.tonKho)

    return (
        <Ctx.Provider value={{ items, count, total, loading, canCheckout, open, setOpen, add, setQty, remove, clear, refresh }}>
            {children}
        </Ctx.Provider>
    )
}