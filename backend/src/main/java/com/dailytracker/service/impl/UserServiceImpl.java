package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.*;
import com.dailytracker.mapper.*;
import com.dailytracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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
    private final DailySummaryMapper dailySummaryMapper;
    private final GoalPlanMapper goalPlanMapper;
    private final ObjectMapper objectMapper;

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

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public byte[] exportData(Long userId) {
        Map<String, Object> exportMap = new LinkedHashMap<>();
        exportMap.put("exportTime", LocalDateTime.now().toString());
        exportMap.put("userId", userId);

        // 用户信息
        exportMap.put("profile", getProfile(userId));

        // 每日计划
        exportMap.put("dailyPlans", dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>().eq(DailyPlan::getUserId, userId)));

        // 记账
        exportMap.put("accounting", accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>().eq(Accounting::getUserId, userId)));

        // 摘录
        exportMap.put("excerpts", excerptMapper.selectList(
                new LambdaQueryWrapper<Excerpt>().eq(Excerpt::getUserId, userId)));

        // 每日总结
        exportMap.put("summaries", dailySummaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>().eq(DailySummary::getUserId, userId)));

        // 目标计划
        exportMap.put("goals", goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>().eq(GoalPlan::getUserId, userId)));

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exportMap)
                    .getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }
    }
}
