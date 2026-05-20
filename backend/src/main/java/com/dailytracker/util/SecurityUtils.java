package com.dailytracker.util;

import com.dailytracker.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 获取当前登录用户信息的静态工具方法
 *
 * 【类的用途】
 * 提供便捷的静态方法，在 Controller 和 Service 层的任何位置获取当前登录用户的信息
 * （用户ID、用户名等），而无需通过方法参数传递。
 *
 * 【设计思想】
 * 1. 使用静态方法，无需注入任何 Bean，在任何地方都能调用
 * 2. 底层通过 SecurityContextHolder（ThreadLocal 实现）获取安全上下文
 * 3. 工具类模式（Utility Class）：私有构造函数 + 静态方法
 *
 * 【在架构中的位置】
 * 属于工具层，被 Controller 和 Service 层广泛使用。
 *
 * 【使用示例】
 * // 在 Controller 中获取当前用户ID
 * Long userId = SecurityUtils.getCurrentUserId();
 *
 * // 在 Service 中获取当前用户名
 * String username = SecurityUtils.getCurrentUsername();
 *
 * 【相关技术知识点 - SecurityContextHolder】
 * SecurityContextHolder 是 Spring Security 存储安全上下文的核心类：
 * - 使用 ThreadLocal 存储安全上下文，保证线程安全
 * - 每个 HTTP 请求线程都有独立的安全上下文
 * - JwtAuthenticationFilter 设置认证信息后，此工具类就能读取到
 * - 默认策略：MODE_THREADLOCAL（每个线程独立）
 * - 在异步场景中可能需要使用 MODE_INHERITABLETHREADLOCAL
 *
 * 【注意事项】
 * 1. 只有在用户已认证的情况下才能调用，否则会抛出 IllegalStateException
 * 2. 在非 Web 请求线程（如定时任务、异步线程）中调用会失败
 * 3. 在 SecurityConfig 白名单路径的 Controller 中调用也会失败（因为未认证）
 */
public class SecurityUtils {

    /**
     * 私有构造函数 - 禁止实例化
     *
     * 工具类不需要创建实例，所有方法都是静态的。
     * 将构造函数设为私有，防止其他类通过 new SecurityUtils() 创建实例。
     */
    private SecurityUtils() {}

    /**
     * 获取当前登录用户的 ID
     *
     * 【方法作用】
     * 从 Spring Security 的安全上下文中获取当前已认证用户的 ID。
     * 这是最常用的方法，因为大部分业务操作都需要按用户隔离数据。
     *
     * 【工作原理】
     * 1. SecurityContextHolder.getContext() 获取当前线程的安全上下文
     * 2. getAuthentication() 获取认证对象（由 JwtAuthenticationFilter 设置）
     * 3. getPrincipal() 获取认证主体（即 SecurityUser 对象）
     * 4. 使用 Java 17 的模式匹配 instanceof 语法，同时判断类型并转换
     * 5. 调用 getUserId() 获取用户ID
     *
     * 【Java 17 模式匹配语法】
     * authentication.getPrincipal() instanceof SecurityUser securityUser
     * 这行代码做了两件事：
     * 1. 判断 principal 是否是 SecurityUser 类型
     * 2. 如果是，自动将其转换为 SecurityUser 并赋值给变量 securityUser
     * 等价于旧写法：
     * if (authentication.getPrincipal() instanceof SecurityUser) {
     *     SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
     * }
     *
     * @return Long 当前登录用户的 ID
     * @throws IllegalStateException 如果用户未登录或安全上下文中没有用户信息
     */
    public static Long getCurrentUserId() {
        // 获取认证对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 检查认证对象存在且主体是 SecurityUser 类型（使用 Java 17 模式匹配）
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUserId();   // 返回用户ID
        }
        // 如果无法获取用户信息，抛出异常（这是编程错误，通常表示在未认证的上下文中调用了此方法）
        throw new IllegalStateException("无法获取当前用户信息");
    }

    /**
     * 获取当前登录用户的用户名
     *
     * 【方法作用】
     * 从 Spring Security 的安全上下文中获取当前已认证用户的用户名。
     *
     * @return String 当前登录用户的用户名
     * @throws IllegalStateException 如果用户未登录或安全上下文中没有用户信息
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUsername();   // 返回用户名
        }
        throw new IllegalStateException("无法获取当前用户信息");
    }
}
