import api from './axios'

export const fetchProducts = () => api.get('/public/san-pham').then(r => r.data)
export const fetchProduct = id => api.get(`/public/san-pham/${id}`).then(r => r.data)
export const searchProducts = params => api.get('/public/san-pham/tim-kiem', { params }).then(r => r.data)
export const fetchFilters = () => api.get('/public/san-pham/bo-loc').then(r => r.data)