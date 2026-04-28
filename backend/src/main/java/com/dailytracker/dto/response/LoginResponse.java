package com.dailytracker.dto.response;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpireIn;
}
