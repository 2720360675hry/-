import request from '@/utils/request'

// 上传文件到OSS
export function uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/sys/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}


// 保留原来的文件上传接口
export function uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/sys/upload/file',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}