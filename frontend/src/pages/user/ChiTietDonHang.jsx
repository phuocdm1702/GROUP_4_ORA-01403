import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { App } from 'antd'
import dayjs from 'dayjs'
import api from '../../api/axios'
import { imgUrl, vnd } from '../../data/products'
import ProductImage from '../../components/user/ProductImage'
import { STATUS_COLOR } from './DonHangCuaToi'

const STEPS = ['Chờ xác nhận', 'Đã xác nhận', 'Đang giao hàng', 'Đã hoàn thành']

export default function ChiTietDonHang() {
    const { ma } = useParams()
    const { message, modal } = App.useApp()
    const [d, setD] = useState(null)
    const [error, setError] = useState('')
    const [tick, setTick] = useState(0)

    useEffect(() => {
        let ignore = false
        api.get(`/user/don-hang/${ma}`)
            .then(({ data }) => { if (!ignore) { setD(data); setError('') } })
            .catch(e => { if (!ignore) setError(e.response?.data?.message ?? 'Không tải được đơn hàng') })
        return () => { ignore = true }
    }, [ma, tick])

    const huy = () => modal.confirm({
        title: 'Hủy đơn hàng này?',
        content: 'Đơn hàng sẽ bị hủy, sản phẩm được trả về kho và mã giảm giá (nếu có) được hoàn lại. Thao tác này không thể hoàn tác.',
        okText: 'Hủy đơn', cancelText: 'Giữ đơn', okButtonProps: { danger: true },
        onOk: async () => {
            try {
                await api.post(`/user/don-hang/${ma}/huy`)
                message.success('Đã hủy đơn hàng')
            } catch (e) {
                message.error(e.response?.data?.message ?? 'Không hủy được đơn hàng')
            }
            setTick(t => t + 1)
        },
    })

    if (error) {
        return (
            <div>
                <p className="err">{error}</p>
                <Link to="/tai-khoan/don-hang" className="link">Quay lại danh sách đơn hàng</Link>
            </div>
        )
    }
    if (!d) return <p className="muted">Đang tải...</p>

    const stepIndex = STEPS.indexOf(d.trangThai)

    return (
        <div>
            <p><Link to="/tai-khoan/don-hang" className="link">← Đơn hàng của tôi</Link></p>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, margin: '14px 0 20px' }}>
                <div>
                    <h2 className="sec" style={{ margin: 0 }}>Đơn hàng {d.maHoaDon}</h2>
                    <span className="muted">Đặt lúc {dayjs(d.ngayTao).format('DD/MM/YYYY HH:mm')}</span>
                </div>
                {d.coTheHuy && <button className="btn ghost" onClick={huy}>Hủy đơn</button>}
            </div>

            {d.trangThai === 'Đã hủy' ? (
                <div className="cancelled">Đơn hàng đã bị hủy.</div>
            ) : (
                <div className="steps">
                    {STEPS.map((s, i) => (
                        <div key={s} className={`step ${i <= stepIndex ? 'done' : ''}`}>
                            <span className="dot">{i + 1}</span>{s}
                        </div>
                    ))}
                </div>
            )}

            <div className="cart-grid" style={{ marginTop: 24 }}>
                <div>
                    {d.dong.map((x, i) => (
                        <div className="crow" key={i}>
                            <Link to={`/san-pham/${x.idSanPham}?mau=${x.maMau}`} className="cthumb">
                                <ProductImage src={imgUrl(x.anhUrl)} color={x.hex} alt={x.tenSanPham} />
                            </Link>
                            <div>
                                <div>{x.tenSanPham}</div>
                                <small className="muted">{x.tenMau} / {x.size}</small>
                                <div className="muted" style={{ marginTop: 6 }}>{vnd(x.donGia)} × {x.soLuong}</div>
                            </div>
                            <b>{vnd(x.thanhTien)}</b>
                        </div>
                    ))}

                    <h2 className="sec">Thông tin giao hàng</h2>
                    <p><b>{d.tenNguoiNhan}</b> · {d.sdtNguoiNhan}</p>
                    <p>{d.diaChiGiaoHang}</p>
                    {d.ghiChu && <p className="muted">Ghi chú: {d.ghiChu}</p>}
                </div>

                <aside className="summary">
                    <h2>Thanh toán</h2>
                    <p style={{ margin: '0 0 12px' }}>
                        <span className={`badge ${STATUS_COLOR[d.trangThai] ?? ''}`}>{d.trangThai}</span>{' '}
                        <span className="muted">{d.trangThaiThanhToan}</span>
                    </p>
                    <div className="srow"><span>Tạm tính</span><span>{vnd(d.tienTruocGiam)}</span></div>
                    {d.tienGiam > 0 && <div className="srow"><span>Giảm giá {d.maGiamGia ? `(${d.maGiamGia})` : ''}</span><span>− {vnd(d.tienGiam)}</span></div>}
                    <div className="srow"><span>Phí vận chuyển</span><span>{d.phiGiaoHang > 0 ? vnd(d.phiGiaoHang) : 'Miễn phí'}</span></div>
                    <div className="srow total"><span>Tổng cộng</span><span>{vnd(d.tongTien)}</span></div>
                    <p className="muted">{d.phuongThucThanhToan}</p>
                </aside>
            </div>
        </div>
    )
}