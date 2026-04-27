package com.example.sys.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
//@Component
public class AliOssUtil {

    // 从配置文件注入OSS连接信息（建议在application.properties中配置）
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到阿里云OSS
     * @param file 上传的文件
     * @param folder 存储文件夹
     * @return 文件访问URL
     * @throws Exception
     */
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        try {
            // 1. 获取原始文件名
            String originalFilename = file.getOriginalFilename();

            // 2. 生成唯一文件名，防止覆盖
            String fileName = UUID.randomUUID().toString() +
                    getFileExtension(originalFilename);

            // 3. 构建OSS对象key（完整路径）
            String objectName = folder + fileName;

            // 4. 获取文件输入流
            InputStream inputStream = file.getInputStream();

            // 5. 创建OSSClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            try {
                // 6. 创建PutObjectRequest对象
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);

                // 7. 上传文件
                ossClient.putObject(putObjectRequest);

                // 8. 构建文件访问URL
                String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
                return url;

            } finally {
                // 9. 关闭OSSClient
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("文件上传到OSS失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // 默认扩展名
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 上传字节数组到OSS
     * @param data 字节数组
     * @param folder 文件夹路径
     * @param fileName 文件名（包含后缀）
     * @return 文件URL
     */
    public String uploadBytes(byte[] data, String folder, String fileName) {
        String key = folder + fileName;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ossClient.putObject(bucketName, key, new ByteArrayInputStream(data));
            return "https://" + bucketName + "." + endpoint + "/" + key;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从OSS下载文件到本地
     * @param objectName OSS中的文件路径（如："images/xxx.jpg"）
     * @param localFilePath 本地保存路径（如："D:/download/xxx.jpg"）
     */
    public void downloadFile(String objectName, String localFilePath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 下载文件到本地
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(localFilePath));
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除OSS中的文件
     * @param objectName OSS中的文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            if (ossClient.doesObjectExist(bucketName, objectName)) {
                ossClient.deleteObject(bucketName, objectName);
                return true;
            }
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 获取文件的临时访问URL（带过期时间）
     * @param objectName OSS中的文件路径
     * @param expireSeconds 过期时间（秒）
     * @return 临时访问URL
     */
    public String getTempUrl(String objectName, int expireSeconds) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 设置URL过期时间
            Date expiration = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
            // 生成URL
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 检查文件是否存在
     * @param objectName OSS中的文件路径
     * @return 是否存在
     */
    public boolean doesFileExist(String objectName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
