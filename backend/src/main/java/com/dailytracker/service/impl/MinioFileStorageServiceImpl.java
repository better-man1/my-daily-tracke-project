package com.dailytracker.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.config.MinioProperties;
import com.dailytracker.service.FileStorageService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;

/**
 * MinIO 文件存储服务实现类（MinIO File Storage Service Implementation）
 *
 * 【类设计说明】
 * 本类是 FileStorageService 接口的具体实现，基于 MinIO 对象存储系统提供文件上传和删除功能。
 * MinIO 是一个高性能、兼容 Amazon S3 API 的开源对象存储系统，适合私有化部署。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入依赖
 *
 * 【核心设计】
 * - 自动桶管理：上传前检查存储桶是否存在，不存在则自动创建并设置公开读策略
 * - UUID 文件命名：使用 UUID 避免文件名冲突
 * - 按日期分目录：按 yyyy/MM/dd 结构组织文件，避免单目录文件过多
 * - 容错删除：删除失败只记录日志不抛异常，避免影响主业务流程
 *
 * 【策略模式体现】
 * 本类是 FileStorageService 接口的 MinIO 策略实现。
 * 如果需要切换到阿里云 OSS 或本地文件存储，只需创建新的实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    /** MinIO 客户端（由 Spring 配置类创建并注入） */
    private final MinioClient minioClient;
    /** MinIO 配置属性（endpoint、accessKey、secretKey、bucketName 等） */
    private final MinioProperties minioProperties;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucketName()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucketName()).build());
                // 设置存储桶策略为公开读
                String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Action\":[\"s3:GetObject\"],\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Resource\":[\"arn:aws:s3:::" + minioProperties.getBucketName() + "/*\"]}]}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .config(policy)
                        .build());
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".") ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            
            // 按日期构建目录结构
            String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
            String fileName = path + "/" + datePath + "/" + IdUtil.simpleUUID() + ext;
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(fileName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }

            // 返回文件访问URL
            String endpoint = minioProperties.getEndpoint();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint + "/" + minioProperties.getBucketName() + "/" + fileName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 解析对象名
            String endpointAndBucket = minioProperties.getEndpoint();
            if (!endpointAndBucket.endsWith("/")) {
                endpointAndBucket += "/";
            }
            endpointAndBucket += minioProperties.getBucketName() + "/";

            if (fileUrl != null && fileUrl.startsWith(endpointAndBucket)) {
                String objectName = fileUrl.substring(endpointAndBucket.length());
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(objectName)
                        .build());
            }
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            // 删除失败不抛出异常，只记录日志
        }
    }
}
