package com.dailytracker.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 用户对象 - 封装认证后的用户信息
 *
 * 【类的用途】
 * 实现 Spring Security 的 UserDetails 接口，将数据库中的用户信息
 * 包装成 Spring Security 能够识别和使用的用户对象。
 *
 * 这个对象在以下场景中被使用：
 * 1. UserDetailsService 加载用户信息时返回此对象
 * 2. JwtAuthenticationFilter 验证 Token 成功后，将此对象放入 SecurityContext
 * 3. Controller/Service 中通过 SecurityContextHolder 获取当前登录用户信息
 *
 * 【设计思想】
 * 使用适配器模式（Adapter Pattern），将应用层的用户实体（数据库用户表）
 * 适配为 Spring Security 框架要求的 UserDetails 接口。
 * 额外添加了 userId 字段，因为 Spring Security 默认只关注 username，
 * 但我们的业务逻辑通常需要 userId。
 *
 * 【在架构中的位置】
 * 属于安全层，是连接用户数据层和 Spring Security 框架的桥梁。
 *
 * 【相关技术知识点 - UserDetails 接口】
 * UserDetails 是 Spring Security 的核心接口，代表一个安全实体（用户）。
 * Spring Security 通过此接口获取用户信息来进行认证和授权判断。
 * 接口方法说明：
 *   - getAuthorities()：获取用户的权限集合
 *   - getPassword()：获取用户密码（用于密码验证）
 *   - getUsername()：获取用户名（用于标识用户）
 *   - isAccountNonExpired()：账号是否未过期
 *   - isAccountNonLocked()：账号是否未锁定
 *   - isCredentialsNonExpired()：凭证是否未过期
 *   - isEnabled()：账号是否启用
 *
 * @Data Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法
 */
@Data
public class SecurityUser implements UserDetails {

    /**
     * 用户ID - 来自数据库用户表的主键
     * 这是自定义字段，不在 UserDetails 接口中，但业务中经常需要用到
     */
    private Long userId;

    /**
     * 用户名 - 用于登录和标识用户
     * 对应 UserDetails 接口的 getUsername() 方法
     */
    private String username;

    /**
     * 密码 - BCrypt 加密后的密码哈希值
     * 对应 UserDetails 接口的 getPassword() 方法
     * 【注意】密码只用于认证比对，不应在日志或响应中暴露
     */
    private String password;

    /**
     * 账号是否启用 - true 表示正常，false 表示被管理员禁用
     * 对应 UserDetails 接口的 isEnabled() 方法
     * 被禁用的用户无法登录
     */
    private boolean enabled;

    /**
     * 构造函数 - 创建 SecurityUser 实例
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param password 加密后的密码
     * @param enabled  是否启用
     */
    public SecurityUser(Long userId, String username, String password, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    /**
     * 获取用户的权限集合
     *
     * 【方法作用】
     * 返回用户拥有的所有权限（角色/操作权限）。
     * Spring Security 使用这些权限进行访问控制（如 @PreAuthorize 注解）。
     *
     * 【当前实现】
     * 返回空列表（Collections.emptyList()），表示当前系统不使用基于角色的权限控制，
     * 所有已认证用户拥有相同的权限。
     *
     * 【扩展说明】
     * 如果需要实现角色权限控制，可以返回包含权限信息的列表：
     * return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
     * 然后在 Controller 方法上使用 @PreAuthorize("hasRole('ADMIN')") 进行权限控制
     *
     * @return Collection<? extends GrantedAuthority> 权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();   // 当前系统无角色权限控制，返回空列表
    }

    /**
     * 账号是否未过期
     *
     * 【返回值说明】
     * 固定返回 true，表示账号永不过期。
     * 如果需要实现账号有效期功能，可以根据数据库中的过期时间字段判断。
     *
     * @return boolean true=未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定
     *
     * 【返回值说明】
     * 固定返回 true，表示账号永不被锁定。
     * 如果需要实现账号锁定功能（如多次密码错误后锁定），可以在此方法中判断。
     *
     * @return boolean true=未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证（密码）是否未过期
     *
     * 【返回值说明】
     * 固定返回 true，表示密码永不过期。
     * 如果需要实现密码定期更换策略，可以在此方法中判断密码是否过期。
     *
     * @return boolean true=未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用
     *
     * 【返回值说明】
     * 返回 enabled 字段的值。
     * 当返回 false 时，Spring Security 会拒绝该用户的登录请求。
     * 管理员可以通过修改数据库中的 enabled 字段来禁用/启用用户。
     *
     * @return boolean true=启用，false=禁用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
