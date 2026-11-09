import { useEffect, useState } from 'react'
import {
    Card, Table, Button, Input, Select, DatePicker, Space, Tag,
    Modal, Form, InputNumber, Switch, message, Tooltip, Row, Col
} from 'antd'
import {
    PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, StopOutlined, CheckCircleOutlined
} from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../../api/axios'

const { RangePicker } = DatePicker

const formatVND = (val) =>
    val != null ? Number(val).toLocaleString('vi-VN') + ' ₫' : '0 ₫'

export default function PhieuGiamGia() {
    const [data, setData] = useState([])
    const [loading, setLoading] = useState(false)
    const [filters, setFilters] = useState({
        keyword: '',
        loaiGiamGia: '',
        trangThaiLoc: '',
        dateRange: null,
    })

    const [modalOpen, setModalOpen] = useState(false)
    const [editingRecord, setEditingRecord] = useState(null)
    const [form] = Form.useForm()
    const loaiGiamGiaValue = Form.useWatch('loaiGiamGia', form)

    const fetchData = async () => {
        setLoading(true)
        try {
            const params = {}
            if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
            if (filters.loaiGiamGia) params.loaiGiamGia = filters.loaiGiamGia
            if (filters.trangThaiLoc) params.trangThaiLoc = filters.trangThaiLoc
            if (filters.dateRange?.[0]) params.tuNgay = filters.dateRange[0].format('YYYY-MM-DD')
            if (filters.dateRange?.[1]) params.denNgay = filters.dateRange[1].format('YYYY-MM-DD')

            const res = await api.get('/admin/phieu-giam-gia', { params })
            setData(res.data)
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể tải danh sách phiếu giảm giá')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchData()
    }, [filters.loaiGiamGia, filters.trangThaiLoc, filters.dateRange])

    const openCreateModal = () => {
        setEditingRecord(null)
        form.resetFields()
        form.setFieldsValue({
            loaiGiamGia: 'PHAN_TRAM',
            soLuong: 100,
            giaTriToiThieu: 100000,
            toiDaGiamGia: 50000,
            trangThai: true,
            ngayApDung: [dayjs(), dayjs().add(15, 'day')],
        })
        setModalOpen(true)
    }

    const openEditModal = (record) => {
        setEditingRecord(record)
        form.setFieldsValue({
            maGiamGia: record.maGiamGia,
            tenGiamGia: record.tenGiamGia,
            loaiGiamGia: record.loaiGiamGia,
            giaTriGiam: record.giaTriGiam,
            toiDaGiamGia: record.toiDaGiamGia,
            giaTriToiThieu: record.giaTriToiThieu,
            soLuong: record.soLuong,
            ngayApDung: [dayjs(record.ngayBatDau), dayjs(record.ngayKetThuc)],
            moTa: record.moTa,
            trangThai: record.trangThai,
        })
        setModalOpen(true)
    }

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            const payload = {
                maGiamGia: values.maGiamGia?.trim() || null,
                tenGiamGia: values.tenGiamGia.trim(),
                loaiGiamGia: values.loaiGiamGia,
                giaTriGiam: values.giaTriGiam,
                toiDaGiamGia: values.loaiGiamGia === 'PHAN_TRAM' ? values.toiDaGiamGia : values.giaTriGiam,
                giaTriToiThieu: values.giaTriToiThieu,
                soLuong: values.soLuong,
                ngayBatDau: values.ngayApDung[0].format('YYYY-MM-DD'),
                ngayKetThuc: values.ngayApDung[1].format('YYYY-MM-DD'),
                moTa: values.moTa?.trim() || '',
                trangThai: values.trangThai,
            }

            if (editingRecord) {
                await api.put(`/admin/phieu-giam-gia/${editingRecord.id}`, payload)
                message.success('Cập nhật phiếu giảm giá thành công!')
            } else {
                await api.post('/admin/phieu-giam-gia', payload)
                message.success('Thêm phiếu giảm giá thành công!')
            }
            setModalOpen(false)
            fetchData()
        } catch (err) {
            if (err.response?.data?.message) {
                message.error(err.response.data.message)
            }
        }
    }

    // Sử dụng api.put thay cho api.patch để tránh lỗi CORS 403
    const handleToggleStatus = async (record) => {
        try {
            await api.put(`/admin/phieu-giam-gia/${record.id}/toggle-status`)
            message.success('Đã cập nhật trạng thái phiếu giảm giá!')
            fetchData()
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể đổi trạng thái')
        }
    }

    const renderStatusTag = (status) => {
        switch (status) {
            case 'DANG_HOAT_DONG':
                return <Tag color="green">Đang hoạt động</Tag>
            case 'SAP_DIEN_RA':
                return <Tag color="blue">Sắp diễn ra</Tag>
            case 'DA_KET_THUC':
                return <Tag color="default">Đã kết thúc</Tag>
            default:
                return <Tag color="red">Ngừng hoạt động</Tag>
        }
    }

    const columns = [
        { title: 'Mã', dataIndex: 'maGiamGia', key: 'maGiamGia', width: 100, render: (t) => <b>{t}</b> },
        { title: 'Tên phiếu giảm giá', dataIndex: 'tenGiamGia', key: 'tenGiamGia' },
        {
            title: 'Loại giảm',
            dataIndex: 'loaiGiamGia',
            key: 'loaiGiamGia',
            render: (v) => (v === 'PHAN_TRAM' ? <Tag color="purple">Phần trăm (%)</Tag> : <Tag color="cyan">Số tiền (VNĐ)</Tag>),
        },
        {
            title: 'Giá trị giảm',
            key: 'giaTriGiam',
            render: (_, r) =>
                r.loaiGiamGia === 'PHAN_TRAM'
                    ? `${r.giaTriGiam}% (Tối đa ${formatVND(r.toiDaGiamGia)})`
                    : formatVND(r.giaTriGiam),
        },
        { title: 'Đơn tối thiểu', dataIndex: 'giaTriToiThieu', key: 'giaTriToiThieu', render: formatVND },
        {
            title: 'Còn lại / Tổng',
            key: 'soLuong',
            render: (_, r) => `${r.soLuong - r.soLuongDaDung} / ${r.soLuong} (Đã dùng: ${r.soLuongDaDung})`,
        },
        {
            title: 'Thời gian áp dụng',
            key: 'thoiGian',
            render: (_, r) => `${dayjs(r.ngayBatDau).format('DD/MM/YYYY')} - ${dayjs(r.ngayKetThuc).format('DD/MM/YYYY')}`,
        },
        { title: 'Trạng thái', dataIndex: 'trangThaiHienThi', key: 'trangThaiHienThi', render: renderStatusTag },
        {
            title: 'Thao tác',
            key: 'action',
            width: 110,
            render: (_, record) => (
                <Space>
                    <Tooltip title="Chỉnh sửa">
                        <Button icon={<EditOutlined />} size="small" onClick={() => openEditModal(record)} />
                    </Tooltip>
                    <Tooltip title={record.trangThai ? 'Ngừng hoạt động tức tốc' : 'Kích hoạt lại'}>
                        <Button
                            danger={record.trangThai}
                            icon={record.trangThai ? <StopOutlined /> : <CheckCircleOutlined />}
                            size="small"
                            onClick={() => handleToggleStatus(record)}
                        />
                    </Tooltip>
                </Space>
            ),
        },
    ]

    return (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Card
                title="Quản lý Phiếu giảm giá"
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                        Thêm phiếu giảm giá
                    </Button>
                }
            >
                <Row gutter={[16, 16]}>
                    <Col xs={24} md={7}>
                        <Input
                            placeholder="Tìm theo mã hoặc tên phiếu giảm giá..."
                            value={filters.keyword}
                            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
                            onPressEnter={fetchData}
                            suffix={<SearchOutlined onClick={fetchData} style={{ cursor: 'pointer' }} />}
                        />
                    </Col>
                    <Col xs={12} md={4}>
                        <Select
                            style={{ width: '100%' }}
                            value={filters.loaiGiamGia}
                            onChange={(v) => setFilters({ ...filters, loaiGiamGia: v })}
                            options={[
                                { value: '', label: 'Tất cả loại giảm' },
                                { value: 'PHAN_TRAM', label: 'Theo phần trăm (%)' },
                                { value: 'SO_TIEN', label: 'Theo tiền (VNĐ)' },
                            ]}
                        />
                    </Col>
                    <Col xs={12} md={5}>
                        <Select
                            style={{ width: '100%' }}
                            value={filters.trangThaiLoc}
                            onChange={(v) => setFilters({ ...filters, trangThaiLoc: v })}
                            options={[
                                { value: '', label: 'Tất cả trạng thái' },
                                { value: 'DANG_HOAT_DONG', label: 'Đang hoạt động' },
                                { value: 'SAP_DIEN_RA', label: 'Sắp diễn ra' },
                                { value: 'DA_KET_THUC', label: 'Đã kết thúc / Hết lượt' },
                                { value: 'NGUNG_HOAT_DONG', label: 'Ngừng hoạt động' },
                            ]}
                        />
                    </Col>
                    <Col xs={18} md={6}>
                        <RangePicker
                            style={{ width: '100%' }}
                            format="DD/MM/YYYY"
                            value={filters.dateRange}
                            onChange={(dates) => setFilters({ ...filters, dateRange: dates })}
                        />
                    </Col>
                    <Col xs={6} md={2}>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() => setFilters({ keyword: '', loaiGiamGia: '', trangThaiLoc: '', dateRange: null })}
                        >
                            Mặc định
                        </Button>
                    </Col>
                </Row>
            </Card>

            <Card>
                <Table
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={data}
                    pagination={{ pageSizeOptions: ['5', '10', '20', '30'], showSizeChanger: true, defaultPageSize: 10 }}
                />
            </Card>

            <Modal
                title={editingRecord ? `Cập nhật phiếu giảm giá: ${editingRecord.maGiamGia}` : 'Thêm phiếu giảm giá mới'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleSubmit}
                okText="Lưu phiếu giảm giá"
                cancelText="Hủy"
                width={650}
            >
                <Form form={form} layout="vertical">
                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item label="Mã phiếu (Bỏ trống tự sinh)" name="maGiamGia">
                                <Input placeholder="Tự động sinh..." disabled={!!editingRecord} />
                            </Form.Item>
                        </Col>
                        <Col span={16}>
                            <Form.Item
                                label="Tên phiếu giảm giá"
                                name="tenGiamGia"
                                rules={[{ required: true, whitespace: true, message: 'Vui lòng nhập tên phiếu giảm giá!' }]}
                            >
                                <Input placeholder="Nhập tên phiếu giảm giá..." />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={16}>
                        <Col span={8}>
                            <Form.Item label="Loại giảm giá" name="loaiGiamGia" rules={[{ required: true }]}>
                                <Select
                                    options={[
                                        { value: 'PHAN_TRAM', label: 'Giảm theo %' },
                                        { value: 'SO_TIEN', label: 'Giảm theo số tiền (VNĐ)' },
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label={loaiGiamGiaValue === 'PHAN_TRAM' ? 'Mức giảm (%)' : 'Số tiền giảm (VNĐ)'}
                                name="giaTriGiam"
                                rules={[
                                    { required: true, message: 'Bắt buộc nhập!' },
                                    {
                                        validator: (_, val) => {
                                            if (loaiGiamGiaValue === 'PHAN_TRAM' && (val < 1 || val > 100))
                                                return Promise.reject('Phần trăm giảm phải từ 1 đến 100!')
                                            if (loaiGiamGiaValue === 'SO_TIEN' && val < 1000)
                                                return Promise.reject('Số tiền giảm phải từ 1.000đ!')
                                            return Promise.resolve()
                                        },
                                    },
                                ]}
                            >
                                <InputNumber style={{ width: '100%' }} min={1} />
                            </Form.Item>
                        </Col>
                        {loaiGiamGiaValue === 'PHAN_TRAM' && (
                            <Col span={8}>
                                <Form.Item
                                    label="Giảm tối đa (VNĐ)"
                                    name="toiDaGiamGia"
                                    rules={[
                                        { required: true, message: 'Nhập mức giảm tối đa!' },
                                        { type: 'number', min: 1000, message: 'Tối thiểu từ 1.000đ!' },
                                    ]}
                                >
                                    <InputNumber style={{ width: '100%' }} min={1000} step={1000} />
                                </Form.Item>
                            </Col>
                        )}
                    </Row>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="Đơn hàng tối thiểu (VNĐ)"
                                name="giaTriToiThieu"
                                rules={[
                                    { required: true, message: 'Bắt buộc nhập!' },
                                    { type: 'number', min: 1000, message: 'Đơn tối thiểu phải từ 1.000đ!' },
                                ]}
                            >
                                <InputNumber style={{ width: '100%' }} min={1000} step={10000} />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                label="Số lượng phát hành"
                                name="soLuong"
                                rules={[
                                    { required: true, message: 'Bắt buộc nhập!' },
                                    { type: 'number', min: 1, message: 'Số lượng phải lớn hơn 0!' },
                                ]}
                            >
                                <InputNumber style={{ width: '100%' }} min={1} />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="Thời gian áp dụng (Ngày bắt đầu - Ngày kết thúc)"
                        name="ngayApDung"
                        rules={[{ required: true, message: 'Vui lòng chọn khoảng ngày áp dụng!' }]}
                    >
                        <RangePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
                    </Form.Item>

                    <Form.Item label="Mô tả" name="moTa">
                        <Input.TextArea rows={2} placeholder="Mô tả điều kiện áp dụng..." />
                    </Form.Item>

                    <Form.Item label="Trạng thái kích hoạt" name="trangThai" valuePropName="checked">
                        <Switch checkedChildren="Hoạt động" unCheckedChildren="Ngừng" />
                    </Form.Item>
                </Form>
            </Modal>
        </Space>
    )
}