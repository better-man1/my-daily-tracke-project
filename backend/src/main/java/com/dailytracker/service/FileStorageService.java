package com.dailytracker.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file 待上传的文件
     * @param path 存储路径（包含目录前缀，如：avatar/2026/04）
     * @return 文件的访问URL
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * 删除文件
     *
     * @param fileUrl 文件的访问URL
     */
    void deleteFile(String fileUrl);
}
