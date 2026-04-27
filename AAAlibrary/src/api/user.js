//1.导入request.js
import request from '@/utils/request'

//2.定义注册的函数
export const registerService = (data) => {
    return request({
        //请求的接口地址必须跟后端保持一致
        url: '/sys/user/register',
        method: 'post',
        data
    })
}

//2.定义登陆的函数
export const loginService = (data) => {
    return request({
        //请求的接口地址必须跟后端保持一致
        url: '/sys/user/login',
        method: 'post',
        data
    })
}

//获取用户信息
export const userInfoGetService = () => {
    return request.get('/sys/user/userinfo');
}

// 新增：获取普通用户列表（管理员）
export const getNormalUsersService = () => {
    return request.get('/sys/user/normal-users');
}
// 新增：禁用用户（管理员）
export const disableUserService = (userId) => {
    return request.post(`/sys/user/disable/${userId}`);
};
//新增： 解禁用户（管理员）
export const enableUserService = (userId) => {
    return request.post(`/sys/user/enable/${userId}`);
};

// 更新用户信息
export const updateUserInfoService = (data) => {
    return request({
        url: '/sys/user/update',
        method: 'put',  // 或者 'post'，根据后端接口确定
        data
    })
}

/**
 * 用户修改密码
 */
export function changePassword(data) {
    return request({
        url: '/sys/user/changePassword',
        method: 'post',
        data
    })
}

// 检查 uploadAvatarService 函数
export const uploadAvatarService = (file) => {
    const formData = new FormData()
    formData.append('file', file)

    console.log('=== 上传服务调试 ===')
    console.log('文件:', file.name)
    console.log('FormData 条目数:', Array.from(formData.entries()).length)

    return request({
        url: '/sys/upload/avatar',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}
