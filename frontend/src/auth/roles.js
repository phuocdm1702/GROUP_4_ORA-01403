const PRODUCT = ['ADMIN', 'MANAGER', 'WAREHOUSE']
const BOSS = ['ADMIN', 'MANAGER']

export const ACCESS = {
    '/admin/thong-ke': BOSS,
    '/admin/hoa-don': ['ADMIN', 'MANAGER', 'SALES'],
    '/admin/san-pham': PRODUCT,
    '/admin/mau-sac': PRODUCT,
    '/admin/kich-thuoc': PRODUCT,
    '/admin/chat-lieu': PRODUCT,
    '/admin/thuong-hieu': PRODUCT,
    '/admin/phieu-giam-gia': BOSS,
    '/admin/dot-giam-gia': BOSS,
    '/admin/khach-hang': ['ADMIN', 'MANAGER', 'SUPPORT'],
    '/admin/nhan-vien': ['ADMIN'],
}

export const canAccess = (user, path) => (ACCESS[path] ?? []).some(r => user.roles?.includes(r))

export const firstAllowed = user => Object.keys(ACCESS).find(p => canAccess(user, p)) ?? null