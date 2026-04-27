package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.sys.common.Result;
import com.example.sys.entity.User;
import com.example.sys.service.IUserService;
import com.example.sys.util.JwtUtil;
import com.example.sys.util.Md5Util;
import com.example.sys.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xumj
 * @since 2025-09-08
 */
@RestController
@RequestMapping("/sys/user")
public class UserController {
//    1.创建一个接口
    @Autowired
    private IUserService userService;

//    定义redis缓存对象
    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping("/all")
    public List<User> allUser(){
        List<User> users = userService.list();
        return  users;
    }

//    查询用户的方法
    @GetMapping("/list")
    public Result<List<User>> list(){
        // 带权限的查询 获取ThreadLocal
        User user = (User) ThreadLocalUtil.get();
        // 如果是管理员 允许查看
        if("ADMIN".equals(user.getUserRole())){
            return Result.success(userService.list());
        }else{
            return Result.error("无权限");
        }
    }

    //3.写一个用户注册的方法
    @PostMapping("/register")
    public Result<String> register(@RequestBody User registerUser){
        //        查询用户名是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name",registerUser.getUserName());
        User userone = userService.getOne(queryWrapper);
        if (userone!=null){
            return Result.error("用户名已经存在，请重新输入");
        }
//        查询邮箱是否重复
        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("user_email",registerUser.getUserEmail());
        User userTwo = userService.getOne(queryWrapper1);
        if (userTwo!=null){
            return Result.error("邮箱已经存在");
        }

//        System.out.println("收到注册请求");
//        新建用户
        User user=new User();
//        设置用户信息
        user.setUserName(registerUser.getUserName());
//        使用MD5加密
        String newPwd=Md5Util.getMD5String(registerUser.getUserPassword());
        user.setUserPassword(newPwd);

        user.setUserEmail(registerUser.getUserEmail());
        user.setUserRole("user");
        user.setUserStatus("0");

//        新增用户
        boolean result=userService.save(user);
        if(result){
            return Result.success("注册成功了");
        }else{
            return Result.error("注册失败");
        }
    }

    /**
     * 用户登录方法
     * 已禁用用户无法登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody User loginUser) {
        System.out.println("收到登录请求...");
        String encryptedPwd = Md5Util.getMD5String(loginUser.getUserPassword());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_email", loginUser.getUserEmail())
                .eq("user_password", encryptedPwd);

        User user = userService.getOne(queryWrapper);

        if (user != null) {
            // 检查用户是否被禁用
            if ("1".equals(user.getUserStatus())) {
                return Result.error("用户已被禁止使用，请联系管理员");
            }

            // 生成token
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("userName", user.getUserName());
            claims.put("userRole", user.getUserRole()); // 加入角色信息
            String token = JwtUtil.genToken(claims);

            // 清除密码信息
            user.setUserPassword(null);

            // 保存到redis，设置过期时间
            redisTemplate.opsForValue().set(token, user, 30, TimeUnit.MINUTES);

            return Result.success(token);
        }

        return Result.error("用户名或密码错误！");
    }

    //查询个人的信息
    @RequestMapping("/userinfo")
    public Result<User> userInfo(){
//        ThreadLocalUtil 读取用户信息
        User user = (User) ThreadLocalUtil.get();
        return Result.success(user);
    }

    /**
     * 查询所有普通用户信息（不显示密码）
     * 仅管理员可访问
     */
    @GetMapping("/normal-users")
    public Result<List<User>> getAllNormalUsers() {
        // 权限校验：仅管理员可访问
        User currentUser = (User) ThreadLocalUtil.get();
        if (!"ADMIN".equals(currentUser.getUserRole())) {
            return Result.error("无权限访问，需要管理员权限");
        }

        // 查询所有普通用户（角色为user）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_role", "user");
        List<User> users = userService.list(queryWrapper);

        // 清除密码信息，确保不返回密码
        users.forEach(user -> user.setUserPassword(null));

        return Result.success(users);
    }

    /**
     * 修改用户状态为不可用（设置为1）
     * 仅管理员可操作
     */
    @PostMapping("/disable/{userId}")
    public Result<String> disableUser(@PathVariable Integer userId) {
        // 权限校验（已在拦截器或方法内实现）
        User currentUser = (User) ThreadLocalUtil.get();
        if (!"ADMIN".equals(currentUser.getUserRole())) {
            return Result.error("无权限操作，需要管理员权限");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("1".equals(user.getUserStatus())) {
            return Result.error("该用户已处于禁用状态");
        }

        user.setUserStatus("1");
        boolean success = userService.updateById(user);
        return success ? Result.success("用户已成功禁用") : Result.error("禁用用户失败，请重试");
    }
    /**
     * 修改用户状态为可用（设置为0）
     * 仅管理员可操作
     */
    @PostMapping("/enable/{userId}")
    public Result<String> enableUser(@PathVariable Integer userId) {
        // 权限校验
        User currentUser = (User) ThreadLocalUtil.get();
        if (!"ADMIN".equals(currentUser.getUserRole())) {
            return Result.error("无权限操作，需要管理员权限");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if ("0".equals(user.getUserStatus())) {
            return Result.error("该用户已处于正常状态");
        }

        user.setUserStatus("0"); // 恢复为正常状态
        boolean success = userService.updateById(user);
        return success ? Result.success("用户已成功解禁") : Result.error("解禁用户失败，请重试");
    }

    //    上传头像功能
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam String avatarUrl){
        System.out.println("收到头像上传请求"+avatarUrl);
        User user = ThreadLocalUtil.get();
        user.setAvatarUrl(avatarUrl);

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getUserId,user.getUserId());
        wrapper.set(User::getAvatarUrl,avatarUrl);
    //    修改数据库中的头像地址
        boolean update = userService.update(wrapper);
        return Result.success(update);
    }

    //更新用户信息
    /**
     * 更新用户基本信息
     */
    @PutMapping("/update")
    public Result<?> updateUserInfo(@RequestBody User user) {
        try {
            System.out.println("接收到更新请求: " + user.toString());

            // 验证必要参数
            if (user.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }

            // 检查用户是否存在
            User existingUser = userService.getById(user.getUserId());
            if (existingUser == null) {
                return Result.error("用户不存在");
            }

            // 创建更新对象，只更新允许修改的字段
            User updateUser = new User();
            updateUser.setUserId(user.getUserId());

            // 更新用户名（如果提供了）
            if (user.getUserName() != null && !user.getUserName().trim().isEmpty()) {
                updateUser.setUserName(user.getUserName().trim());
            }

            // 更新邮箱（如果提供了）
            if (user.getUserEmail() != null && !user.getUserEmail().trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getUserEmail, user.getUserEmail().trim())
                        .ne(User::getUserId, user.getUserId());
                User emailUser = userService.getOne(queryWrapper);
                if (emailUser != null) {
                    return Result.error("邮箱已被其他用户使用");
                }
                updateUser.setUserEmail(user.getUserEmail().trim());
            }

            // 执行更新
            boolean isSuccess = userService.updateById(updateUser);

            if (isSuccess) {
                // 清除可能的用户缓存
                String userKey = "user:" + user.getUserId();
                redisTemplate.delete(userKey);

                System.out.println("用户信息更新成功: " + updateUser.toString());
                return Result.success("用户信息更新成功");
            } else {
                return Result.error("用户信息更新失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable Integer userId) {
        try {
            User user = userService.getById(userId);
            if (user != null) {
                // 隐藏密码信息
                user.setUserPassword(null);
                return Result.success(user);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 用户修改密码
     */
    /**
     * 用户修改密码（需要验证原密码）
     */
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody Map<String, Object> request) {
        try {
            // 从ThreadLocal获取当前登录用户信息
            User currentUser = (User) ThreadLocalUtil.get();
            if (currentUser == null) {
                return Result.error("用户未登录");
            }

            Integer userId = currentUser.getUserId();
            String oldPassword = (String) request.get("oldPassword");
            String newPassword = (String) request.get("newPassword");

            // 参数校验
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return Result.error("原密码不能为空");
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error("新密码不能为空");
            }

            if (newPassword.length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }

            // 检查新密码是否与原密码相同
            if (oldPassword.equals(newPassword)) {
                return Result.error("新密码不能与原密码相同");
            }

            // 调用service修改密码
            boolean isSuccess = userService.changePassword(userId, oldPassword, newPassword);

            if (isSuccess) {
                // 清除用户缓存
                String userKey = "user:" + userId;
                redisTemplate.delete(userKey);

                // 注意：这里不清除token，用户还可以继续使用当前会话
                // 如果需要强制重新登录，可以清除token缓存

                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败，原密码错误或用户不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("系统错误: " + e.getMessage());
        }
    }
}

