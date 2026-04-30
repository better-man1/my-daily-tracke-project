package com.dailytracker.service;

import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.UserSettings;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 获取用户个人信息
     */
    UserProfileResponse getProfile(Long userId);

    /**
     * 更新个人信息（昵称、头像、邮箱、手机、签名）
     */
    void updateProfile(Long userId, String nickname, String email, String phone, String signature);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 获取用户偏好设置
     */
    UserSettings getSettings(Long userId);

    /**
     * 更新用户偏好设置
     */
    void updateSettings(Long userId, UserSettings settings);

    /**
     * 更新头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像URL
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 导出用户全量数据
     *
     * @param userId 用户ID
     * @return 数据Map
     */
    Map<String, Object> exportData(Long userId);
}
