package com.example.sys.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.sys.common.Result;
import com.example.sys.entity.User;
import com.example.sys.service.IUserService;
import com.example.sys.util.AliOssUtil;
import com.example.sys.util.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;


@RestController
@RequestMapping("/sys/upload")
public class FileUploadController {

    @Autowired
    private IUserService userService;

    private final String UPLOAD_DIR;

    public FileUploadController() {
        String projectRoot = System.getProperty("user.dir");
        this.UPLOAD_DIR = projectRoot + File.separator + "uploads" + File.separator + "avatars" + File.separator;
        System.out.println("文件上传目录: " + UPLOAD_DIR);
        ensureUploadDirectory();
    }

    /**
     * 上传头像文件并直接更新用户头像
     * 合并了文件上传和头像更新两个操作
     */
    @PostMapping("/avatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 检查文件是否为空
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 2. 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }

            // 3. 获取当前登录用户
            User user = ThreadLocalUtil.get();
            if (user == null) {
                return Result.error("用户未登录");
            }

            // 4. 生成唯一文件名并保存文件
            String fileName = generateFileName(file.getOriginalFilename());
            File destFile = new File(UPLOAD_DIR + fileName);
            file.transferTo(destFile);

            // 5. 构建访问URL
            String avatarUrl = "/api/files/avatars/" + fileName;

            // 6. 更新用户头像URL到数据库
            user.setAvatarUrl(avatarUrl);
            LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(User::getUserId, user.getUserId());
            wrapper.set(User::getAvatarUrl, avatarUrl);

            boolean update = userService.update(wrapper);

            if (update) {
                return Result.success(avatarUrl);
            } else {
                // 如果数据库更新失败，删除已上传的文件
                if (destFile.exists()) {
                    destFile.delete();
                }
                return Result.error("头像更新失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 原来的文件上传接口（保留用于其他文件上传）
     */
    @PostMapping("/file")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("收到文件上传请求，文件名: " + file.getOriginalFilename());

            // 检查文件
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 生成文件名并保存
            String fileName = generateFileName(file.getOriginalFilename());
            File destFile = new File(UPLOAD_DIR + fileName);
            file.transferTo(destFile);

            // 返回访问URL
            String url = "/api/files/avatars/" + fileName;
            System.out.println("文件上传成功，URL: " + url);

            return Result.success(url);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 确保上传目录存在
     */
    private void ensureUploadDirectory() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("创建上传目录: " + UPLOAD_DIR + " 结果: " + (created ? "成功" : "失败"));
        } else {
            System.out.println("上传目录已存在: " + UPLOAD_DIR);
        }
    }

}
