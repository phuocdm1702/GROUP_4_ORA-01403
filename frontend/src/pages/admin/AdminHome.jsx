import { Navigate } from 'react-router-dom'
import { Result } from 'antd'
import { adminSession, getValid } from '../../auth/session'
import { firstAllowed } from '../../auth/roles'

export default function AdminHome() {
    const user = getValid(adminSession)
    const to = user && firstAllowed(user)
    return to ? <Navigate to={to} replace />
        : <Result status="403" title="403" subTitle="Tài khoản chưa được phân quyền trang nào." />
}