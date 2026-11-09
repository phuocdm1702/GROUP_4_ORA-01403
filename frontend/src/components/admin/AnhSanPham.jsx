import { useCallback, useEffect, useState } from 'react'
import { Card, Tabs, Upload, Button, Image, Space, Tag, Popconfirm, Typography, App } from 'antd'
import { PlusOutlined, DeleteOutlined, StarOutlined } from '@ant-design/icons'
import api from '../../api/axios'
import { imgUrl } from '../../data/products'

const TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_MB = 5

const dot = hex => ({
    width: 14, height: 14, borderRadius: '50%', background: hex ?? '#ccc',
    border: '1px solid #d9d9d9', display: 'inline-block',
})

export default function AnhSanPham({ idAo, colors }) {
    const { message } = App.useApp()
    const [list, setList] = useState([])

    const load = useCallback(async () => {
        try {
            const res = await api.get(`/admin/san-pham/${idAo}/anh`)
            setList(res.data)
        } catch {
            message.error('Không tải được danh sách ảnh')
        }
    }, [idAo, message])

    useEffect(() => { load() }, [load])

    const beforeUpload = file => {
        if (!TYPES.includes(file.type)) {
            message.error('Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP')
            return Upload.LIST_IGNORE
        }
        if (file.size / 1024 / 1024 > MAX_MB) {
            message.error(`Ảnh vượt quá ${MAX_MB}MB`)
            return Upload.LIST_IGNORE
        }
        return true
    }

    const upload = async ({ file, onSuccess, onError }, idMau) => {
        const fd = new FormData()
        fd.append('file', file)
        if (idMau != null) fd.append('idMau', idMau)
        try {
            await api.post(`/admin/san-pham/${idAo}/anh`, fd)
            onSuccess()
            load()
        } catch (e) {
            onError(e)
            message.error(e.response?.data?.message ?? 'Tải ảnh thất bại')
        }
    }

    const setPrimary = async id => {
        try { await api.put(`/admin/anh/${id}/chinh`); load() }
        catch { message.error('Không đổi được ảnh chính') }
    }

    const remove = async id => {
        try { await api.delete(`/admin/anh/${id}`); load() }
        catch { message.error('Không xóa được ảnh') }
    }

    const panel = idMau => {
        const imgs = list.filter(a => (a.idMau ?? null) === idMau)
        return (
            <Space wrap size={16} align="start">
                {imgs.map((a, i) => (
                    <div key={a.id} style={{ width: 140 }}>
                        <Image src={imgUrl(a.url)} width={140} height={180} style={{ objectFit: 'cover' }} />
                        <div style={{ marginTop: 6, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            {i === 0
                                ? <Tag color="green" style={{ margin: 0 }}>Ảnh chính</Tag>
                                : <Button size="small" icon={<StarOutlined />} onClick={() => setPrimary(a.id)}>Chọn chính</Button>}
                            <Popconfirm title="Xóa ảnh này?" okText="Xóa" cancelText="Hủy" onConfirm={() => remove(a.id)}>
                                <Button size="small" danger icon={<DeleteOutlined />} />
                            </Popconfirm>
                        </div>
                    </div>
                ))}
                <Upload multiple showUploadList={false} accept={TYPES.join(',')}
                        beforeUpload={beforeUpload} customRequest={o => upload(o, idMau)}>
                    <div style={{ width: 140, height: 180, border: '1px dashed #d9d9d9', borderRadius: 8,
                        display: 'grid', placeItems: 'center', cursor: 'pointer', textAlign: 'center' }}>
                        <div><PlusOutlined /><div style={{ marginTop: 6 }}>Tải ảnh lên</div></div>
                    </div>
                </Upload>
            </Space>
        )
    }

    const items = [
        { key: 'chung', label: 'Ảnh chung', children: panel(null) },
        ...colors.map(c => ({
            key: String(c.id),
            label: <Space size={6}><span style={dot(c.maHex)} />{c.tenMau}</Space>,
            children: panel(c.id),
        })),
    ]

    return (
        <Card title="Hình ảnh theo màu">
            <Typography.Paragraph type="secondary">
                Ảnh đầu tiên của mỗi màu là ảnh chính (hiện ở trang chủ), ảnh thứ hai hiện khi rê chuột.
                Màu chưa có ảnh sẽ dùng ảnh chung. JPG, PNG hoặc WEBP, tối đa {MAX_MB}MB, mỗi màu tối đa 8 ảnh.
                Nên dùng ảnh dọc tỉ lệ 3:4 (ví dụ 900×1200).
            </Typography.Paragraph>
            <Tabs items={items} />
        </Card>
    )
}