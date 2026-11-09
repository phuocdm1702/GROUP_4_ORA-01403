import axios from 'axios'
import { adminSession, customerSession, getValid } from '../auth/session'

const api = axios.create({ baseURL: 'http://localhost:8080/api' })

// /admin/** dùng token nhân viên, /user/** dùng token khách hàng, còn lại là công khai
api.interceptors.request.use(config => {
    const url = config.url ?? ''
    const user = url.startsWith('/admin') ? getValid(adminSession)
        : url.startsWith('/user') ? getValid(customerSession) : null
    if (user) config.headers.Authorization = `Bearer ${user.token}`
    return config
})

// Token hết hạn, hoặc tài khoản bị khóa -> về trang đăng nhập
api.interceptors.response.use(
    res => res,
    error => {
        const url = error.config?.url ?? ''
        if (error.response?.status === 401) {
            if (url.startsWith('/admin')) {
                adminSession.clear()
                window.location.assign('/admin/login')
            } else if (url.startsWith('/user')) {
                customerSession.clear()
                window.location.assign('/dang-nhap')
            }
        }
        return Promise.reject(error)
    },
)

export default api