export default function Shirt({ color }) {
    return (
        <svg viewBox="0 0 200 240" className="shirt">
            <path d="M70 20L30 45 5 95l35 15 10-25v135h100V85l10 25 35-15-25-50-40-25-15 25-15 5-15-5z"
                  fill={color} stroke="rgba(0,0,0,.18)" />
            <path d="M85 45l15 30 15-30M100 75v30" fill="none" stroke="rgba(0,0,0,.3)" />
        </svg>
    )
}