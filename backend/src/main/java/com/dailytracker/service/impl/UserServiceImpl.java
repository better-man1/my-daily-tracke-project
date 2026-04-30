package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.User;
import com.dailytracker.entity.UserSettings;
import com.dailytracker.mapper.*;
import com.dailytracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserSettingsMapper userSettingsMapper;
    private final PasswordEncoder passwordEncoder;
    private final DailyPlanMapper dailyPlanMapper;
    private final AccountingMapper accountingMapper;
    private final ExcerptMapper excerptMapper;
    private final GoalPlanMapper goalPlanMapper;
    private final DailySummaryMapper dailySummaryMapper;
    private final BudgetMapper budgetMapper;

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = getUser(userId);
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setSignature(user.getSignature());
        response.setStatus(user.getStatus());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String nickname, String email, String phone, String signature) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        user.setSignature(signature);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_OLD_PASSWORD_ERROR);
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(updateUser);
    }

    @Override
    public UserSettings getSettings(Long userId) {
        UserSettings settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, userId)
        );
        if (settings == null) {
            // 如果没有设置，返回默认值
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme("light");
            settings.setDefaultView("dashboard");
            settings.setWeekStartDay(1);
            settings.setLanguage("zh-CN");
        }
        return settings;
    }

    @Override
    @Transactional
    public void updateSettings(Long userId, UserSettings newSettings) {
        UserSettings existing = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, userId)
        );
        newSettings.setUserId(userId);
        if (existing == null) {
            userSettingsMapper.insert(newSettings);
        } else {
            newSettings.setId(existing.getId());
            userSettingsMapper.updateById(newSettings);
        }
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public Map<String, Object> exportData(Long userId) {
        Map<String, Object> data = new HashMap<>();
        
        // 1. 用户信息
        User user = getUser(userId);
        user.setPassword(null); // 导出时脱敏
        data.put("user", user);
        
        // 2. 偏好设置
        data.put("settings", getSettings(userId));
        
        // 3. 每日计划
        data.put("dailyPlans", dailyPlanMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.DailyPlan>()
                        .eq(com.dailytracker.entity.DailyPlan::getUserId, userId)
                        .eq(com.dailytracker.entity.DailyPlan::getIsDeleted, 0)
        ));
        
        // 4. 记账数据
        data.put("accounting", accountingMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.Accounting>()
                        .eq(com.dailytracker.entity.Accounting::getUserId, userId)
                        .eq(com.dailytracker.entity.Accounting::getIsDeleted, 0)
        ));
        
        // 5. 每日摘录
        data.put("excerpts", excerptMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.Excerpt>()
                        .eq(com.dailytracker.entity.Excerpt::getUserId, userId)
                        .eq(com.dailytracker.entity.Excerpt::getIsDeleted, 0)
        ));
        
        // 6. 目标计划
        data.put("goals", goalPlanMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.GoalPlan>()
                        .eq(com.dailytracker.entity.GoalPlan::getUserId, userId)
                        .eq(com.dailytracker.entity.GoalPlan::getIsDeleted, 0)
        ));
        
        // 7. 每日总结
        data.put("summaries", dailySummaryMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.DailySummary>()
                        .eq(com.dailytracker.entity.DailySummary::getUserId, userId)
                        .eq(com.dailytracker.entity.DailySummary::getIsDeleted, 0)
        ));

        // 8. 预算
        data.put("budgets", budgetMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.Budget>()
                        .eq(com.dailytracker.entity.Budget::getUserId, userId)
        ));
        
        return data;
    }

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }
}
