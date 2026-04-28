package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人信息响应
 */
@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String signature;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
