package com.example.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户邮箱（用户账号）
     */
    private String userEmail;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 用户状态（0:正常,1:禁用）
     */
    private String userStatus;

    private String avatarUrl;

    // 新增：接收 avatarUrl 的构造方法
    public User(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // 1. 添加无参构造（必须）
    public User() {
    }
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getAvatarUrl() {
        return avatarUrl; // 原代码错误返回了userStatus，此处修正
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl; // 原代码错误赋值给了userStatus，此处修正
    }


    @Override
    public String toString() {
        return "User{" +
            "userId=" + userId +
            ", userName=" + userName +
            ", userPassword=" + userPassword +
            ", userEmail=" + userEmail +
            ", userRole=" + userRole +
            ", userStatus=" + userStatus +
            ", avatarUrl='" + avatarUrl +
        "}";
    }
}
