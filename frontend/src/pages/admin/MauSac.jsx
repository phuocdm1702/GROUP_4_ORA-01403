import { useEffect, useState } from 'react'
import { Table, Button, Modal, Form, Input, Select, ColorPicker, Space, Tag, Popconfirm, Card, App } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import api from '../../api/axios'

export default function MauSac() {
    const { message } = App.useApp()
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(false)
    const [open, setOpen] = useState(false)
    const [editing, setEditing] = useState(null)
    const [search, setSearch] = useState('')
    const [form] = Form.useForm()

    const load = async () => {
        setLoading(true)
        try {
            const res = await api.get('/admin/mau-sac')
            setData(res.data)
        } catch {
            message.error('Không tải được dữ liệu. Kiểm tra backend đã chạy chưa.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    const openForm = (record) => {
        setEditing(record)
        form.resetFields()
        form.setFieldsValue(record ?? { maHex: '#1f2a44', trangThai: true })
        setOpen(true)
    }

    const submit = async () => {
        const values = await form.validateFields()
        const hex = typeof values.maHex === 'string' ? values.maHex : values.maHex?.toHexString()
        const body = { ...values, maHex: hex }
        try {
            if (editing) await api.put(`/admin/mau-sac/${editing.id}`, body)
            else await api.post('/admin/mau-sac', body)
            message.success(editing ? 'Cập nhật thành công' : 'Thêm mới thành công')
            setOpen(false)
            load()
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Có lỗi xảy ra')
        }
    }

    const remove = async (id) => {
        try {
            await api.delete(`/admin/mau-sac/${id}`)
            message.success('Đã xóa')
            load()
        } catch {
            message.error('Không xóa được (màu đang được sản phẩm sử dụng?)')
        }
    }

    const filtered = data.filter(d =>
        ((d.tenMau ?? '') + (d.maMau ?? '')).toLowerCase().includes(search.toLowerCase()))

    const columns = [
        { title: 'STT', width: 70, render: (_, __, i) => i + 1 },
        { title: 'Mã màu', dataIndex: 'maMau', width: 120 },
        { title: 'Tên màu', dataIndex: 'tenMau', width: 180 },
        {
            title: 'Màu', dataIndex: 'maHex', width: 160,
            render: (hex) => (
                <Space>
                    <span style={{ width: 22, height: 22, borderRadius: '50%', background: hex,
                        border: '1px solid #d9d9d9', display: 'inline-block' }} />
                    {hex}
                </Space>
            ),
        },
        { title: 'Mô tả', dataIndex: 'moTa' },
        {
            title: 'Trạng thái', dataIndex: 'trangThai', width: 130,
            render: (v) => v ? <Tag color="green">Hoạt động</Tag> : <Tag>Ngừng</Tag>,
        },
        {
            title: 'Thao tác', width: 120,
            render: (_, r) => (
                <Space>
                    <Button icon={<EditOutlined />} onClick={() => openForm(r)} />
                    <Popconfirm title="Xóa màu này?" okText="Xóa" cancelText="Hủy" onConfirm={() => remove(r.id)}>
                        <Button danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                </Space>
            ),
        },
    ]

    return (
        <Card title="Quản lý màu sắc"
              extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => openForm(null)}>Thêm màu</Button>}>
            <Input.Search placeholder="Tìm theo mã hoặc tên màu" allowClear
                          style={{ maxWidth: 320, marginBottom: 16 }} onChange={e => setSearch(e.target.value)} />
            <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns}
                   pagination={{ pageSize: 8 }} />

            <Modal title={editing ? 'Sửa màu sắc' : 'Thêm màu sắc'} open={open} destroyOnClose
                   onOk={submit} onCancel={() => setOpen(false)} okText="Lưu" cancelText="Hủy">
                <Form form={form} layout="vertical">
                    <Form.Item name="maMau" label="Mã màu" rules={[{ required: true, message: 'Nhập mã màu' }]}>
                        <Input placeholder="VD: MAU06" />
                    </Form.Item>
                    <Form.Item name="tenMau" label="Tên màu" rules={[{ required: true, message: 'Nhập tên màu' }]}>
                        <Input placeholder="VD: Xanh navy" />
                    </Form.Item>
                    <Form.Item name="maHex" label="Chọn màu">
                        <ColorPicker showText format="hex" />
                    </Form.Item>
                    <Form.Item name="moTa" label="Mô tả"><Input.TextArea rows={2} /></Form.Item>
                    <Form.Item name="trangThai" label="Trạng thái">
                        <Select options={[{ value: true, label: 'Hoạt động' }, { value: false, label: 'Ngừng' }]} />
                    </Form.Item>
                </Form>
            </Modal>
        </Card>
    )
}