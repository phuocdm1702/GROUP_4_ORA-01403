// "Duy trì đăng nhập" bật -> lưu localStorage (còn sau khi đóng trình duyệt), tắt -> sessionStorage
const makeStore = key => ({
    get() {
        try { return JSON.parse(sessionStorage.getItem(key) ?? localStorage.getItem(key)) } catch { return null }
    },
    set(value, remember) {
        this.clear()
        ;(remember ? localStorage : sessionStorage).setItem(key, JSON.stringify(value))
    },

    // Sửa một phần dữ liệu phiên, giữ nguyên nơi lưu (localStorage hay sessionStorage)
    update(patch) {
        const store = sessionStorage.getItem(key) != null ? sessionStorage
            : localStorage.getItem(key) != null ? localStorage : null
        if (!store) return
        store.setItem(key, JSON.stringify({ ...JSON.parse(store.getItem(key)), ...patch }))
    },

    clear() {
        sessionStorage.removeItem(key)
        localStorage.removeItem(key)
    },
})

export const adminSession = makeStore('admin_user')
export const customerSession = makeStore('customer_user')

const isExpired = token => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
        return payload.exp * 1000 < Date.now()
    } catch {
        return true
    }
}

// Trả về người dùng nếu còn token hợp lệ, ngược lại xóa phiên và trả null
export const getValid = store => {
    const user = store.get()
    if (!user?.token || isExpired(user.token)) {
        store.clear()
        return null
    }
    return user
}