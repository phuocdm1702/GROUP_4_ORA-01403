import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider, App as AntApp } from 'antd'
import App from './App.jsx'
import { CartProvider } from './context/CartContext'
import { AuthProvider } from './context/AuthContext'
import './index.css'

createRoot(document.getElementById('root')).render(
    <BrowserRouter>
        <ConfigProvider theme={{ token: { colorPrimary: '#1f2a44', borderRadius: 8 } }}>
            <AntApp>
                <AuthProvider>
                    <CartProvider>
                        <App />
                    </CartProvider>
                </AuthProvider>
            </AntApp>
        </ConfigProvider>
    </BrowserRouter>
)