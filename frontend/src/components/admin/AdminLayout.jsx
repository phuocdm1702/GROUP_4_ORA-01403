import { useState } from 'react'
import { Layout, Menu, Breadcrumb, Avatar, Dropdown } from 'antd'
import {
    BarChartOutlined, FileTextOutlined, ShoppingOutlined, TagsOutlined,
    GiftOutlined, PercentageOutlined, TeamOutlined, UserOutlined,
    MenuFoldOutlined, MenuUnfoldOutlined,
} from '@ant-design/icons'
import { Navigate, Outlet, useNavigate, useLocation } from 'react-router-dom'
import { adminSession, getValid } from '../../auth/session'
import { canAccess } from '../../auth/roles'

const { Sider, Header, Content } = Layout

const ALL_ITEMS = [
    { key: '/admin/thong-ke', icon: <BarChartOutlined />, label: 'Thống kê' },
    { key: '/admin/hoa-don', icon: <FileTextOutlined />, label: 'Quản lý hóa đơn' },
    { key: '/admin/san-pham', icon: <ShoppingOutlined />, label: 'Quản lý sản phẩm' },
    {
        key: 'thuoc-tinh', icon: <TagsOutlined />, label: 'Quản lý thuộc tính',
        children: [
            { key: '/admin/mau-sac', label: 'Màu sắc' },
            { key: '/admin/kich-thuoc', label: 'Kích thước' },
            { key: '/admin/chat-lieu', label: 'Chất liệu' },
            { key: '/admin/thuong-hieu', label: 'Thương hiệu' },
        ],
    },
    { key: '/admin/phieu-giam-gia', icon: <GiftOutlined />, label: 'Phiếu giảm giá' },
    { key: '/admin/dot-giam-gia', icon: <PercentageOutlined />, label: 'Đợt giảm giá' },
    { key: '/admin/khach-hang', icon: <TeamOutlined />, label: 'Quản lý khách hàng' },
    { key: '/admin/nhan-vien', icon: <UserOutlined />, label: 'Quản lý nhân viên' },
]

export default function AdminLayout() {
    const [collapsed, setCollapsed] = useState(false)
    const navigate = useNavigate()
    const { pathname } = useLocation()
    const user = getValid(adminSession)

    if (!user) return <Navigate to="/admin/login" replace />

    const items = ALL_ITEMS
        .map(i => (i.children ? { ...i, children: i.children.filter(c => canAccess(user, c.key)) } : i))
        .filter(i => (i.children ? i.children.length > 0 : canAccess(user, i.key)))

    const current = items.flatMap(i => i.children ?? i)
        .filter(i => pathname.startsWith(i.key))
        .sort((a, b) => b.key.length - a.key.length)[0]

    const logout = () => {
        adminSession.clear()
        navigate('/admin/login')
    }

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider theme="light" width={250} collapsed={collapsed} style={{ borderRight: '1px solid #f0f0f0' }}>
                <div style={{ height: 80, display: 'grid', placeItems: 'center',
                    fontWeight: 700, letterSpacing: 3, color: '#1f2a44' }}>
                    {collapsed ? 'PH' : 'POLO HOUSE'}
                </div>
                <Menu mode="inline" items={items} selectedKeys={current ? [current.key] : []}
                      defaultOpenKeys={['thuoc-tinh']} onClick={({ key }) => navigate(key)} />
            </Sider>

            <Layout>
                <Header style={{ background: '#fff', display: 'flex', alignItems: 'center',
                    justifyContent: 'space-between', padding: '0 24px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span onClick={() => setCollapsed(!collapsed)} style={{ cursor: 'pointer', fontSize: 18 }}>
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </span>
                        <Breadcrumb items={[{ title: 'Quản lý bán hàng' }, { title: current?.label ?? '' }]} />
                    </div>
                    <Dropdown menu={{ items: [{ key: 'out', label: 'Đăng xuất', onClick: logout }] }}>
                        <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span>{user.name} ({user.role})</span>
                            <Avatar icon={<UserOutlined />} />
                        </div>
                    </Dropdown>
                </Header>

                <Content style={{ margin: 24 }}>
                    <Outlet />
                </Content>
            </Layout>
        </Layout>
    )
}