import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Table, Button, Input, Select, Space, Tag, Switch, Card, App } from 'antd'
import { PlusOutlined, EditOutlined } from '@ant-design/icons'
import api from '../../api/axios'
import { vnd } from '../../data/products'

export default function SanPham() {
    const { message } = App.useApp()
    const navigate = useNavigate()
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(false)
    const [search, setSearch] = useState('')
    const [status, setStatus] = useState('all')

    const load = async () => {
        setLoading(true)
        try {
            const res = await api.get('/admin/san-pham')
            setData(res.data)
        } catch {
            message.error('Không tải được dữ liệu. Kiểm tra backend đã chạy chưa.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    const toggle = async (r, value) => {
        try {
            await api.put(`/admin/san-pham/${r.id}/trang-thai`, null, { params: { value } })
            message.success(value ? 'Đã mở bán' : 'Đã ngừng bán')
            load()
        } catch {
            message.error('Không đổi được trạng thái')
        }
    }

    const filtered = data.filter(d => {
        const okText = ((d.maAo ?? '') + (d.tenAo ?? '')).toLowerCase().includes(search.toLowerCase())
        const okStatus = status === 'all' || (status === 'on' ? d.trangThai : !d.trangThai)
        return okText && okStatus
    })

    const columns = [
        { title: 'STT', width: 60, render: (_, __, i) => i + 1 },
        { title: 'Mã', dataIndex: 'maAo', width: 100 },
        { title: 'Tên sản phẩm', dataIndex: 'tenAo', width: 260 },
        {
            title: 'Màu', width: 130,
            render: (_, r) => (
                <Space size={6}>
                    {r.mau.map(c => (
                        <span key={c.id} title={c.ten} style={{ width: 18, height: 18, borderRadius: '50%',
                            background: c.hex, border: '1px solid #d9d9d9', display: 'inline-block' }} />
                    ))}
                </Space>
            ),
        },
        { title: 'Biến thể', dataIndex: 'soBienThe', width: 90 },
        {
            title: 'Tồn kho', dataIndex: 'tongTon', width: 100,
            render: v => v === 0 ? <Tag color="red">Hết hàng</Tag> : v,
        },
        {
            title: 'Giá bán', width: 200,
            render: (_, r) => r.giaTu == null ? '—'
                : r.giaTu === r.giaDen ? vnd(r.giaTu) : `${vnd(r.giaTu)} – ${vnd(r.giaDen)}`,
        },
        {
            title: 'Trạng thái', dataIndex: 'trangThai', width: 110,
            render: (v, r) => <Switch checked={v} onChange={c => toggle(r, c)} />,
        },
        {
            title: 'Thao tác', width: 90,
            render: (_, r) => <Button icon={<EditOutlined />} onClick={() => navigate(`/admin/san-pham/${r.id}`)} />,
        },
    ]

    return (
        <Card title="Quản lý sản phẩm"
              extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/san-pham/them')}>Thêm sản phẩm</Button>}>
            <Space style={{ marginBottom: 16 }} wrap>
                <Input.Search placeholder="Tìm theo mã hoặc tên" allowClear style={{ width: 320 }}
                              onChange={e => setSearch(e.target.value)} />
                <Select value={status} onChange={setStatus} style={{ width: 160 }}
                        options={[{ value: 'all', label: 'Tất cả trạng thái' }, { value: 'on', label: 'Đang bán' }, { value: 'off', label: 'Ngừng bán' }]} />
            </Space>
            <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns}
                   pagination={{ pageSize: 8 }} scroll={{ x: 1000 }} />
        </Card>
    )
}