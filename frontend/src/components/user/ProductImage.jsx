import { useState } from 'react'
import Shirt from './Shirt'

export default function ProductImage({ src, color, alt = '' }) {
    const [failed, setFailed] = useState(null)
    if (!src || failed === src) return <Shirt color={color} />
    return <img src={src} alt={alt} className="photo" onError={() => setFailed(src)} />
}