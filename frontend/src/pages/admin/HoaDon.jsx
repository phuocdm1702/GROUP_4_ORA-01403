import { useEffect, useState } from 'react'
import {
    Card, Table, Button, Input, Select, DatePicker, Space, Tag, Tabs,
    Modal, Form, InputNumber, Descriptions, Steps, Divider, QRCode,
    Row, Col, message, Popconfirm, Switch
} from 'antd'
import {
    SearchOutlined, ReloadOutlined, EyeOutlined, PrinterOutlined,
    EditOutlined, RollbackOutlined, RetweetOutlined, CheckOutlined, CloseCircleOutlined
} from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../../api/axios'

const { RangePicker } = DatePicker

const formatVND = (val) =>
    val != null ? Number(val).toLocaleString('vi-VN') + ' ₫' : '0 ₫'

const ORDER_STEPS = ['Chờ xác nhận', 'Đã xác nhận', 'Đang giao hàng', 'Đã hoàn thành']

export default function HoaDon() {
    const [orders, setOrders] = useState([])
    const [loading, setLoading] = useState(false)
    const [activeTab, setActiveTab] = useState('ALL')

    const [filters, setFilters] = useState({
        keyword: '',
        hinhThucMuaHang: '',
        dateRange: [dayjs(), dayjs()],
        tatCaNgay: true, // Để true ban đầu giúp thấy ngay 6 đơn mẫu HD01->HD06, có thể gạt về Hôm nay
    })

    const [detailModalOpen, setDetailModalOpen] = useState(false)
    const [selectedOrder, setSelectedOrder] = useState(null)
    const [itemFilter, setItemFilter] = useState({ keyword: '', mauSac: '', kichThuoc: '' })

    const [editMode, setEditMode] = useState(false)
    const [editForm] = Form.useForm()
    const [editedQuantities, setEditedQuantities] = useState({})

    const fetchOrders = async () => {
        setLoading(true)
        try {
            const params = { tatCaNgay: filters.tatCaNgay }
            if (filters.keyword.trim()) params.keyword = filters.keyword.trim()
            if (filters.hinhThucMuaHang) params.hinhThucMuaHang = filters.hinhThucMuaHang

            if (activeTab === 'HOAN_TIEN') {
                params.trangThaiThanhToan = 'Đã hoàn tiền'
            } else if (activeTab !== 'ALL') {
                params.trangThai = activeTab
            }

            if (!filters.tatCaNgay && filters.dateRange) {
                if (filters.dateRange[0]) params.tuNgay = filters.dateRange[0].format('YYYY-MM-DD')
                if (filters.dateRange[1]) params.denNgay = filters.dateRange[1].format('YYYY-MM-DD')
            }

            const res = await api.get('/admin/hoa-don', { params })
            setOrders(res.data)
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể tải danh sách hóa đơn')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchOrders()
    }, [activeTab, filters.hinhThucMuaHang, filters.dateRange, filters.tatCaNgay])

    const openOrderDetail = async (id) => {
        try {
            const res = await api.get(`/admin/hoa-don/${id}`)
            const data = res.data
            setSelectedOrder(data)
            setEditMode(false)
            setItemFilter({ keyword: '', mauSac: '', kichThuoc: '' })

            const qtyMap = {}
            data.chiTietList?.forEach((it) => { qtyMap[it.id] = it.soLuong })
            setEditedQuantities(qtyMap)

            editForm.setFieldsValue({
                tenNguoiNhan: data.tenNguoiNhan,
                sdtNguoiNhan: data.sdtNguoiNhan,
                diaChiGiaoHang: data.diaChiGiaoHang,
                phiGiaoHang: data.phiGiaoHang,
            })
            setDetailModalOpen(true)
        } catch (err) {
            message.error(err.response?.data?.message || 'Lỗi tải chi tiết hóa đơn')
        }
    }

    const handleChangeStatus = async (trangThaiMoi, lyDoGhiChu = '') => {
        if (!selectedOrder) return
        try {
            const res = await api.put(`/admin/hoa-don/${selectedOrder.id}/trang-thai`, {
                trangThaiMoi,
                lyDoGhiChu,
            })
            message.success(`Đã chuyển trạng thái sang: ${trangThaiMoi}`)
            setSelectedOrder(res.data)
            fetchOrders()
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể cập nhật trạng thái')
        }
    }

    const handleSaveOrderInfo = async () => {
        try {
            const values = await editForm.validateFields()
            const items = Object.entries(editedQuantities).map(([idHoaDonChiTiet, soLuongMoi]) => ({
                idHoaDonChiTiet: Number(idHoaDonChiTiet),
                soLuongMoi: Number(soLuongMoi),
            }))

            const res = await api.put(`/admin/hoa-don/${selectedOrder.id}/thong-tin`, {
                ...values,
                items,
            })
            message.success('Đã cập nhật thông tin hóa đơn và tính lại tổng tiền!')
            setSelectedOrder(res.data)
            setEditMode(false)
            fetchOrders()
        } catch (err) {
            if (err.response?.data?.message) message.error(err.response.data.message)
        }
    }

    const handleRefund = async () => {
        try {
            const res = await api.post(`/admin/hoa-don/${selectedOrder.id}/hoan-phi`, {
                ghiChu: 'Admin xác nhận hoàn phí',
            })
            message.success('Đã xác nhận hoàn tiền cho đơn hàng!')
            setSelectedOrder(res.data)
            fetchOrders()
        } catch (err) {
            message.error(err.response?.data?.message || 'Lỗi hoàn phí')
        }
    }

    const handleRecreateOrder = async () => {
        try {
            const res = await api.post(`/admin/hoa-don/${selectedOrder.id}/mua-lai-ngay`)
            message.success(`Đã tạo đơn đền bù mới (${res.data.maHoaDon}) miễn phí vận chuyển!`)
            setSelectedOrder(res.data)
            fetchOrders()
        } catch (err) {
            message.error(err.response?.data?.message || 'Lỗi tạo đơn đền bù')
        }
    }

    const renderOrderStatusTag = (status) => {
        const colorMap = {
            'Chờ xác nhận': 'gold',
            'Đã xác nhận': 'blue',
            'Đang giao hàng': 'processing',
            'Đã hoàn thành': 'green',
            'Đã hủy': 'red',
        }
        return <Tag color={colorMap[status] || 'default'}>{status}</Tag>
    }

    const renderPaymentStatusTag = (status) => {
        const colorMap = {
            'Chưa thanh toán': 'warning',
            'Đã thanh toán': 'success',
            'Đã hoàn tiền': 'purple',
        }
        return <Tag color={colorMap[status] || 'default'}>{status}</Tag>
    }

    const filteredOrderItems = (selectedOrder?.chiTietList || []).filter((item) => {
        const matchKey = !itemFilter.keyword ||
            item.maAoChiTiet.toLowerCase().includes(itemFilter.keyword.toLowerCase()) ||
            item.tenAo.toLowerCase().includes(itemFilter.keyword.toLowerCase())
        const matchColor = !itemFilter.mauSac || item.tenMau === itemFilter.mauSac
        const matchSize = !itemFilter.kichThuoc || item.tenKichThuoc === itemFilter.kichThuoc
        return matchKey && matchColor && matchSize
    })

    const columns = [
        { title: 'Mã HĐ', dataIndex: 'maHoaDon', key: 'maHoaDon', render: (t) => <b>{t}</b> },
        { title: 'Khách hàng', dataIndex: 'tenKhachHang', key: 'tenKhachHang' },
        { title: 'Người nhận / SĐT', key: 'nguoiNhan', render: (_, r) => `${r.tenNguoiNhan} - ${r.sdtNguoiNhan}` },
        {
            title: 'Loại đơn',
            dataIndex: 'hinhThucMuaHang',
            key: 'hinhThucMuaHang',
            render: (v) => <Tag color={v === 'Online' ? 'geekblue' : 'cyan'}>{v}</Tag>,
        },
        { title: 'Tổng tiền', dataIndex: 'tongTien', key: 'tongTien', render: (v) => <b>{formatVND(v)}</b> },
        { title: 'Thanh toán', dataIndex: 'trangThaiThanhToan', key: 'trangThaiThanhToan', render: renderPaymentStatusTag },
        { title: 'Trạng thái đơn', dataIndex: 'trangThai', key: 'trangThai', render: renderOrderStatusTag },
        {
            title: 'Ngày tạo',
            dataIndex: 'ngayTao',
            key: 'ngayTao',
            render: (v) => (v ? dayjs(v).format('DD/MM/YYYY HH:mm') : ''),
        },
        {
            title: 'Thao tác',
            key: 'action',
            render: (_, r) => (
                <Button type="primary" ghost size="small" icon={<EyeOutlined />} onClick={() => openOrderDetail(r.id)}>
                    Chi tiết
                </Button>
            ),
        },
    ]

    return (
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Card title="Quản lý Hóa đơn">
                <Row gutter={[16, 16]} align="middle">
                    <Col xs={24} md={7}>
                        <Input
                            placeholder="Tìm theo mã HĐ, tên hoặc SĐT người nhận..."
                            value={filters.keyword}
                            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
                            onPressEnter={fetchOrders}
                            suffix={<SearchOutlined onClick={fetchOrders} style={{ cursor: 'pointer' }} />}
                        />
                    </Col>
                    <Col xs={12} md={4}>
                        <Select
                            style={{ width: '100%' }}
                            value={filters.hinhThucMuaHang}
                            onChange={(v) => setFilters({ ...filters, hinhThucMuaHang: v })}
                            options={[
                                { value: '', label: 'Tất cả loại hóa đơn' },
                                { value: 'Online', label: 'Trực tuyến (Online)' },
                                { value: 'Tại quầy', label: 'Tại quầy' },
                            ]}
                        />
                    </Col>
                    <Col xs={12} md={6}>
                        <RangePicker
                            style={{ width: '100%' }}
                            format="DD/MM/YYYY"
                            disabled={filters.tatCaNgay}
                            value={filters.dateRange}
                            onChange={(dates) => setFilters({ ...filters, dateRange: dates, tatCaNgay: false })}
                        />
                    </Col>
                    <Col xs={12} md={4}>
                        <Space>
                            <span>Xem tất cả ngày:</span>
                            <Switch
                                checked={filters.tatCaNgay}
                                onChange={(checked) => setFilters({ ...filters, tatCaNgay: checked })}
                            />
                        </Space>
                    </Col>
                    <Col xs={12} md={3}>
                        <Button
                            icon={<ReloadOutlined />}
                            onClick={() =>
                                setFilters({
                                    keyword: '',
                                    hinhThucMuaHang: '',
                                    dateRange: [dayjs(), dayjs()],
                                    tatCaNgay: false,
                                })
                            }
                        >
                            Hôm nay
                        </Button>
                    </Col>
                </Row>

                <Tabs
                    style={{ marginTop: 16 }}
                    activeKey={activeTab}
                    onChange={setActiveTab}
                    items={[
                        { key: 'ALL', label: 'Tất cả' },
                        { key: 'Chờ xác nhận', label: 'Chờ xác nhận' },
                        { key: 'Đã xác nhận', label: 'Đã xác nhận' },
                        { key: 'Đang giao hàng', label: 'Đang giao hàng' },
                        { key: 'Đã hoàn thành', label: 'Đã hoàn thành' },
                        { key: 'Đã hủy', label: 'Đã hủy' },
                        { key: 'HOAN_TIEN', label: 'Đã hoàn tiền' },
                    ]}
                />

                <Table
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={orders}
                    pagination={{ pageSizeOptions: ['5', '10', '20', '30'], showSizeChanger: true, defaultPageSize: 10 }}
                />
            </Card>

            <Modal
                title={selectedOrder ? `Chi tiết hóa đơn: ${selectedOrder.maHoaDon}` : 'Chi tiết hóa đơn'}
                open={detailModalOpen}
                onCancel={() => setDetailModalOpen(false)}
                width={1000}
                footer={[
                    <Button key="print" icon={<PrinterOutlined />} onClick={() => window.print()}>
                        In hóa đơn (QR)
                    </Button>,
                    <Button key="close" onClick={() => setDetailModalOpen(false)}>
                        Đóng
                    </Button>,
                ]}
            >
                {selectedOrder && (
                    <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                        {selectedOrder.trangThai !== 'Đã hủy' ? (
                            <Steps
                                current={ORDER_STEPS.indexOf(selectedOrder.trangThai)}
                                items={ORDER_STEPS.map((st) => ({ title: st }))}
                            />
                        ) : (
                            <Tag color="red" style={{ fontSize: 15, padding: '6px 12px' }}>
                                ĐƠN HÀNG ĐÃ HỦY ({selectedOrder.trangThaiThanhToan})
                            </Tag>
                        )}

                        <Card size="small" style={{ background: '#fafafa' }}>
                            <Space wrap>
                                {selectedOrder.trangThai === 'Chờ xác nhận' && (
                                    <>
                                        <Button
                                            type="primary"
                                            icon={<CheckOutlined />}
                                            onClick={() => handleChangeStatus('Đã xác nhận', 'Xác nhận đơn & trừ kho')}
                                        >
                                            Xác nhận đơn (Trừ tồn kho)
                                        </Button>
                                        <Button icon={<EditOutlined />} onClick={() => setEditMode(!editMode)}>
                                            {editMode ? 'Hủy sửa' : 'Sửa địa chỉ / Số lượng SP'}
                                        </Button>
                                    </>
                                )}

                                {selectedOrder.trangThai === 'Đã xác nhận' && (
                                    <Button
                                        type="primary"
                                        onClick={() => handleChangeStatus('Đang giao hàng', 'Bàn giao cho đơn vị vận chuyển')}
                                    >
                                        Chuyển sang Đang giao hàng
                                    </Button>
                                )}

                                {selectedOrder.trangThai === 'Đang giao hàng' && (
                                    <>
                                        <Button
                                            type="primary"
                                            style={{ background: '#52c41a' }}
                                            onClick={() => handleChangeStatus('Đã hoàn thành', 'Giao hàng thành công')}
                                        >
                                            Giao thành công (Hoàn thành)
                                        </Button>

                                        <Popconfirm title="Giao hàng thất bại & Hoàn phí cho khách?" onConfirm={handleRefund}>
                                            <Button danger icon={<RollbackOutlined />}>
                                                Giao thất bại / Xác nhận hoàn phí
                                            </Button>
                                        </Popconfirm>

                                        <Popconfirm
                                            title="Tạo lại đơn hàng mới y nguyên và miễn phí vận chuyển (Đền bù mất hàng)?"
                                            onConfirm={handleRecreateOrder}
                                        >
                                            <Button icon={<RetweetOutlined />}>Mua lại ngay (Đền bù mất hàng)</Button>
                                        </Popconfirm>
                                    </>
                                )}

                                {['Chờ xác nhận', 'Đã xác nhận'].includes(selectedOrder.trangThai) && (
                                    <Popconfirm
                                        title="Bạn có chắc chắn muốn hủy đơn hàng này?"
                                        onConfirm={() => handleChangeStatus('Đã hủy', 'Admin hủy đơn')}
                                    >
                                        <Button danger icon={<CloseCircleOutlined />}>Hủy đơn hàng</Button>
                                    </Popconfirm>
                                )}
                            </Space>
                        </Card>

                        <Row gutter={16}>
                            <Col span={18}>
                                {!editMode ? (
                                    <Descriptions bordered size="small" column={2}>
                                        <Descriptions.Item label="Khách hàng">{selectedOrder.tenKhachHang}</Descriptions.Item>
                                        <Descriptions.Item label="Email">{selectedOrder.emailKhachHang || '—'}</Descriptions.Item>
                                        <Descriptions.Item label="Người nhận">{selectedOrder.tenNguoiNhan}</Descriptions.Item>
                                        <Descriptions.Item label="Số điện thoại">{selectedOrder.sdtNguoiNhan}</Descriptions.Item>
                                        <Descriptions.Item label="Địa chỉ giao" span={2}>
                                            {selectedOrder.diaChiGiaoHang}
                                        </Descriptions.Item>
                                        <Descriptions.Item label="Hình thức mua">
                                            <Tag color="blue">{selectedOrder.hinhThucMuaHang}</Tag>
                                        </Descriptions.Item>
                                        <Descriptions.Item label="Phương thức TT">
                                            {selectedOrder.phuongThucThanhToan}
                                            {selectedOrder.phuongThucThanhToanOnline ? ` (${selectedOrder.phuongThucThanhToanOnline})` : ''}
                                        </Descriptions.Item>
                                        <Descriptions.Item label="Trạng thái TT">
                                            {renderPaymentStatusTag(selectedOrder.trangThaiThanhToan)}
                                        </Descriptions.Item>
                                        <Descriptions.Item label="Mã giao dịch">{selectedOrder.maGiaoDich || '—'}</Descriptions.Item>
                                        <Descriptions.Item label="Lịch sử / Ghi chú" span={2}>
                                            {selectedOrder.ghiChu || '—'}
                                        </Descriptions.Item>
                                    </Descriptions>
                                ) : (
                                    <Card size="small" title="Chỉnh sửa thông tin giao hàng (Khi Chờ xác nhận)">
                                        <Form form={editForm} layout="vertical">
                                            <Row gutter={12}>
                                                <Col span={8}>
                                                    <Form.Item label="Tên người nhận" name="tenNguoiNhan" rules={[{ required: true }]}>
                                                        <Input />
                                                    </Form.Item>
                                                </Col>
                                                <Col span={8}>
                                                    <Form.Item label="SĐT người nhận" name="sdtNguoiNhan" rules={[{ required: true }]}>
                                                        <Input />
                                                    </Form.Item>
                                                </Col>
                                                <Col span={8}>
                                                    <Form.Item label="Phí vận chuyển (VNĐ)" name="phiGiaoHang" rules={[{ required: true }]}>
                                                        <InputNumber style={{ width: '100%' }} min={0} step={5000} />
                                                    </Form.Item>
                                                </Col>
                                            </Row>
                                            <Form.Item label="Địa chỉ giao hàng" name="diaChiGiaoHang" rules={[{ required: true }]}>
                                                <Input />
                                            </Form.Item>
                                            <Button type="primary" onClick={handleSaveOrderInfo}>
                                                Lưu thay đổi (Tự động tính lại tổng tiền)
                                            </Button>
                                        </Form>
                                    </Card>
                                )}
                            </Col>
                            <Col span={6} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                                <QRCode value={selectedOrder.maHoaDon} size={130} />
                                <span style={{ marginTop: 6, fontSize: 12, color: '#666' }}>
                                    Quét QR tra cứu: <b>{selectedOrder.maHoaDon}</b>
                                </span>
                            </Col>
                        </Row>

                        <Divider orientation="left">Danh sách sản phẩm trong hóa đơn</Divider>

                        <Row gutter={12}>
                            <Col span={10}>
                                <Input
                                    placeholder="Lọc theo mã SKU hoặc tên sản phẩm..."
                                    value={itemFilter.keyword}
                                    onChange={(e) => setItemFilter({ ...itemFilter, keyword: e.target.value })}
                                />
                            </Col>
                            <Col span={7}>
                                <Select
                                    style={{ width: '100%' }}
                                    placeholder="Lọc màu sắc"
                                    allowClear
                                    onChange={(v) => setItemFilter({ ...itemFilter, mauSac: v || '' })}
                                    options={[...new Set(selectedOrder.chiTietList.map((i) => i.tenMau))].map((m) => ({
                                        value: m,
                                        label: m,
                                    }))}
                                />
                            </Col>
                            <Col span={7}>
                                <Select
                                    style={{ width: '100%' }}
                                    placeholder="Lọc kích cỡ"
                                    allowClear
                                    onChange={(v) => setItemFilter({ ...itemFilter, kichThuoc: v || '' })}
                                    options={[...new Set(selectedOrder.chiTietList.map((i) => i.tenKichThuoc))].map((s) => ({
                                        value: s,
                                        label: s,
                                    }))}
                                />
                            </Col>
                        </Row>

                        <Table
                            rowKey="id"
                            size="small"
                            pagination={false}
                            dataSource={filteredOrderItems}
                            columns={[
                                { title: 'Mã biến thể', dataIndex: 'maAoChiTiet' },
                                { title: 'Tên sản phẩm', dataIndex: 'tenAo' },
                                { title: 'Màu sắc', dataIndex: 'tenMau' },
                                { title: 'Size', dataIndex: 'tenKichThuoc' },
                                { title: 'Chất liệu', dataIndex: 'tenChatLieu' },
                                {
                                    title: 'Số lượng',
                                    dataIndex: 'soLuong',
                                    render: (val, r) =>
                                        editMode ? (
                                            <InputNumber
                                                min={1}
                                                max={r.tonKhoHienTai}
                                                value={editedQuantities[r.id] ?? val}
                                                onChange={(newVal) =>
                                                    setEditedQuantities({ ...editedQuantities, [r.id]: newVal || 1 })
                                                }
                                            />
                                        ) : (
                                            val
                                        ),
                                },
                                { title: 'Đơn giá', dataIndex: 'donGia', render: formatVND },
                                { title: 'Thành tiền', dataIndex: 'thanhTien', render: (v) => <b>{formatVND(v)}</b> },
                            ]}
                        />

                        <Row justify="end">
                            <Col xs={24} md={10}>
                                <Descriptions bordered size="small" column={1}>
                                    <Descriptions.Item label="Tổng tiền hàng (Trước giảm)">
                                        {formatVND(selectedOrder.tienTruocGiam)}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="Giảm giá (Voucher)">
                                        -{formatVND(selectedOrder.tienGiamGia)}
                                        {selectedOrder.maGiamGia && (
                                            <Tag color="green" style={{ marginLeft: 8 }}>{selectedOrder.maGiamGia}</Tag>
                                        )}
                                    </Descriptions.Item>
                                    <Descriptions.Item label="Phí vận chuyển">
                                        {formatVND(selectedOrder.phiGiaoHang)}
                                    </Descriptions.Item>
                                    <Descriptions.Item label={<b>Tổng thanh toán</b>}>
                                        <b style={{ color: '#cf1322', fontSize: 16 }}>{formatVND(selectedOrder.tongTien)}</b>
                                    </Descriptions.Item>
                                </Descriptions>
                            </Col>
                        </Row>
                    </Space>
                )}
            </Modal>
        </Space>
    )
}