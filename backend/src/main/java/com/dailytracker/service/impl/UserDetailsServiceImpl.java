package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.entity.User;
import com.dailytracker.mapper.UserMapper;
import com.dailytracker.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService 实现类
 *
 * 【类设计说明】
 * 本类实现了 Spring Security 框架的 UserDetailsService 接口，
 * 是 Spring Security 认证流程的核心组件之一。
 *
 * 【Spring Security 认证流程】
 * 当用户提交登录请求时，Spring Security 的认证过滤器（如 UsernamePasswordAuthenticationFilter）
 * 会调用 UserDetailsService.loadUserByUsername() 方法来加载用户信息，
 * 然后将返回的 UserDetails 对象与用户提交的凭据进行比对。
 *
 * 【注解解释】
 * @Service    - Spring 注解，标记为 Bean。Spring Security 会自动发现并使用此实现。
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入 UserMapper。
 *
 * 【设计说明】
 * 返回的 SecurityUser 对象封装了 Spring Security 需要的用户信息：
 * - 用户ID、用户名、密码（密文）、是否启用
 * Spring Security 会自动使用这些信息进行密码比对和权限判断。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情（Spring Security 回调方法）
     *
     * 【调用时机】
     * 在 Spring Security 的认证流程中，当用户尝试登录时，
     * 框架会自动调用此方法来加载用户的认证信息。
     *
     * 【业务流程】
     * 1. 根据用户名查询数据库（排除已逻辑删除的用户）
     * 2. 如果用户不存在，抛出 UsernameNotFoundException
     * 3. 如果用户被禁用，抛出 BusinessException
     * 4. 将用户信息封装为 SecurityUser（UserDetails 的实现类）返回
     *
     * @param username 用户名
     * @return UserDetails Spring Security 的用户详情对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getIsDeleted, 0)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return new SecurityUser(user.getId(), user.getUsername(), user.getPassword(), user.getStatus() == 1);
    }
}
