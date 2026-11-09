export const API_ORIGIN = 'http://localhost:8080'

// Đường dẫn ảnh lưu trong database là /uploads/xxx.jpg -> ghép thêm địa chỉ backend
export const imgUrl = u => (!u ? null : /^https?:/.test(u) ? u : API_ORIGIN + u)

// Ảnh của một màu; màu chưa có ảnh thì dùng ảnh chung (maMau = null)
export const imagesFor = (sp, maMau) => {
    const all = sp.hinhAnh ?? []
    const own = all.filter(h => h.maMau === maMau)
    return (own.length ? own : all.filter(h => h.maMau == null)).map(h => imgUrl(h.url))
}

export const colorsOf = p => [...new Map(p.bienThe.map(v => [v.mau.ma, v.mau])).values()]
export const vnd = n => Number(n).toLocaleString('vi-VN') + ' ₫'