import { useEffect, useMemo, useState } from 'react'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, Tag, Switch, Card, Drawer,
    Descriptions, Popconfirm, Typography, Spin, Row, Col, App } from 'antd'
import { EditOutlined, EyeOutlined, KeyOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../../api/axios'
import { vnd } from '../../data/products'

const PHONE = /^(0|\+84)\d{9}$/
const ORDER_COLOR = {
    'Chờ xác nhận': 'orange', 'Đã xác nhận': 'blue', 'Đang giao hàng': 'geekblue',
    'Đã hoàn thành': 'green', 'Đã hủy': 'red',
}
const fmt = (v, f = 'DD/MM/YYYY') => (v ? dayjs(v).format(f) : '—')

export default function KhachHang() {
    const { message, modal } = App.useApp()
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(false)
    const [search, setSearch] = useState('')
    const [statusFilter, setStatusFilter] = useState('all')

    const [drawerOpen, setDrawerOpen] = useState(false)
    const [detail, setDetail] = useState(null)

    const [editOpen, setEditOpen] = useState(false)
    const [saving, setSaving] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [form] = Form.useForm()

    const load = async () => {
        setLoading(true)
        try {
            const res = await api.get('/admin/khach-hang')
            setData(res.data)
        } catch {
            message.error('Không tải được dữ liệu. Kiểm tra backend đã chạy chưa.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    const loadDetail = async id => {
        try {
            const res = await api.get(`/admin/khach-hang/${id}`)
            setDetail(res.data)
        } catch {
            message.error('Không tải được thông tin khách hàng')
            setDrawerOpen(false)
        }
    }

    const openDetail = id => {
        setDetail(null)
        setDrawerOpen(true)
        loadDetail(id)
    }

    const openEdit = k => {
        setEditingId(k.id)
        form.resetFields()
        form.setFieldsValue({ ...k, ngaySinh: k.ngaySinh ? dayjs(k.ngaySinh) : null })
        setEditOpen(true)
    }

    const submit = async () => {
        const values = await form.validateFields()
        const body = { ...values, ngaySinh: values.ngaySinh ? values.ngaySinh.format('YYYY-MM-DD') : null }
        setSaving(true)
        try {
            await api.put(`/admin/khach-hang/${editingId}`, body)
            message.success('Cập nhật khách hàng thành công')
            setEditOpen(false)
            load()
            if (drawerOpen) loadDetail(editingId)
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Có lỗi xảy ra')
        } finally {
            setSaving(false)
        }
    }

    const toggle = async (r, value) => {
        try {
            await api.put(`/admin/khach-hang/${r.id}/trang-thai`, null, { params: { value } })
            message.success(value ? 'Đã mở khóa tài khoản' : 'Đã khóa tài khoản')
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Không đổi được trạng thái')
        }
        load()
    }

    const resetPassword = async id => {
        try {
            const { data: res } = await api.post(`/admin/khach-hang/${id}/dat-lai-mat-khau`)
            modal.info({
                title: 'Đã đặt lại mật khẩu',
                content: (
                    <div>
                        <p>Mật khẩu tạm của khách hàng:</p>
                        <Typography.Text code copyable style={{ fontSize: 18 }}>{res.matKhauMoi}</Typography.Text>
                        <p style={{ marginTop: 12 }}>Mật khẩu chỉ hiển thị một lần này. Hãy gửi cho khách và yêu cầu đổi lại sau khi đăng nhập.</p>
                    </div>
                ),
            })
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Không đặt lại được mật khẩu')
        }
    }

    const filtered = useMemo(() => data.filter(d => {
        const text = [d.maKhachHang, d.hoVaTen, d.soDienThoai, d.email].join(' ').toLowerCase()
        return text.includes(search.toLowerCase())
            && (statusFilter === 'all' || (statusFilter === 'on' ? d.trangThai : !d.trangThai))
    }), [data, search, statusFilter])

    const columns = [
        { title: 'STT', width: 60, render: (_, __, i) => i + 1 },
        { title: 'Mã', dataIndex: 'maKhachHang', width: 80 },
        { title: 'Họ và tên', dataIndex: 'hoVaTen', width: 170 },
        { title: 'Số điện thoại', dataIndex: 'soDienThoai', width: 130 },
        { title: 'Email', dataIndex: 'email', width: 210 },
        { title: 'Ngày đăng ký', dataIndex: 'ngayTao', width: 130, render: v => fmt(v) },
        { title: 'Số đơn', dataIndex: 'soDon', width: 80, align: 'right' },
        { title: 'Tổng chi tiêu', dataIndex: 'tongChiTieu', width: 140, align: 'right', render: v => vnd(v) },
        {
            title: 'Trạng thái', dataIndex: 'trangThai', width: 100,
            render: (v, r) => <Switch checked={v} onChange={c => toggle(r, c)} />,
        },
        {
            title: 'Thao tác', width: 110, fixed: 'right',
            render: (_, r) => (
                <Space>
                    <Button icon={<EyeOutlined />} onClick={() => openDetail(r.id)} />
                    <Button icon={<EditOutlined />} onClick={() => openEdit(r)} />
                </Space>
            ),
        },
    ]

    const k = detail?.khachHang

    return (
        <Card title="Quản lý khách hàng">
            <Space style={{ marginBottom: 16 }} wrap>
                <Input.Search placeholder="Tìm theo mã, tên, SĐT, email..." allowClear style={{ width: 320 }}
                              onChange={e => setSearch(e.target.value)} />
                <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 170 }}
                        options={[{ value: 'all', label: 'Tất cả trạng thái' }, { value: 'on', label: 'Đang hoạt động' }, { value: 'off', label: 'Đã khóa' }]} />
            </Space>
            <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns}
                   pagination={{ pageSize: 8 }} scroll={{ x: 1150 }} />

            <Drawer title="Thông tin khách hàng" open={drawerOpen} onClose={() => setDrawerOpen(false)} width={580}>
                {!detail ? <Spin /> : (
                    <>
                        <Descriptions column={1} size="small" bordered
                                      items={[
                                          { key: 'ma', label: 'Mã khách hàng', children: k.maKhachHang },
                                          { key: 'ten', label: 'Họ và tên', children: k.hoVaTen },
                                          { key: 'gt', label: 'Giới tính', children: k.gioiTinh ?? '—' },
                                          { key: 'ns', label: 'Ngày sinh', children: fmt(k.ngaySinh) },
                                          { key: 'sdt', label: 'Số điện thoại', children: k.soDienThoai },
                                          { key: 'em', label: 'Email', children: k.email },
                                          { key: 'nt', label: 'Ngày đăng ký', children: fmt(k.ngayTao, 'DD/MM/YYYY HH:mm') },
                                          { key: 'tt', label: 'Trạng thái', children: k.trangThai ? <Tag color="green">Hoạt động</Tag> : <Tag color="red">Đã khóa</Tag> },
                                          { key: 'sd', label: 'Số đơn', children: k.soDon },
                                          { key: 'ct', label: 'Tổng chi tiêu', children: vnd(k.tongChiTieu) },
                                      ]} />

                        <Space style={{ margin: '16px 0' }} wrap>
                            <Button icon={<EditOutlined />} onClick={() => openEdit(k)}>Sửa thông tin</Button>
                            <Popconfirm title="Đặt lại mật khẩu cho khách này?" description="Mật khẩu cũ sẽ không còn dùng được."
                                        okText="Đặt lại" cancelText="Hủy" onConfirm={() => resetPassword(k.id)}>
                                <Button icon={<KeyOutlined />}>Đặt lại mật khẩu</Button>
                            </Popconfirm>
                        </Space>

                        <Typography.Title level={5}>Địa chỉ ({detail.diaChi.length})</Typography.Title>
                        {detail.diaChi.length === 0 && <Typography.Text type="secondary">Chưa có địa chỉ.</Typography.Text>}
                        {detail.diaChi.map(d => (
                            <div key={d.id} style={{ padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
                                {d.diaChiDayDu} {d.macDinh && <Tag color="blue" style={{ marginLeft: 6 }}>Mặc định</Tag>}
                            </div>
                        ))}

                        <Typography.Title level={5} style={{ marginTop: 24 }}>Đơn hàng gần đây</Typography.Title>
                        <Table size="small" rowKey="maHoaDon" pagination={false} dataSource={detail.donGanDay}
                               locale={{ emptyText: 'Chưa có đơn hàng' }}
                               columns={[
                                   { title: 'Mã đơn', dataIndex: 'maHoaDon' },
                                   { title: 'Ngày', dataIndex: 'ngayTao', render: v => fmt(v) },
                                   { title: 'Tổng tiền', dataIndex: 'tongTien', align: 'right', render: v => vnd(v) },
                                   { title: 'Trạng thái', dataIndex: 'trangThai', render: v => <Tag color={ORDER_COLOR[v]}>{v}</Tag> },
                               ]} />
                    </>
                )}
            </Drawer>

            <Modal title="Sửa thông tin khách hàng" open={editOpen} forceRender width={620}
                   onOk={submit} confirmLoading={saving} onCancel={() => setEditOpen(false)} okText="Lưu" cancelText="Hủy">
                <Form form={form} layout="vertical">
                    <Row gutter={16}>
                        <Col span={24}>
                            <Form.Item name="hoVaTen" label="Họ và tên" rules={[{ required: true, message: 'Nhập họ và tên' }]}>
                                <Input maxLength={100} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="gioiTinh" label="Giới tính">
                                <Select allowClear placeholder="Chọn giới tính" options={['Nam', 'Nữ', 'Khác'].map(v => ({ value: v, label: v }))} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="ngaySinh" label="Ngày sinh">
                                <DatePicker format="DD/MM/YYYY" style={{ width: '100%' }} disabledDate={d => d && d.isAfter(dayjs())} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="soDienThoai" label="Số điện thoại"
                                       rules={[{ required: true, message: 'Nhập số điện thoại' }, { pattern: PHONE, message: 'Số điện thoại không hợp lệ' }]}>
                                <Input maxLength={15} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="email" label="Email"
                                       rules={[{ required: true, message: 'Nhập email' }, { type: 'email', message: 'Email không hợp lệ' }]}>
                                <Input maxLength={120} />
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </Modal>
        </Card>
    )
}