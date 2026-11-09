export function ProductSkeletons({ n = 8 }) {
    return (
        <div className="grid" aria-busy="true" aria-label="Đang tải sản phẩm">
            {Array.from({ length: n }, (_, i) => (
                <div className="card" key={i}>
                    <div className="img sk" />
                    <div className="sk line" style={{ width: '35%' }} />
                    <div className="sk line" style={{ width: '80%' }} />
                    <div className="sk line" style={{ width: '30%' }} />
                </div>
            ))}
        </div>
    )
}