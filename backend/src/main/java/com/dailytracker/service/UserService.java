package com.dailytracker.service;

import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.UserSettings;

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
     * 导出用户全量数据（JSON格式）
     */
    byte[] exportData(Long userId);
}
