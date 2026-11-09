import { Navigate, useLocation } from 'react-router-dom'
import { Result } from 'antd'
import { adminSession, getValid } from '../../auth/session'

export default function ProtectedRoute({ children, roles }) {
    const user = getValid(adminSession)
    const location = useLocation()

    if (!user) return <Navigate to="/admin/login" replace state={{ from: location.pathname }} />
    if (roles && !roles.some(r => user.roles?.includes(r))) {
        return <Result status="403" title="403" subTitle="Bạn không có quyền truy cập trang này." />
    }
    return children
}