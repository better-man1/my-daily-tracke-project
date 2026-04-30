package com.dailytracker.service;

import com.dailytracker.dto.request.LoginRequest;
import com.dailytracker.dto.request.RegisterRequest;
import com.dailytracker.dto.request.WxLoginRequest;
import com.dailytracker.dto.response.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 微信登录
     */
    LoginResponse wxLogin(WxLoginRequest request);
}
