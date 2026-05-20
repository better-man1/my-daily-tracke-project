package com.dailytracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS（跨源资源共享）跨域配置类
 *
 * 【类的用途】
 * 配置允许前端（如浏览器中运行的 Vue/React 应用）跨域访问后端 API。
 * 在前后端分离的架构中，前端和后端通常运行在不同的域名或端口上，
 * 浏览器的同源策略（Same-Origin Policy）会阻止跨域请求，
 * CORS 配置就是告诉浏览器"允许这些跨域请求通过"。
 *
 * 【设计思想】
 * 通过实现 WebMvcConfigurer 接口来定制 Spring MVC 的 CORS 策略，
 * 这是一种全局性的配置，所有 Controller 中的接口都会应用此 CORS 规则。
 *
 * 【在架构中的位置】
 * 属于 Web 层的基础配置，在请求到达 Controller 之前生效。
 * CORS 预检请求（OPTIONS）会在此配置中被处理。
 *
 * 【相关技术知识点 - CORS 详解】
 * 1. 什么是跨域？
 *    当请求的协议、域名、端口任一不同时，就是跨域请求。
 *    例如：前端在 http://localhost:5173，后端在 http://localhost:8080
 *
 * 2. CORS 工作流程：
 *    - 简单请求：浏览器直接发送请求，在响应头中检查 Access-Control-Allow-Origin
 *    - 预检请求：对于复杂请求（如带自定义 Header 的请求），浏览器先发送 OPTIONS 请求，
 *      询问服务器是否允许跨域，服务器返回允许的方法和头信息后，浏览器才发送真正的请求
 *
 * 3. 与 Spring Security CORS 的关系：
 *    本配置基于 Spring MVC，如果同时使用了 Spring Security，
 *    需要确保 Security 配置中不会阻止 CORS 的预检请求（OPTIONS）。
 *    本项目中 SecurityConfig 的白名单没有包含 OPTIONS，但因为 Spring MVC 的
 *    CorsFilter 会在 Security Filter 之前处理 CORS，所以预检请求能正常通过。
 *
 * @Configuration Spring 配置类注解，Spring 容器会自动加载和处理此配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 重写 addCorsMappings 方法，配置全局 CORS 策略
     *
     * 【方法作用】
     * 向 Spring MVC 的 CORS 注册表中添加跨域映射规则。
     * 此方法在 Spring 容器初始化时被自动调用。
     *
     * 【参数说明】
     * @param registry CORS 注册表，用于注册跨域配置
     *
     * 【配置详解】
     * - addMapping("/**")：匹配所有 API 路径，对所有接口生效
     * - allowedOriginPatterns("*")：允许任何来源（域名）的请求
     *   【注意】allowedOrigins("*") 在 Spring Boot 2.4+ 中与 allowCredentials(true) 不兼容，
     *   所以使用 allowedOriginPatterns("*") 替代
     * - allowedMethods：允许的 HTTP 方法
     *   GET（查询）、POST（新增）、PUT（修改）、DELETE（删除）、OPTIONS（预检请求）
     * - allowedHeaders("*")：允许所有请求头，包括 Authorization（JWT Token）
     * - allowCredentials(true)：允许携带认证信息（Cookie、Authorization Header）
     *   当设为 true 时，前端可以在跨域请求中携带凭据
     * - maxAge(3600)：预检请求的缓存时间（单位：秒）
     *   浏览器在 3600 秒（1小时）内对相同的跨域请求不会再次发送 OPTIONS 预检请求
     *   这减少了预检请求的次数，提高了性能
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                              // 对所有路径生效
                .allowedOriginPatterns("*")                     // 允许任何来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")                            // 允许所有请求头
                .allowCredentials(true)                         // 允许携带凭据
                .maxAge(3600);                                  // 预检请求缓存1小时
    }
}
