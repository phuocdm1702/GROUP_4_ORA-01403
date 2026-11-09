import { createContext, useContext, useState } from 'react'
import { customerSession, getValid } from '../auth/session'

const Ctx = createContext(null)
export const useAuth = () => useContext(Ctx)

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => getValid(customerSession))

    const login = (data, remember) => {
        const u = { token: data.token, id: data.id, name: data.hoVaTen }
        customerSession.set(u, remember)
        setUser(u)
    }
    const logout = () => {
        customerSession.clear()
        setUser(null)
    }
    // Đổi họ tên ở trang hồ sơ thì cập nhật luôn tên trên header
    const rename = name => {
        customerSession.update({ name })
        setUser(u => (u ? { ...u, name } : u))
    }

    return <Ctx.Provider value={{ user, login, logout, rename }}>{children}</Ctx.Provider>
}