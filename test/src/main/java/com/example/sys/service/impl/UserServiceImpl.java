package com.example.sys.service.impl;

import com.example.sys.entity.User;
import com.example.sys.mapper.UserMapper;
import com.example.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.sys.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        // 参数校验
        if (userId == null || oldPassword == null || newPassword == null) {
            return false;
        }

        if (oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return false;
        }

        if (newPassword.length() < 6) {
            return false;
        }

        // 查询用户信息
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }

        // 验证原密码
        String encryptedOldPassword = Md5Util.getMD5String(oldPassword);
        if (!encryptedOldPassword.equals(user.getUserPassword())) {
            return false;
        }

        // 检查新密码是否与原密码相同
        String encryptedNewPassword = Md5Util.getMD5String(newPassword);
        if (encryptedNewPassword.equals(user.getUserPassword())) {
            return false; // 新密码不能与原密码相同
        }

        // 更新密码
        user.setUserPassword(encryptedNewPassword);
        return this.updateById(user);
    }
}
