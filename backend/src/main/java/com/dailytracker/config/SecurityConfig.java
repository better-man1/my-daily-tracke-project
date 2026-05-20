package com.dailytracker.config;

import com.dailytracker.common.result.Result;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 安全配置类
 *
 * 【类的用途】
 * 这是整个应用的安全核心配置类，负责定义：
 * 1. HTTP 请求的认证和授权规则（哪些 URL 需要认证，哪些可以公开访问）
 * 2. JWT 过滤器的注册（在 Spring Security 过滤器链中插入自定义的 JWT 认证过滤器）
 * 3. 密码加密器的配置（使用 BCrypt 算法加密密码）
 * 4. 认证失败和权限不足时的自定义 JSON 响应
 * 5. Session 管理策略（使用无状态的 JWT 方式，不使用 Session）
 *
 * 【设计思想】
 * 采用前后端分离架构下的 JWT（JSON Web Token）认证方案：
 * - 用户登录成功后，服务端生成 JWT Token 返回给前端
 * - 前端在后续请求的 Header 中携带 Token
 * - 服务端通过 JwtAuthenticationFilter 解析并验证 Token
 * - 不使用传统的 Session/Cookie 机制，实现无状态认证
 *
 * 【在架构中的位置】
 * 属于安全层（Security Layer），是 Spring Security 框架的核心配置。
 * 它在整个请求处理流程中充当"门卫"角色，所有 HTTP 请求都必须经过安全过滤链的处理。
 *
 * 【相关技术知识点 - Spring Security 过滤器链】
 * Spring Security 的核心是一系列 Servlet Filter 组成的过滤器链：
 *   UsernamePasswordAuthenticationFilter -> JWT Filter -> ... -> FilterSecurityInterceptor
 * 每个 Filter 负责不同的安全检查，请求必须通过所有 Filter 才能到达 Controller。
 *
 * @Configuration Spring 配置类注解，表示此类包含 Spring Bean 的定义
 * @EnableWebSecurity 启用 Spring Security 的 Web 安全功能，这是一个关键的开关注解
 * @RequiredArgsConstructor Lombok 注解，自动生成包含所有 final 字段的构造函数
 *                          用于实现构造器注入（推荐优于 @Autowired 字段注入）
 *                          Spring 会自动将所需依赖注入到构造函数参数中
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * JWT 认证过滤器 - 用于解析和验证每个请求中的 JWT Token
     * 通过构造器注入（final + @RequiredArgsConstructor）
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Jackson JSON 序列化工具 - 用于将错误响应对象序列化为 JSON 字符串
     * 当认证失败时，需要将 Result 对象转为 JSON 返回给前端
     */
    private final ObjectMapper objectMapper;

    /**
     * 白名单路径数组 - 这些路径不需要认证即可访问
     *
     * 【设计说明】
     * 白名单中的路径是公开 API，任何人都可以访问：
     * - 注册/登录/刷新Token：认证相关的基本接口，用户在未登录时就需要调用
     * - doc.html 等：API 文档页面，开发调试时需要访问
     * - webjars/v3/api-docs/swagger-ui：Swagger/Knife4j 文档所需的静态资源
     *
     * 【安全注意】
     * 白名单配置需要谨慎，不要将需要认证的接口误放入白名单。
     */
    private static final String[] WHITE_LIST = {
            "/api/v1/auth/register",   // 用户注册接口
            "/api/v1/auth/login",      // 用户登录接口
            "/api/v1/auth/refresh",    // Token 刷新接口
            "/doc.html",               // Knife4j API 文档首页
            "/webjars/**",             // API 文档所需的 JS/CSS 等静态资源
            "/v3/api-docs/**",         // OpenAPI 3.0 规范的 JSON/YAML 接口文档
            "/swagger-ui/**"           // Swagger UI 资源
    };

    /**
     * 配置 Spring Security 过滤器链（核心方法）
     *
     * 【方法作用】
     * 定义完整的安全过滤策略，包括：
     * - CSRF 保护策略
     * - Session 管理策略
     * - URL 权限规则
     * - 异常处理（认证失败、权限不足）
     * - 自定义过滤器注册
     *
     * 【Spring Security 新版本说明】
     * 从 Spring Security 5.7 开始，推荐使用 SecurityFilterChain Bean 方式配置，
     * 替代之前继承 WebSecurityConfigurerAdapter 的方式（已废弃）。
     *
     * @Bean 将返回的 SecurityFilterChain 注册为 Spring Bean
     *
     * @param http HttpSecurity 对象，Spring Security 提供的 HTTP 安全配置构建器
     *             通过链式调用配置各种安全策略
     * @return SecurityFilterChain 配置完成的安全过滤器链
     * @throws Exception 配置过程中可能的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ========== 1. 禁用 CSRF 保护 ==========
                // CSRF（跨站请求伪造）保护是传统 Session/Cookie 认证下的安全措施
                // 【为什么要禁用？】
                // 因为本项目使用 JWT Token 进行认证，Token 存储在前端 localStorage 中，
                // 不会像 Cookie 那样被浏览器自动携带，所以 CSRF 攻击对 JWT 无效
                // AbstractHttpConfigurer::disable 是方法引用，等价于 csrf -> csrf.disable()
                .csrf(AbstractHttpConfigurer::disable)

                // ========== 2. Session 管理策略 ==========
                // SessionCreationPolicy.STATELESS 表示不创建和使用 HttpSession
                // 【为什么要无状态？】
                // RESTful API 的设计原则是无状态（Stateless），每次请求都应包含所有必要信息
                // JWT 方案中，用户状态存储在 Token 中而非服务端 Session 中
                // 好处：服务端不需要存储 Session，利于水平扩展（集群部署）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ========== 3. 配置 URL 权限规则 ==========
                // authorizeHttpRequests 配置基于 HttpServletRequest 的授权规则
                .authorizeHttpRequests(auth -> auth
                        // 白名单路径全部放行（permitAll = 允许所有人访问，无需认证）
                        .requestMatchers(WHITE_LIST).permitAll()
                        // 其他所有请求都需要认证（authenticated = 必须登录后才能访问）
                        .anyRequest().authenticated()
                )

                // ========== 4. 异常处理 ==========
                // exceptionHandling 配置认证和授权异常时的处理方式
                .exceptionHandling(ex -> ex
                        // authenticationEntryPoint：处理"未认证"（401）的情况
                        // 当用户未登录就访问受保护资源时触发
                        .authenticationEntryPoint((request, response, e) -> {
                            // 设置 HTTP 状态码为 401（Unauthorized）
                            response.setStatus(401);
                            // 设置响应内容类型为 JSON
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            // 设置字符编码为 UTF-8，防止中文乱码
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            // 构建统一的错误响应体
                            Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);
                            // 使用 Jackson 将 Result 对象序列化为 JSON 字符串写入响应
                            response.getWriter().write(objectMapper.writeValueAsString(result));
                        })
                        // accessDeniedHandler：处理"权限不足"（403）的情况
                        // 当已登录用户访问没有权限的资源时触发
                        .accessDeniedHandler((request, response, e) -> {
                            // 设置 HTTP 状态码为 403（Forbidden）
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            // 构建权限不足的错误响应
                            Result<Void> result = Result.fail(ResultCode.FORBIDDEN);
                            response.getWriter().write(objectMapper.writeValueAsString(result));
                        })
                )

                // ========== 5. 注册 JWT 过滤器 ==========
                // addFilterBefore：在指定的过滤器之前插入自定义过滤器
                // 将 JwtAuthenticationFilter 插入到 UsernamePasswordAuthenticationFilter 之前
                // 【为什么要放在前面？】
                // 这样 JWT 认证会在 Spring Security 默认的用户名密码认证之前执行
                // JWT 验证通过后，会设置 SecurityContext，后续的 Security 过滤器就能识别已认证的用户
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 构建并返回配置好的 SecurityFilterChain
        return http.build();
    }

    /**
     * 配置密码编码器
     *
     * 【方法作用】
     * 创建一个 BCrypt 密码编码器实例，用于用户密码的加密和验证。
     * 注册用户时用它加密密码，登录时用它验证密码是否正确。
     *
     * 【BCrypt 算法特点】
     * - 基于 Blowfish 加密算法
     * - 自带盐值（salt），每次加密结果不同，防止彩虹表攻击
     * - 可配置强度（默认 10 轮），强度越高越安全但越慢
     * - 是目前最推荐的密码哈希算法之一
     *
     * @Bean 注册为 Spring Bean，这样在 Service 层可以通过依赖注入使用
     * @return PasswordEncoder BCrypt 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     *
     * 【方法作用】
     * 从 AuthenticationConfiguration 中获取并暴露 AuthenticationManager Bean。
     * AuthenticationManager 是 Spring Security 的核心认证接口，
     * 在用户登录时调用它的 authenticate() 方法来验证用户名和密码。
     *
     * 【为什么需要手动注册？】
     * 在 Spring Security 新版本中，AuthenticationManager 不会自动注册为 Bean，
     * 需要通过 AuthenticationConfiguration 获取并手动注册。
     *
     * @Bean 注册为 Spring Bean，供登录 Service 中注入使用
     *
     * @param config Spring Boot 自动配置的 AuthenticationConfiguration
     * @return AuthenticationManager 认证管理器
     * @throws Exception 获取认证管理器时可能的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
