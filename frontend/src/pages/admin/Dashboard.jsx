import { useEffect, useState } from 'react'
import {
    Card, Col, Row, Statistic, DatePicker, Button, Select,
    Input, Segmented, Table, Tag, Space, Badge, Alert, message
} from 'antd'
import {
    ReloadOutlined, SearchOutlined, WarningOutlined, ClockCircleOutlined
} from '@ant-design/icons'
import dayjs from 'dayjs'
import {
    LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid, ResponsiveContainer
} from 'recharts'
import api from '../../api/axios'

const formatVND = (val) =>
    val != null ? Number(val).toLocaleString('vi-VN') + ' ₫' : '0 ₫'

export default function Dashboard() {
    const [loading, setLoading] = useState(false)

    // Bộ lọc theo đúng voice video [17:56 - 18:40]
    const [boLocThoiGian, setBoLocThoiGian] = useState('ALL')
    const [tuNgay, setTuNgay] = useState(dayjs().subtract(30, 'day'))
    const [denNgay, setDenNgay] = useState(dayjs())
    const [thongKeTheo, setThongKeTheo] = useState('NGAY')
    const [tuKhoaSanPham, setTuKhoaSanPham] = useState('')
    const [idChatLieu, setIdChatLieu] = useState(null)
    const [loaiSapXepSanPham, setLoaiSapXepSanPham] = useState('BAN_CHAY')

    const [dashboard, setDashboard] = useState({
        kpi: {
            tongDoanhThu: 0,
            loiNhuanBanHang: 0,
            tongSoDonHang: 0,
            soDonHoanThanh: 0,
            soDonHuyHoan: 0,
            tienDonHuyHoan: 0,
            giaTriTrungBinhDon: 0,
            tongSanPhamDaBan: 0,
            doanhThuHomNay: 0,
            soDonChoXuLy: 0,
        },
        bieuDo: [],
        sanPhamList: [],
        trangThaiDonHang: {
            'Chờ xác nhận': 0,
            'Đã xác nhận': 0,
            'Đang giao hàng': 0,
            'Đã hoàn thành': 0,
            'Đã hủy': 0,
        },
        danhSachChatLieu: [],
    })

    const fetchThongKe = async () => {
        setLoading(true)
        try {
            const params = {
                boLocThoiGian,
                thongKeTheo,
                loaiSapXepSanPham,
            }
            if (boLocThoiGian === 'TUY_CHON') {
                if (tuNgay) params.tuNgay = tuNgay.format('YYYY-MM-DD')
                if (denNgay) params.denNgay = denNgay.format('YYYY-MM-DD')
            }
            if (tuKhoaSanPham.trim()) {
                params.tuKhoaSanPham = tuKhoaSanPham.trim()
            }
            if (idChatLieu) {
                params.idChatLieu = idChatLieu
            }

            // Gọi qua axios instance (baseURL: http://localhost:8080/api + tự gắn Bearer token)
            const res = await api.get('/admin/thong-ke', { params })
            setDashboard(res.data)
        } catch (err) {
            message.error(err.response?.data?.message || 'Không thể tải dữ liệu thống kê (Hãy kiểm tra Backend đã chạy lại chưa)')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchThongKe()
    }, [boLocThoiGian, thongKeTheo, idChatLieu, loaiSapXepSanPham])

    const { kpi, bieuDo, sanPhamList, trangThaiDonHang, danhSachChatLieu } = dashboard

    const stats = [
        { title: 'Tổng doanh thu', value: kpi?.tongDoanhThu ?? 0, suffix: '₫', color: '#1f2a44' },
        { title: 'Lợi nhuận bán hàng', value: kpi?.loiNhuanBanHang ?? 0, suffix: '₫', color: '#1677ff' },
        {
            title: 'Tổng đơn hàng (Hoàn thành)',
            value: kpi?.tongSoDonHang ?? 0,
            suffix: `(${kpi?.soDonHoanThanh ?? 0} đơn HT)`,
        },
        {
            title: 'Đơn hoàn / Đã hủy',
            value: kpi?.soDonHuyHoan ?? 0,
            suffix: `(${formatVND(kpi?.tienDonHuyHoan ?? 0)})`,
            color: '#cf1322',
        },
        { title: 'Giá trị trung bình đơn', value: kpi?.giaTriTrungBinhDon ?? 0, suffix: '₫' },
        { title: 'Sản phẩm đã bán', value: kpi?.tongSanPhamDaBan ?? 0, suffix: 'áo', color: '#3f8600' },
    ]

    const revenueChartData = (bieuDo || []).map((item) => ({
        mocThoiGian: item.mocThoiGian,
        doanhThu: Number(item.doanhThu || 0),
        soDonHoanThanh: item.soDonHoanThanh,
        soSanPhamBan: item.soSanPhamBan,
    }))

    const orderStatusList = Object.entries(
        trangThaiDonHang || {
            'Chờ xác nhận': 0,
            'Đã xác nhận': 0,
            'Đang giao hàng': 0,
            'Đã hoàn thành': 0,
            'Đã hủy': 0,
        }
    )

    const productColumns = [
        { title: 'STT', key: 'stt', width: 65, render: (_, __, index) => <b>{index + 1}</b> },
        { title: 'Mã sản phẩm', dataIndex: 'maAo', key: 'maAo', render: (text) => <Tag color="geekblue">{text}</Tag> },
        { title: 'Tên sản phẩm', dataIndex: 'tenAo', key: 'tenAo', render: (text) => <b>{text}</b> },
        { title: 'Chất liệu', dataIndex: 'tenChatLieu', key: 'tenChatLieu' },
        { title: 'Đã bán', dataIndex: 'soLuongDaBan', key: 'soLuongDaBan', render: (val) => <Tag color="green">{val} sản phẩm</Tag> },
        { title: 'Doanh thu', dataIndex: 'doanhThu', key: 'doanhThu', render: (val) => <b style={{ color: '#1f2a44' }}>{formatVND(val)}</b> },
        {
            title: 'Tồn kho',
            dataIndex: 'tongTonKho',
            key: 'tongTonKho',
            render: (val) =>
                val <= 10 ? (
                    <Tag color="red" icon={<WarningOutlined />}>Thấp ({val})</Tag>
                ) : (
                    <Tag color="default">{val} trong kho</Tag>
                ),
        },
    ]

    const handleResetFilters = () => {
        setBoLocThoiGian('ALL')
        setTuNgay(dayjs().subtract(30, 'day'))
        setDenNgay(dayjs())
        setThongKeTheo('NGAY')
        setTuKhoaSanPham('')
        setIdChatLieu(null)
        setLoaiSapXepSanPham('BAN_CHAY')
    }

    return (
        <>
            <Alert
                style={{ marginBottom: 16 }}
                type="info"
                showIcon
                icon={<ClockCircleOutlined />}
                message={
                    <Space size="large" wrap>
                        <span>Doanh thu hôm nay: <b style={{ color: '#1f2a44' }}>{formatVND(kpi?.doanhThuHomNay)}</b></span>
                        <span>Hóa đơn đang chờ xử lý: <Badge count={kpi?.soDonChoXuLy ?? 0} showZero color="#faad14" /></span>
                    </Space>
                }
            />

            <Card style={{ marginBottom: 16 }}>
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    <div style={{ display: 'flex', gap: 16, alignItems: 'end', flexWrap: 'wrap' }}>
                        <div>
                            <div style={{ marginBottom: 4, fontWeight: 500 }}>Khoảng thời gian</div>
                            <Segmented
                                value={boLocThoiGian}
                                onChange={setBoLocThoiGian}
                                options={[
                                    { label: 'Tất cả', value: 'ALL' },
                                    { label: 'Hôm nay', value: 'HOM_NAY' },
                                    { label: 'Tuần này', value: 'TUAN_NAY' },
                                    { label: 'Tháng này', value: 'THANG_NAY' },
                                    { label: 'Tháng trước', value: 'THANG_TRUOC' },
                                    { label: 'Năm nay', value: 'NAM_NAY' },
                                    { label: 'Tùy chọn', value: 'TUY_CHON' },
                                ]}
                            />
                        </div>

                        <div>
                            <div style={{ marginBottom: 4 }}>Từ ngày</div>
                            <DatePicker
                                value={tuNgay}
                                onChange={(d) => { setTuNgay(d); setBoLocThoiGian('TUY_CHON') }}
                                format="DD/MM/YYYY"
                            />
                        </div>

                        <div>
                            <div style={{ marginBottom: 4 }}>Đến ngày</div>
                            <DatePicker
                                value={denNgay}
                                onChange={(d) => { setDenNgay(d); setBoLocThoiGian('TUY_CHON') }}
                                format="DD/MM/YYYY"
                            />
                        </div>

                        <div>
                            <div style={{ marginBottom: 4 }}>Thống kê theo</div>
                            <Select
                                style={{ width: 140 }}
                                value={thongKeTheo}
                                onChange={setThongKeTheo}
                                options={[
                                    { value: 'NGAY', label: 'Theo Ngày' },
                                    { value: 'THANG', label: 'Theo Tháng' },
                                    { value: 'NAM', label: 'Theo Năm' },
                                ]}
                            />
                        </div>

                        <Space style={{ marginLeft: 'auto' }}>
                            <Button onClick={handleResetFilters}>Mặc định</Button>
                            <Button type="primary" icon={<ReloadOutlined />} loading={loading} onClick={fetchThongKe}>
                                Cập nhật dữ liệu
                            </Button>
                        </Space>
                    </div>

                    <Row gutter={[16, 16]}>
                        <Col xs={24} md={9}>
                            <Input
                                placeholder="Tìm theo mã sản phẩm hoặc tên sản phẩm..."
                                value={tuKhoaSanPham}
                                onChange={(e) => setTuKhoaSanPham(e.target.value)}
                                onPressEnter={fetchThongKe}
                                suffix={<SearchOutlined onClick={fetchThongKe} style={{ cursor: 'pointer' }} />}
                            />
                        </Col>
                        <Col xs={12} md={7}>
                            <Select
                                style={{ width: '100%' }}
                                placeholder="Lọc theo chất liệu"
                                allowClear
                                value={idChatLieu}
                                onChange={(val) => setIdChatLieu(val ?? null)}
                                options={(danhSachChatLieu || []).map((c) => ({
                                    value: c.id,
                                    label: `${c.tenChatLieu} (${c.maChatLieu})`,
                                }))}
                            />
                        </Col>
                        <Col xs={12} md={8}>
                            <Select
                                style={{ width: '100%' }}
                                value={loaiSapXepSanPham}
                                onChange={setLoaiSapXepSanPham}
                                options={[
                                    { value: 'BAN_CHAY', label: 'Sản phẩm bán chạy nhất' },
                                    { value: 'DOANH_THU_CAO', label: 'Doanh thu cao nhất' },
                                    { value: 'TON_KHO_THAP', label: 'Tồn kho thấp nhất' },
                                    { value: 'TON_KHO_CAO', label: 'Tồn kho cao nhất' },
                                ]}
                            />
                        </Col>
                    </Row>
                </Space>
            </Card>

            <Row gutter={[16, 16]}>
                {stats.map((s) => (
                    <Col key={s.title} xs={24} sm={12} xl={8}>
                        <Card loading={loading}>
                            <Statistic
                                title={s.title}
                                value={s.value}
                                suffix={s.suffix}
                                groupSeparator="."
                                valueStyle={s.color ? { color: s.color, fontWeight: 600 } : { fontWeight: 600 }}
                            />
                        </Card>
                    </Col>
                ))}
            </Row>

            <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                <Col xs={24} xl={16}>
                    <Card title="Biểu đồ xu hướng doanh thu">
                        <ResponsiveContainer width="100%" height={300}>
                            <LineChart data={revenueChartData}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="mocThoiGian" />
                                <YAxis
                                    tickFormatter={(v) =>
                                        v >= 1_000_000 ? `${(v / 1_000_000).toFixed(1)} tr` : `${(v / 1000).toFixed(0)}k`
                                    }
                                />
                                <Tooltip formatter={(v) => [Number(v).toLocaleString('vi-VN') + ' ₫', 'Doanh thu']} />
                                <Line type="monotone" dataKey="doanhThu" stroke="#1f2a44" strokeWidth={2} activeDot={{ r: 6 }} />
                            </LineChart>
                        </ResponsiveContainer>
                    </Card>
                </Col>
                <Col xs={24} xl={8}>
                    <Card title="Trạng thái đơn hàng">
                        {orderStatusList.map(([label, n]) => (
                            <div
                                key={label}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    padding: '10px 0',
                                    borderBottom: '1px solid #f5f5f5',
                                }}
                            >
                                <span>{label}</span>
                                <b>{n}</b>
                            </div>
                        ))}
                    </Card>
                </Col>
            </Row>

            <Card title="Thống kê chi tiết sản phẩm & tồn kho" style={{ marginTop: 16 }}>
                <Table
                    rowKey="idSanPham"
                    loading={loading}
                    columns={productColumns}
                    dataSource={sanPhamList}
                    pagination={{ defaultPageSize: 5, pageSizeOptions: ['5', '10', '20', '30'], showSizeChanger: true }}
                />
            </Card>
        </>
    )
}