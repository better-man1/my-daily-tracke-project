package com.dailytracker.security;

import com.dailytracker.common.result.Result;
import com.dailytracker.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器 - 拦截每个 HTTP 请求，验证 JWT Token 并设置认证信息
 *
 * 【类的用途】
 * 这是 JWT 认证方案的核心过滤器，每次 HTTP 请求到达 Controller 之前都会执行。
 * 主要职责：
 * 1. 从 HTTP 请求头中提取 JWT Token
 * 2. 验证 Token 的有效性（签名、过期时间）
 * 3. 从 Token 中解析用户信息
 * 4. 将认证信息设置到 Spring Security 的安全上下文中
 * 5. 如果 Token 无效，返回 JSON 格式的错误响应
 *
 * 【设计思想】
 * 继承 OncePerRequestFilter 确保每个请求只过滤一次（避免重复执行）。
 * 通过 SecurityContextHolder 设置认证信息，使 Spring Security 后续的授权检查能够识别当前用户。
 *
 * 【在架构中的位置】
 * 位于 Spring Security 过滤器链中，在 UsernamePasswordAuthenticationFilter 之前执行。
 * 请求处理流程：HTTP 请求 -> JwtAuthenticationFilter -> Security Filter Chain -> Controller
 *
 * 【相关技术知识点 - Servlet Filter】
 * 1. Filter（过滤器）是 Servlet 规范中的组件，可以在请求到达 Servlet 之前和之后进行处理
 * 2. OncePerRequestFilter 是 Spring 提供的抽象类，保证每个请求只执行一次过滤逻辑
 *    这在 Servlet 容器分发（forward/include）场景下很重要，避免重复过滤
 * 3. FilterChain（过滤器链）是多个 Filter 的有序集合，请求必须依次通过所有 Filter
 *
 * @Slf4j Lombok 注解，自动生成日志对象 log
 * @Component Spring 组件注解，注册为 Spring Bean
 *            被 SecurityConfig 中 addFilterBefore() 引用
 * @RequiredArgsConstructor Lombok 注解，自动生成包含 final 字段的构造函数
 *                          实现依赖注入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT Token 工具类 - 用于验证和解析 Token
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Spring Security 用户详情服务 - 用于根据用户名加载用户信息
     */
    private final UserDetailsService userDetailsService;

    /**
     * Jackson JSON 序列化工具 - 用于将错误响应序列化为 JSON
     */
    private final ObjectMapper objectMapper;

    /**
     * 过滤器的核心处理方法 - 每个请求都会执行此方法
     *
     * 【方法作用】
     * 完整的 JWT 认证流程：
     * 1. 从请求头中提取 Token
     * 2. 如果有 Token，验证其有效性
     * 3. 验证通过 -> 加载用户信息 -> 设置安全上下文 -> 放行请求
     * 4. 验证失败 -> 返回 Token 无效的错误响应
     * 5. 如果没有 Token -> 直接放行（由后续的 Security 配置决定是否允许访问）
     *
     * 【重写说明】
     * @Override 重写 OncePerRequestFilter 的 doFilterInternal 方法
     * Spring 保证此方法在每个 HTTP 请求中只执行一次
     *
     * @param request     当前 HTTP 请求对象
     * @param response    当前 HTTP 响应对象
     * @param filterChain 过滤器链，调用 filterChain.doFilter() 将请求传递给下一个过滤器
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 第一步：从请求头中提取 JWT Token
        String token = resolveToken(request);

        // 第二步：判断 Token 是否存在（StringUtils.hasText 同时检查非 null 和非空字符串）
        if (StringUtils.hasText(token)) {
            // 第三步：验证 Token 是否有效
            if (jwtTokenProvider.validateToken(token)) {
                // Token 有效 -> 从 Token 中获取用户名
                String username = jwtTokenProvider.getUsernameFromToken(token);

                // 通过 UserDetailsService 从数据库（或缓存）加载完整的用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 创建 Spring Security 的认证对象
                // UsernamePasswordAuthenticationToken 是 Spring Security 中最常用的认证实现
                // 参数说明：
                //   1. principal - 主体（用户信息），这里传入 userDetails
                //   2. credentials - 凭证（密码），JWT 模式下不需要，设为 null
                //   3. authorities - 权限列表，从 userDetails 中获取
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 将请求的详细信息（如远程IP、Session ID等）设置到认证对象中
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 【关键步骤】将认证对象设置到 SecurityContext 中
                // SecurityContextHolder 是 Spring Security 的核心类，使用 ThreadLocal 存储安全上下文
                // 设置后，后续的 Security 过滤器和 Controller 都可以通过 SecurityContextHolder
                // 获取当前认证用户的信息
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // Token 验证失败（已过期或签名无效）-> 返回错误响应
                // 注意：这里直接写入响应并 return，不再继续过滤器链
                writeErrorResponse(response, ResultCode.USER_TOKEN_INVALID);
                return;   // 终止请求，不传递给后续过滤器和 Controller
            }
        }

        // 如果没有 Token 或 Token 验证成功，继续执行过滤器链
        // 没有 Token 的情况由 Spring Security 的授权规则（如 WHITE_LIST）决定是否放行
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token
     *
     * 【方法作用】
     * 从 Authorization 请求头中提取 Bearer Token。
     * Authorization 头的格式为：Bearer eyJhbGciOiJIUzI1NiJ9...
     * 需要去掉 "Bearer " 前缀，只保留 Token 部分。
     *
     * @param request HTTP 请求对象
     * @return String Token 字符串，如果不存在则返回 null
     */
    private String resolveToken(HttpServletRequest request) {
        // 获取 Authorization 请求头的值
        String bearerToken = request.getHeader("Authorization");

        // 检查 Token 是否存在且以 "Bearer " 开头
        // "Bearer " 长度为 7 个字符，所以 substring(7) 获取后面的 Token 部分
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);   // 截取 "Bearer " 之后的部分
        }
        return null;
    }

    /**
     * 向客户端写入 JSON 格式的错误响应
     *
     * 【方法作用】
     * 当 Token 无效时，直接向 HTTP 响应中写入 JSON 格式的错误信息，
     * 不走 Spring MVC 的异常处理流程（因为此时还没到 Controller）。
     *
     * @param response   HTTP 响应对象
     * @param resultCode 错误码枚举
     * @throws IOException 写入响应时的 IO 异常
     */
    private void writeErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        // 设置 HTTP 状态码为 401（未授权）
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置响应内容类型为 JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 设置字符编码为 UTF-8
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 构建错误响应体并序列化为 JSON 写入响应
        Result<Void> result = Result.fail(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
