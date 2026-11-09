import { useEffect, useMemo, useState } from 'react'
import { Table, Button, Modal, Form, Input, Select, DatePicker, Space, Tag, Switch, Card, Row, Col, App } from 'antd'
import { PlusOutlined, EditOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../../api/axios'

const PHONE = /^(0|\+84)\d{9}$/

export default function NhanVien() {
    const { message } = App.useApp()
    const [data, setData] = useState([])
    const [roles, setRoles] = useState([])
    const [loading, setLoading] = useState(false)
    const [open, setOpen] = useState(false)
    const [editing, setEditing] = useState(null)
    const [saving, setSaving] = useState(false)
    const [search, setSearch] = useState('')
    const [roleFilter, setRoleFilter] = useState('all')
    const [statusFilter, setStatusFilter] = useState('all')
    const [form] = Form.useForm()

    const load = async () => {
        setLoading(true)
        try {
            const [nv, vt] = await Promise.all([api.get('/admin/nhan-vien'), api.get('/admin/vai-tro')])
            setData(nv.data)
            setRoles(vt.data)
        } catch {
            message.error('Không tải được dữ liệu. Kiểm tra backend đã chạy chưa.')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { load() }, [])

    const openForm = record => {
        setEditing(record)
        form.resetFields()
        form.setFieldsValue(record
            ? { ...record, ngaySinh: record.ngaySinh ? dayjs(record.ngaySinh) : null, matKhau: undefined }
            : { trangThai: true })
        setOpen(true)
    }

    const submit = async () => {
        const values = await form.validateFields()
        const body = {
            ...values,
            ngaySinh: values.ngaySinh ? values.ngaySinh.format('YYYY-MM-DD') : null,
            matKhau: values.matKhau || undefined,
        }
        setSaving(true)
        try {
            if (editing) await api.put(`/admin/nhan-vien/${editing.id}`, body)
            else await api.post('/admin/nhan-vien', body)
            message.success(editing ? 'Cập nhật nhân viên thành công' : 'Thêm nhân viên thành công')
            setOpen(false)
            load()
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Có lỗi xảy ra')
        } finally {
            setSaving(false)
        }
    }

    const toggle = async (r, value) => {
        try {
            await api.put(`/admin/nhan-vien/${r.id}/trang-thai`, null, { params: { value } })
            message.success(value ? 'Đã mở khóa tài khoản' : 'Đã khóa tài khoản')
        } catch (e) {
            message.error(e.response?.data?.message ?? 'Không đổi được trạng thái')
        }
        load()
    }

    const filtered = useMemo(() => data.filter(d => {
        const text = [d.maNhanVien, d.hoVaTen, d.soDienThoai, d.email, d.tenNguoiDung].join(' ').toLowerCase()
        return text.includes(search.toLowerCase())
            && (roleFilter === 'all' || d.idVaiTro === roleFilter)
            && (statusFilter === 'all' || (statusFilter === 'on' ? d.trangThai : !d.trangThai))
    }), [data, search, roleFilter, statusFilter])

    const columns = [
        { title: 'STT', width: 60, render: (_, __, i) => i + 1 },
        { title: 'Mã', dataIndex: 'maNhanVien', width: 80 },
        { title: 'Họ và tên', dataIndex: 'hoVaTen', width: 170 },
        { title: 'Giới tính', dataIndex: 'gioiTinh', width: 90 },
        { title: 'Số điện thoại', dataIndex: 'soDienThoai', width: 130 },
        { title: 'Email', dataIndex: 'email', width: 200 },
        { title: 'Tên đăng nhập', dataIndex: 'tenNguoiDung', width: 130 },
        {
            title: 'Vai trò', dataIndex: 'tenVaiTro', width: 200,
            render: v => <Tag color={v === 'Quản trị viên' ? 'volcano' : 'blue'}>{v}</Tag>,
        },
        {
            title: 'Trạng thái', dataIndex: 'trangThai', width: 100,
            render: (v, r) => <Switch checked={v} onChange={c => toggle(r, c)} />,
        },
        {
            title: 'Thao tác', width: 90, fixed: 'right',
            render: (_, r) => <Button icon={<EditOutlined />} onClick={() => openForm(r)} />,
        },
    ]

    return (
        <Card title="Quản lý nhân viên"
              extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => openForm(null)}>Thêm nhân viên</Button>}>
            <Space style={{ marginBottom: 16 }} wrap>
                <Input.Search placeholder="Tìm theo mã, tên, SĐT, email..." allowClear style={{ width: 320 }}
                              onChange={e => setSearch(e.target.value)} />
                <Select value={roleFilter} onChange={setRoleFilter} style={{ width: 230 }}
                        options={[{ value: 'all', label: 'Tất cả vai trò' }, ...roles.map(r => ({ value: r.id, label: r.tenVaiTro }))]} />
                <Select value={statusFilter} onChange={setStatusFilter} style={{ width: 170 }}
                        options={[{ value: 'all', label: 'Tất cả trạng thái' }, { value: 'on', label: 'Đang hoạt động' }, { value: 'off', label: 'Đã khóa' }]} />
            </Space>
            <Table rowKey="id" loading={loading} dataSource={filtered} columns={columns}
                   pagination={{ pageSize: 8 }} scroll={{ x: 1200 }} />

            <Modal title={editing ? `Sửa nhân viên ${editing.maNhanVien}` : 'Thêm nhân viên'} open={open} forceRender
                   width={720} onOk={submit} confirmLoading={saving} onCancel={() => setOpen(false)}
                   okText="Lưu" cancelText="Hủy">
                <Form form={form} layout="vertical">
                    <Row gutter={16}>
                        <Col xs={24} md={12}>
                            <Form.Item name="hoVaTen" label="Họ và tên" rules={[{ required: true, message: 'Nhập họ và tên' }]}>
                                <Input maxLength={100} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="idVaiTro" label="Vai trò" rules={[{ required: true, message: 'Chọn vai trò' }]}>
                                <Select placeholder="Chọn vai trò" options={roles.map(r => ({ value: r.id, label: r.tenVaiTro }))} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="gioiTinh" label="Giới tính">
                                <Select allowClear placeholder="Chọn giới tính"
                                        options={['Nam', 'Nữ', 'Khác'].map(v => ({ value: v, label: v }))} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="ngaySinh" label="Ngày sinh"
                                       rules={[{
                                           validator: (_, v) => !v || !v.isAfter(dayjs().subtract(18, 'year'))
                                               ? Promise.resolve() : Promise.reject(new Error('Nhân viên phải từ 18 tuổi trở lên')),
                                       }]}>
                                <DatePicker format="DD/MM/YYYY" style={{ width: '100%' }} placeholder="DD/MM/YYYY" />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="soDienThoai" label="Số điện thoại"
                                       rules={[{ required: true, message: 'Nhập số điện thoại' }, { pattern: PHONE, message: 'Số điện thoại không hợp lệ' }]}>
                                <Input maxLength={15} placeholder="0912345678" />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="email" label="Email"
                                       rules={[{ required: true, message: 'Nhập email' }, { type: 'email', message: 'Email không hợp lệ' }]}>
                                <Input maxLength={120} />
                            </Form.Item>
                        </Col>
                        <Col span={24}>
                            <Form.Item name="diaChi" label="Địa chỉ">
                                <Input maxLength={255} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="tenNguoiDung" label="Tên đăng nhập"
                                       rules={[
                                           { required: true, message: 'Nhập tên đăng nhập' },
                                           { min: 4, message: 'Tối thiểu 4 ký tự' },
                                           { pattern: /^[A-Za-z0-9._-]+$/, message: 'Chỉ gồm chữ không dấu, số, dấu . _ -' },
                                       ]}>
                                <Input maxLength={60} />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="matKhau" label={editing ? 'Mật khẩu mới (để trống nếu không đổi)' : 'Mật khẩu'}
                                       rules={[{ required: !editing, message: 'Nhập mật khẩu' }, { min: 6, message: 'Tối thiểu 6 ký tự' }]}>
                                <Input.Password maxLength={72} autoComplete="new-password" />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="trangThai" label="Trạng thái">
                                <Select options={[{ value: true, label: 'Đang hoạt động' }, { value: false, label: 'Đã khóa' }]} />
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </Modal>
        </Card>
    )
}