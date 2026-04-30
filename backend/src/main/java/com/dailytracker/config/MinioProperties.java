package com.dailytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * MinIO服务端点
     */
    private String endpoint;

    /**
     * 访问凭证
     */
    private String accessKey;

    /**
     * 密钥凭证
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;
}
