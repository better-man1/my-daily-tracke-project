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
 * MinIO 文件存储服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
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
