package com.dailytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储的配置属性类
 *
 * 【类的用途】
 * 将 application.yml 中以 "minio" 为前缀的配置属性映射到 Java 对象中，
 * 实现类型安全的配置读取。
 *
 * 【设计思想】
 * 使用 @ConfigurationProperties 进行属性绑定，是 Spring Boot 推荐的配置读取方式。
 * 相比 @Value("${minio.endpoint}") 逐个读取，这种方式更加简洁、类型安全、支持批量绑定。
 *
 * 【在架构中的位置】
 * 属于配置层，被 MinioConfig 注入使用，是配置与代码分离的体现。
 *
 * 【对应的 application.yml 配置示例】
 * minio:
 *   endpoint: http://localhost:9000
 *   access-key: minioadmin
 *   secret-key: minioadmin
 *   bucket-name: daily-tracker
 *
 * 【相关技术知识点】
 * @Data Lombok 注解，自动生成以下方法：
 *       - 所有字段的 getter 和 setter 方法
 *       - toString() 方法
 *       - equals() 和 hashCode() 方法
 *       - 无参构造函数
 *
 * @Component Spring 组件注解，将此类注册为 Spring Bean
 *             只有注册为 Bean，@ConfigurationProperties 才能生效
 *
 * @ConfigurationProperties(prefix = "minio")
 *       Spring Boot 注解，将配置文件中以 "minio" 为前缀的属性绑定到此对象的字段
 *       属性名通过"松散绑定"规则映射：
 *       - yml 中的 access-key 会映射到 accessKey（kebab-case -> camelCase）
 *       - yml 中的 bucket-name 会映射到 bucketName
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 服务端点地址
     * 例如：http://localhost:9000 或 http://192.168.1.100:9000
     * 包含协议（http/https）和端口号
     */
    private String endpoint;

    /**
     * 访问密钥（Access Key）
     * 用于身份验证，类似于用户名
     * 在 MinIO 初始化时设置，默认为 "minioadmin"
     */
    private String accessKey;

    /**
     * 密钥（Secret Key）
     * 用于身份验证，类似于密码
     * 在 MinIO 初始化时设置，默认为 "minioadmin"
     * 【安全提示】生产环境中应使用强密码，且不要提交到代码仓库
     */
    private String secretKey;

    /**
     * 存储桶名称（Bucket Name）
     * MinIO 中的"桶"类似于文件夹，用于组织和隔离不同类型的文件
     * 例如：daily-tracker、user-avatars、task-attachments
     */
    private String bucketName;
}
