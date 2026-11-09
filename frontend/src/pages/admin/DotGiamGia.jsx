import { useEffect, useMemo, useState } from 'react'
import {
    Card, Table, Button, Input, Select, DatePicker, Space, Tag,
    Modal, Form, InputNumber, Switch, message, Tooltip, Row, Col, Divider
} from 'antd'
import {
    PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined,
    StopOutlined, CheckCircleOutlined
} from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../../api/axios'

const { RangePicker } = DatePicker

const formatVND = (val) =>
    val != null ? Number(val).toLocaleString('vi-VN') + ' ₫' : '0 ₫'

export default function DotGiamGia() {
    const [data, setData] = useState([])
    const [allVariants, setAllVariants] = useState([])
    const [loading, setLoading] = useState(false)

    // Bộ lọc theo đúng video [06:55 - 07:10]: Mã/Tên, Trạng thái, Ngày
    const [filters, setFilters] = useState({
        keyword: '',
        trangThaiLoc: '',
        dateRange: null,
    })

    // Modal Thêm / Sửa đợt giảm giá & Chọn sản phẩm
    const [modalOpen, setModalOpen] = useState(false)
    const [editingRecord, setEditingRecord] = useState(null)
    const [selectedProductIds, setSelectedProductIds] = useState([])
    const [selectedVariantIds, setSelectedVariantIds] = useState([])
    const [productSearchKey, setProductSearchKey] = useState('')

    const [form] = Form.useForm()
    const phanTramValue = Form.useWatch('phanTramGiam', form) || 0

    const fetchData = async () => {
        setLoading(true)
        try {
            const params = {}
            if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
            if (filters.trangThaiLoc) params.trangThaiLoc = filters.trangThaiLoc
            if (filters.dateRange?.[0]) params.tuNgay = filters.dateRange[0].format('YYYY-MM-DD')
            if (filters.dateRange?.[1]) params.denNgay = filters.dateRange[1].format('YYYY-MM-DD')

            const [resList, resVariants] = await Promise.all([
                api.get('/admin/dot-giam-gia', { params }),
                api.get('/admin/dot-giam-gia/bien-the-san-pham'),
            ])
            setData(resList.data)
            setAllVariants(resVariants.data)
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể tải dữ liệu đợt giảm giá')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchData()
    }, [filters.trangThaiLoc, filters.dateRange])

    // Gom nhóm danh sách Sản phẩm cha từ danh sách biến thể (Video [07:35])
    const productList = useMemo(() => {
        const map = new Map()
        allVariants.forEach((v) => {
            if (!map.has(v.idSanPham)) {
                map.set(v.idSanPham, {
                    idSanPham: v.idSanPham,
                    maAo: v.maAo,
                    tenAo: v.tenAo,
                    soBienThe: 0,
                })
            }
            map.get(v.idSanPham).soBienThe += 1
        })
        const arr = Array.from(map.values())
        if (!productSearchKey.trim()) return arr
        const kw = productSearchKey.trim().toLowerCase()
        return arr.filter(
            (p) => p.maAo.toLowerCase().includes(kw) || p.tenAo.toLowerCase().includes(kw)
        )
    }, [allVariants, productSearchKey])

    // Khi tích chọn sản phẩm ở bảng trên -> Hiển thị chi tiết biến thể của sản phẩm đó ở bảng dưới (Video [07:40])
    const visibleVariants = useMemo(() => {
        if (selectedProductIds.length === 0) return []
        return allVariants.filter((v) => selectedProductIds.includes(v.idSanPham))
    }, [allVariants, selectedProductIds])

    const openCreateModal = () => {
        setEditingRecord(null)
        setSelectedProductIds([])
        setSelectedVariantIds([])
        setProductSearchKey('')
        form.resetFields()
        form.setFieldsValue({
            phanTramGiam: 10,
            trangThai: true,
            ngayApDung: [dayjs(), dayjs().add(15, 'day')],
        })
        setModalOpen(true)
    }

    const openEditModal = (record) => {
        setEditingRecord(record)
        setProductSearchKey('')
        const appliedVariantIds = record.idSanPhamChiTietList || []
        setSelectedVariantIds(appliedVariantIds)

        const parentIds = [
            ...new Set(
                allVariants
                    .filter((v) => appliedVariantIds.includes(v.id))
                    .map((v) => v.idSanPham)
            ),
        ]
        setSelectedProductIds(parentIds)

        form.setFieldsValue({
            maKhuyenMai: record.maKhuyenMai,
            tenKhuyenMai: record.tenKhuyenMai,
            phanTramGiam: record.phanTramGiam,
            ngayApDung: [dayjs(record.ngayBatDau), dayjs(record.ngayKetThuc)],
            moTa: record.moTa,
            trangThai: record.trangThai,
        })
        setModalOpen(true)
    }

    // Khi chọn/bỏ chọn Sản phẩm cha -> Tự động chọn/bỏ chọn các biến thể con tương ứng
    const handleProductSelectionChange = (newSelectedProductIds) => {
        const addedProducts = newSelectedProductIds.filter((id) => !selectedProductIds.includes(id))
        const addedVariantIds = allVariants
            .filter((v) => addedProducts.includes(v.idSanPham))
            .map((v) => v.id)

        const remainingVariantIds = selectedVariantIds.filter((vid) => {
            const found = allVariants.find((v) => v.id === vid)
            return found && newSelectedProductIds.includes(found.idSanPham)
        })

        setSelectedProductIds(newSelectedProductIds)
        setSelectedVariantIds([...new Set([...remainingVariantIds, ...addedVariantIds])])
    }

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields()
            const payload = {
                maKhuyenMai: values.maKhuyenMai?.trim() || null,
                tenKhuyenMai: values.tenKhuyenMai.trim(),
                phanTramGiam: values.phanTramGiam,
                ngayBatDau: values.ngayApDung[0].format('YYYY-MM-DD'),
                ngayKetThuc: values.ngayApDung[1].format('YYYY-MM-DD'),
                moTa: values.moTa?.trim() || '',
                trangThai: values.trangThai,
                idSanPhamChiTietList: selectedVariantIds,
            }

            if (editingRecord) {
                await api.put(`/admin/dot-giam-gia/${editingRecord.id}`, payload)
                message.success('Cập nhật đợt giảm giá thành công!')
            } else {
                await api.post('/admin/dot-giam-gia', payload)
                message.success('Thêm đợt giảm giá thành công!')
            }
            setModalOpen(false)
            fetchData()
        } catch (err) {
            if (err.response?.data?.message) {
                message.error(err.response.data.message)
            }
        }
    }

    // Dùng api.put thay cho api.patch để tránh lỗi CORS 403
    const handleToggleStatus = async (record) => {
        try {
            await api.put(`/admin/dot-giam-gia/${record.id}/toggle-status`)
            message.success('Đã cập nhật trạng thái đợt giảm giá!')
            fetchData()
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể đổi trạng thái')
        }
    }

    const renderStatusTag = (status) => {
        switch (status) {
            case 'DANG_DIEN_RA':
                return <Tag color="green">Đang diễn ra</Tag>
            case 'SAP_DIEN_RA':
                return <Tag color="blue">Sắp diễn ra</Tag>
            case 'DA_KET_THUC':
                return <Tag color="default">Đã kết thúc</Tag>
            default:
                return <Tag color="red">Ngừng hoạt động</Tag>
        }
    }

    const columns = [
        { title: 'Mã đợt', dataIndex: 'maKhuyenMai', key: 'maKhuyenMai', width: 110, render: (t) => <b>{t}</b> },
        { title: 'Tên đợt giảm giá', dataIndex: 'tenKhuyenMai', key: 'tenKhuyenMai' },
        {
            title: 'Mức giảm (%)',
            dataIndex: 'phanTramGiam',
            key: 'phanTramGiam',
            render: (v) => <Tag color="volcano" style={{ fontWeight: 600 }}>Giảm {v}%</Tag>,
        },
        {
            title: 'Biến thể áp dụng',
            dataIndex: 'soBienTheApDung',
            key: 'soBienTheApDung',
            render: (v) => <Tag color="geekblue">{v} sản phẩm chi tiết</Tag>,
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
                    <Tooltip title="Xem / Chỉnh sửa">
                        <Button icon={<EditOutlined />} size="small" onClick={() => openEditModal(record)} />
                    </Tooltip>
                    <Tooltip title={record.trangThai ? 'Ngừng hoạt động' : 'Kích hoạt lại'}>
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
                title="Quản lý Đợt giảm giá"
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
                        Thêm đợt giảm giá
                    </Button>
                }
            >
                <Row gutter={[16, 16]}>
                    <Col xs={24} md={8}>
                        <Input
                            placeholder="Tìm theo mã đợt hoặc tên đợt giảm giá..."
                            value={filters.keyword}
                            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
                            onPressEnter={fetchData}
                            suffix={<SearchOutlined onClick={fetchData} style={{ cursor: 'pointer' }} />}
                        />
                    </Col>
                    <Col xs={12} md={6}>
                        <Select
                            style={{ width: '100%' }}
                            value={filters.trangThaiLoc}
                            onChange={(v) => setFilters({ ...filters, trangThaiLoc: v })}
                            options={[
                                { value: '', label: 'Tất cả trạng thái' },
                                { value: 'DANG_DIEN_RA', label: 'Đang diễn ra' },
                                { value: 'SAP_DIEN_RA', label: 'Sắp diễn ra' },
                                { value: 'DA_KET_THUC', label: 'Đã kết thúc' },
                                { value: 'NGUNG_HOAT_DONG', label: 'Ngừng hoạt động' },
                            ]}
                        />
                    </Col>
                    <Col xs={12} md={7}>
                        <RangePicker
                            style={{ width: '100%' }}
                            format="DD/MM/YYYY"
                            value={filters.dateRange}
                            onChange={(dates) => setFilters({ ...filters, dateRange: dates })}
                        />
                    </Col>
                    <Col xs={24} md={3}>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() => setFilters({ keyword: '', trangThaiLoc: '', dateRange: null })}
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

            {/* Modal Thêm / Cập nhật Đợt giảm giá & Chọn sản phẩm chi tiết */}
            <Modal
                title={editingRecord ? `Cập nhật đợt giảm giá: ${editingRecord.maKhuyenMai}` : 'Thêm đợt giảm giá mới'}
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                onOk={handleSubmit}
                okText="Lưu đợt giảm giá"
                cancelText="Hủy"
                width={980}
            >
                <Form form={form} layout="vertical">
                    <Row gutter={16}>
                        <Col span={6}>
                            <Form.Item label="Mã đợt (Bỏ trống tự sinh)" name="maKhuyenMai">
                                <Input placeholder="Tự động sinh..." disabled={!!editingRecord} />
                            </Form.Item>
                        </Col>
                        <Col span={10}>
                            <Form.Item
                                label="Tên đợt giảm giá"
                                name="tenKhuyenMai"
                                rules={[{ required: true, whitespace: true, message: 'Vui lòng không để trống tên đợt!' }]}
                            >
                                <Input placeholder="Nhập tên đợt giảm giá..." />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                label="Phần trăm giảm (%)"
                                name="phanTramGiam"
                                rules={[
                                    { required: true, message: 'Vui lòng nhập % giảm!' },
                                    { type: 'number', min: 1, max: 100, message: 'Chỉ được giảm từ 1% đến 100%!' },
                                ]}
                            >
                                <InputNumber style={{ width: '100%' }} min={1} max={100} addonAfter="%" />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Row gutter={16}>
                        <Col span={12}>
                            <Form.Item
                                label="Thời gian áp dụng (Ngày bắt đầu - Ngày kết thúc)"
                                name="ngayApDung"
                                rules={[{ required: true, message: 'Vui lòng chọn ngày áp dụng!' }]}
                            >
                                <RangePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item label="Mô tả" name="moTa">
                                <Input placeholder="Mô tả đợt giảm giá..." />
                            </Form.Item>
                        </Col>
                        <Col span={4}>
                            <Form.Item label="Trạng thái" name="trangThai" valuePropName="checked">
                                <Switch checkedChildren="Bật" unCheckedChildren="Tắt" />
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>

                <Divider orientation="left">1. Chọn sản phẩm áp dụng</Divider>
                <Input
                    placeholder="Tìm nhanh sản phẩm theo mã áo hoặc tên áo..."
                    value={productSearchKey}
                    onChange={(e) => setProductSearchKey(e.target.value)}
                    style={{ marginBottom: 10, maxWidth: 360 }}
                    prefix={<SearchOutlined />}
                />
                <Table
                    rowKey="idSanPham"
                    size="small"
                    pagination={{ pageSize: 5 }}
                    dataSource={productList}
                    rowSelection={{
                        selectedRowKeys: selectedProductIds,
                        onChange: handleProductSelectionChange,
                    }}
                    columns={[
                        { title: 'Mã sản phẩm', dataIndex: 'maAo', width: 140, render: (t) => <b>{t}</b> },
                        { title: 'Tên sản phẩm', dataIndex: 'tenAo' },
                        { title: 'Số lượng biến thể', dataIndex: 'soBienThe', width: 150, render: (v) => `${v} biến thể` },
                    ]}
                />

                {selectedProductIds.length > 0 && (
                    <>
                        <Divider orientation="left">
                            2. Chi tiết sản phẩm của sản phẩm đã chọn (Đã tích chọn: {selectedVariantIds.length} biến thể)
                        </Divider>
                        <Table
                            rowKey="id"
                            size="small"
                            pagination={{ pageSize: 5 }}
                            dataSource={visibleVariants}
                            rowSelection={{
                                selectedRowKeys: selectedVariantIds,
                                onChange: (keys) => setSelectedVariantIds(keys),
                            }}
                            columns={[
                                { title: 'Mã chi tiết', dataIndex: 'maAoChiTiet', render: (t) => <b>{t}</b> },
                                { title: 'Tên sản phẩm', dataIndex: 'tenAo' },
                                { title: 'Màu sắc', dataIndex: 'tenMau' },
                                { title: 'Kích cỡ', dataIndex: 'tenKichThuoc' },
                                { title: 'Chất liệu', dataIndex: 'tenChatLieu' },
                                {
                                    title: 'Giá gốc',
                                    dataIndex: 'giaBanBanDau',
                                    render: (v) => (
                                        <span style={{ textDecoration: 'line-through', color: '#888' }}>
                                            {formatVND(v)}
                                        </span>
                                    ),
                                },
                                {
                                    title: `Giá sau giảm (${phanTramValue}%)`,
                                    key: 'giaSauGiam',
                                    render: (_, r) => {
                                        const after = Math.round(Number(r.giaBanBanDau) * (1 - Number(phanTramValue) / 100))
                                        return <b style={{ color: '#cf1322' }}>{formatVND(after)}</b>
                                    },
                                },
                                { title: 'Tồn kho', dataIndex: 'soLuong' },
                            ]}
                        />
                    </>
                )}
            </Modal>
        </Space>
    )
}