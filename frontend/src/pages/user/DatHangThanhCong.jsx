import { Link, useLocation, useParams } from 'react-router-dom'
import { vnd } from '../../data/products'

export default function DatHangThanhCong() {
    const { ma } = useParams()
    const { state } = useLocation()
    return (
        <div className="done">
            <h1 className="page-title">Đặt hàng thành công</h1>
            <p>Cảm ơn bạn đã mua hàng tại POLO HOUSE.</p>
            <p>Mã đơn hàng của bạn: <b>{ma}</b></p>
            {state?.tongTien != null && <p>Tổng thanh toán: <b>{vnd(state.tongTien)}</b> (thanh toán khi nhận hàng)</p>}
            <p className="muted">Chúng tôi sẽ xác nhận đơn và liên hệ với bạn sớm nhất.</p>
            <p style={{ marginTop: 24, display: 'flex', gap: 10, justifyContent: 'center' }}>
                <Link to={`/tai-khoan/don-hang/${ma}`} className="btn">Xem đơn hàng</Link>
                <Link to="/san-pham" className="btn ghost">Tiếp tục mua sắm</Link>
            </p>
        </div>
    )
}