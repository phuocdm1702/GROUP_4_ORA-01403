import { useState } from 'react'
import { Form, Input, Button, Checkbox, Alert } from 'antd'
import { UserOutlined, LockOutlined, ArrowRightOutlined } from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/axios'
import { adminSession } from '../../auth/session'
import { canAccess, firstAllowed } from '../../auth/roles'

export default function Login() {
    const navigate = useNavigate()
    const location = useLocation()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const onFinish = async values => {
        setError('')
        setLoading(true)
        try {
            const { data } = await api.post('/auth/admin/dang-nhap', {
                tenDangNhap: values.username, matKhau: values.password,
            })
            const user = { token: data.token, id: data.id, name: data.hoVaTen, role: data.tenVaiTro, roles: data.roles }
            adminSession.set(user, !!values.remember)

            const from = location.state?.from
            const to = from && canAccess(user, from.replace(/\/(them|\d+)$/, '')) ? from : firstAllowed(user)
            if (!to) return setError('Tài khoản chưa được phân quyền trang nào')
            navigate(to, { replace: true })
        } catch (e) {
            setError(e.response?.data?.message ?? 'Không đăng nhập được. Kiểm tra backend đã chạy chưa.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div style={{ minHeight: '100vh', display: 'grid', placeItems: 'center',
            background: 'linear-gradient(135deg,#e8eefc,#f5ecfb)' }}>
            <div style={{ display: 'flex', width: 900, maxWidth: '95vw', borderRadius: 24,
                overflow: 'hidden', background: '#fff', boxShadow: '0 20px 60px rgba(31,42,68,.15)' }}>

                <div style={{ flex: 1, padding: 40, color: '#fff', display: 'flex', flexDirection: 'column',
                    justifyContent: 'center', alignItems: 'center', textAlign: 'center',
                    background: 'linear-gradient(160deg,#1f2a44,#4a6fb5)' }}>
                    <div style={{ fontSize: 32, fontWeight: 700, letterSpacing: 4 }}>POLO HOUSE</div>
                    <div style={{ marginTop: 12, opacity: .85, letterSpacing: 2 }}>CLASSIC • TIMELESS</div>
                </div>

                <div style={{ flex: 1, padding: 48 }}>
                    <h2 style={{ color: '#1f2a44', marginBottom: 32 }}>Chào mừng đến với hệ thống</h2>
                    {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}
                    <Form layout="vertical" onFinish={onFinish} size="large" initialValues={{ remember: true }}>
                        <Form.Item label="Tài khoản" name="username"
                                   rules={[{ required: true, message: 'Vui lòng nhập tài khoản' }]}>
                            <Input prefix={<UserOutlined />} placeholder="Tên đăng nhập hoặc số điện thoại" />
                        </Form.Item>
                        <Form.Item label="Mật khẩu" name="password"
                                   rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}>
                            <Input.Password prefix={<LockOutlined />} placeholder="Nhập mật khẩu" />
                        </Form.Item>
                        <Form.Item name="remember" valuePropName="checked">
                            <Checkbox>Duy trì đăng nhập</Checkbox>
                        </Form.Item>
                        <Button type="primary" htmlType="submit" block loading={loading}>
                            Đăng nhập hệ thống <ArrowRightOutlined />
                        </Button>
                    </Form>
                </div>
            </div>
        </div>
    )
}