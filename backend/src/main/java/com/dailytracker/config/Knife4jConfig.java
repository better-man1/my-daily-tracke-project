package com.dailytracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3.0 接口文档配置类
 *
 * 【类的用途】
 * 配置基于 OpenAPI 3.0 规范的 API 文档，结合 Knife4j 增强 UI，提供美观的接口文档界面。
 * 开发和测试时可以通过浏览器访问 http://localhost:8080/doc.html 查看和调试所有 API。
 *
 * 【设计思想】
 * 采用"文档即代码"的理念，API 文档与代码同步维护，避免文档与实现不一致的问题。
 * 配置了全局 Bearer Token 认证，方便在文档界面直接测试需要认证的接口。
 *
 * 【在架构中的位置】
 * 属于开发工具层，仅在开发和测试环境使用，生产环境通常关闭。
 *
 * 【相关技术知识点】
 * 1. Swagger / OpenAPI：
 *    - Swagger 是一个 API 文档生成工具，后来演变为 OpenAPI 规范
 *    - OpenAPI 3.0 是最新的规范版本，定义了描述 RESTful API 的标准格式
 *    - SpringDoc 是 OpenAPI 3.0 在 Spring Boot 中的实现库
 *
 * 2. Knife4j：
 *    - Knife4j 是 Swagger/OpenAPI 的增强 UI，提供更美观的中文界面
 *    - 支持离线文档导出、全局参数、搜索过滤等功能
 *    - 访问地址为 /doc.html（区别于原生 Swagger UI 的 /swagger-ui.html）
 *
 * 3. 为什么需要 API 文档？
 *    - 前后端协作：前端开发者通过文档了解接口定义
 *    - 接口测试：直接在文档界面发送请求测试接口
 *    - 自动生成：从代码注解自动生成文档，减少手动编写的工作量
 *
 * @Configuration Spring 配置类注解
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档信息
     *
     * 【方法作用】
     * 定义 API 文档的元信息（标题、描述、版本等）和全局安全认证方案（JWT Bearer Token）。
     *
     * @Bean 将 OpenAPI 实例注册为 Spring Bean，SpringDoc 会自动使用此配置生成文档
     *
     * @return OpenAPI 配置好的 OpenAPI 文档对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // ========== 文档基本信息 ==========
                .info(new Info()
                        .title("DailyTracker API 文档")                              // 文档标题
                        .description("个人每日记录系统 - RESTful API 接口文档")       // 文档描述
                        .version("v1.0.0")                                           // API 版本号
                        .contact(new Contact()
                                .name("DailyTracker Team")                           // 维护团队名称
                                .email("admin@dailytracker.com"))                    // 联系邮箱
                        .license(new License()
                                .name("MIT License")                                // 开源许可证
                                .url("https://opensource.org/licenses/MIT")))        // 许可证链接

                // ========== 全局安全认证要求 ==========
                // SecurityRequirement 定义全局的安全要求，所有接口默认需要 Bearer Token 认证
                // "Bearer" 是与下方 SecurityScheme 中定义的名称对应
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))

                // ========== 定义认证方案 ==========
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .name("Authorization")                              // HTTP 请求头名称
                                .type(SecurityScheme.Type.HTTP)                     // 认证类型：HTTP 认证
                                .scheme("bearer")                                   // 认证方案：Bearer
                                .bearerFormat("JWT")                                // Bearer Token 格式：JWT
                                .description("请输入 JWT Token，格式：Bearer {token}"))); // 提示信息

        // 【使用说明】
        // 在 Knife4j 文档界面中：
        // 1. 点击"Authorize"按钮
        // 2. 在弹出的输入框中粘贴 JWT Token（不含 "Bearer " 前缀）
        // 3. 之后发送的所有请求都会自动在 Header 中携带 Authorization: Bearer {token}
    }
}
