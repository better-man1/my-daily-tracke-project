package com.dailytracker.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储客户端配置类
 *
 * 【类的用途】
 * 创建并配置 MinIO 客户端（MinioClient）实例，用于与 MinIO 服务器进行通信，
 * 实现文件的上传、下载、删除等对象存储操作。
 *
 * 【设计思想】
 * 将 MinIO 连接配置抽取到独立的属性类（MinioProperties）中，
 * 遵循"配置与代码分离"的原则，通过构造器注入获取配置属性。
 *
 * 【在架构中的位置】
 * 属于基础设施层，为文件存储服务提供 MinIO 客户端连接。
 *
 * 【相关技术知识点 - MinIO】
 * 1. MinIO 是什么？
 *    MinIO 是一个高性能的开源对象存储服务器，兼容 Amazon S3 API。
 *    适用于存储非结构化数据，如图片、视频、日志文件、备份等。
 *
 * 2. MinIO vs 本地文件存储 vs 云存储：
 *    - 本地文件存储：简单但不适合分布式部署
 *    - MinIO：可自建私有云存储，兼容 S3 API，适合内网环境
 *    - 云存储（OSS/S3）：无需运维，但需要付费且数据在外网
 *
 * 3. 核心概念：
 *    - Bucket（桶）：类似于文件夹/容器，用于组织存储对象
 *    - Object（对象）：存储在 Bucket 中的文件，包含数据、元数据和唯一标识
 *    - Endpoint：MinIO 服务器的访问地址
 *
 * @Configuration Spring 配置类注解
 * @RequiredArgsConstructor Lombok 注解，自动生成包含 final 字段的构造函数
 *                          用于注入 MinioProperties
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    /**
     * MinIO 配置属性，包含连接所需的服务地址、访问密钥等信息
     * 通过构造器注入（@RequiredArgsConstructor + final）
     */
    private final MinioProperties minioProperties;

    /**
     * 创建 MinIO 客户端 Bean
     *
     * 【方法作用】
     * 使用配置属性构建 MinioClient 实例，配置连接参数后注册为 Spring Bean，
     * 这样在 Service 层可以通过依赖注入使用 MinioClient。
     *
     * @Bean 将 MinioClient 注册为 Spring Bean，全局共享同一个客户端实例（线程安全）
     *
     * @return MinioClient 配置好的 MinIO 客户端
     *
     * 【MinioClient 说明】
     * MinioClient 是 MinIO SDK 提供的客户端类，所有与 MinIO 的交互都通过它完成。
     * 它是线程安全的，可以在多个线程中共享同一个实例。
     * 主要操作包括：
     *   - putObject()：上传文件
     *   - getObject()：下载文件
     *   - removeObject()：删除文件
     *   - presignedGetObject()：生成预签名下载链接
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())          // MinIO 服务器地址（如 http://localhost:9000）
                .credentials(
                        minioProperties.getAccessKey(),            // 访问密钥（Access Key），类似用户名
                        minioProperties.getSecretKey()             // 密钥（Secret Key），类似密码
                )
                .build();
    }
}
