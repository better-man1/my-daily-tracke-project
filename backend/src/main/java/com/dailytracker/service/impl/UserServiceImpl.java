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
 * 用户服务实现类（User Service Implementation）
 *
 * 【类设计说明】
 * 本类是 UserService 接口的具体实现，负责用户个人信息管理、密码修改、偏好设置、数据导出等功能。
 *
 * 【注解解释】
 * @Service    - Spring 注解，将此类标记为业务层 Bean，由 Spring 容器管理生命周期和依赖注入。
 * @RequiredArgsConstructor - Lombok 注解，自动生成包含所有 final 字段的构造器，实现构造器注入。
 *
 * 【依赖注入的字段说明】
 * 该类注入了多个 Mapper 接口，这是因为 exportData 方法需要聚合查询多个业务模块的数据。
 * 虽然注入了较多依赖，但这些依赖都是为了 exportData 这一聚合查询功能服务的。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 用户数据访问层 */
    private final UserMapper userMapper;
    /** 用户偏好设置数据访问层 */
    private final UserSettingsMapper userSettingsMapper;
    /** 密码编码器（BCrypt），用于密码加密和验证 */
    private final PasswordEncoder passwordEncoder;
    /** 每日计划数据访问层（用于数据导出） */
    private final DailyPlanMapper dailyPlanMapper;
    /** 记账数据访问层（用于数据导出） */
    private final AccountingMapper accountingMapper;
    /** 摘录数据访问层（用于数据导出） */
    private final ExcerptMapper excerptMapper;
    /** 目标计划数据访问层（用于数据导出） */
    private final GoalPlanMapper goalPlanMapper;
    /** 每日总结数据访问层（用于数据导出） */
    private final DailySummaryMapper dailySummaryMapper;
    /** 预算数据访问层（用于数据导出） */
    private final BudgetMapper budgetMapper;

    /**
     * 获取用户个人信息
     *
     * 【业务流程】
     * 1. 调用私有方法 getUser 获取用户实体（包含不存在校验）
     * 2. 将实体属性逐个复制到响应 DTO 中
     * 注意：不使用 BeanUtils.copyProperties 是为了明确控制哪些字段对外暴露（安全考虑）
     *
     * @param userId 用户ID
     * @return UserProfileResponse 用户个人信息响应 DTO
     */
    @Override
    public UserProfileResponse getProfile(Long userId) {
        // 获取用户实体（包含不存在时的异常抛出）
        User user = getUser(userId);
        // 手动映射字段到响应 DTO（比 BeanUtils.copyProperties 更安全可控）
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

    /**
     * 更新个人信息
     *
     * 【设计说明】
     * 使用 MyBatis-Plus 的 updateById 方法进行部分字段更新。
     * 只有设置了非 null 值的字段才会被更新（MyBatis-Plus 默认策略）。
     * 使用 @Transactional 确保更新的原子性。
     *
     * @param userId    用户ID
     * @param nickname  昵称
     * @param email     邮箱
     * @param phone     手机号
     * @param signature 个性签名
     */
    @Override
    @Transactional
    public void updateProfile(Long userId, String nickname, String email, String phone, String signature) {
        User user = new User();
        user.setId(userId);            // 指定要更新的记录ID
        user.setNickname(nickname);    // 待更新的昵称
        user.setEmail(email);          // 待更新的邮箱
        user.setPhone(phone);          // 待更新的手机号
        user.setSignature(signature);  // 待更新的签名
        // updateById 只更新非 null 的字段
        userMapper.updateById(user);
    }

    /**
     * 修改密码
     *
     * 【安全设计】
     * 1. 先验证旧密码是否正确，防止未授权修改
     * 2. 使用 BCrypt 加密新密码后存储
     * 3. 使用 @Transactional 保证密码更新的原子性
     *
     * @param userId  用户ID
     * @param request 密码修改请求（含旧密码和新密码）
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 获取当前用户信息
        User user = getUser(userId);
        // 验证旧密码是否正确（明文与密文比对）
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_OLD_PASSWORD_ERROR);
        }
        // 构建更新实体，只设置ID和新密码
        User updateUser = new User();
        updateUser.setId(userId);
        // 对新密码进行加密后存储
        updateUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(updateUser);
    }

    /**
     * 获取用户偏好设置
     *
     * 【懒初始化策略】
     * 如果用户从未修改过设置（数据库无记录），返回内存中构建的默认值。
     * 默认值：浅色主题(light)、看板首页(dashboard)、周一开始(1)、中文(zh-CN)。
     *
     * @param userId 用户ID
     * @return UserSettings 用户偏好设置
     */
    @Override
    public UserSettings getSettings(Long userId) {
        // 根据用户ID查询偏好设置记录
        UserSettings settings = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, userId)
        );
        if (settings == null) {
            // 如果没有设置记录，返回默认值（不持久化，只在内存中创建）
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme("light");          // 默认浅色主题
            settings.setDefaultView("dashboard"); // 默认首页为看板
            settings.setWeekStartDay(1);          // 默认周一开始（ISO标准）
            settings.setLanguage("zh-CN");        // 默认中文简体
        }
        return settings;
    }

    /**
     * 更新用户偏好设置（Upsert 模式）
     *
     * 【业务逻辑】
     * - 如果记录不存在（首次修改设置）-> INSERT 新记录
     * - 如果记录已存在 -> UPDATE 已有记录
     *
     * @param userId     用户ID
     * @param newSettings 新的偏好设置
     */
    @Override
    @Transactional
    public void updateSettings(Long userId, UserSettings newSettings) {
        // 查询当前是否已有设置记录
        UserSettings existing = userSettingsMapper.selectOne(
                new LambdaQueryWrapper<UserSettings>().eq(UserSettings::getUserId, userId)
        );
        // 确保用户ID正确
        newSettings.setUserId(userId);
        if (existing == null) {
            // 首次保存设置，执行 INSERT
            userSettingsMapper.insert(newSettings);
        } else {
            // 已有设置记录，设置主键后执行 UPDATE
            newSettings.setId(existing.getId());
            userSettingsMapper.updateById(newSettings);
        }
    }

    /**
     * 更新头像
     *
     * 仅更新头像 URL 字段，MyBatis-Plus 的 updateById 会忽略 null 字段。
     *
     * @param userId    用户ID
     * @param avatarUrl 新头像的访问 URL（由 MinIO 文件存储服务返回）
     */
    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    /**
     * 导出用户全量数据
     *
     * 【设计说明】
     * 该方法聚合查询用户所有业务模块的数据，组装为一个 Map 返回。
     * 适用于数据备份、迁移或合规导出场景。
     *
     * 【数据脱敏】
     * 用户密码字段在导出前被设为 null，防止敏感信息泄露。
     *
     * @param userId 用户ID
     * @return 包含所有业务数据的 Map
     */
    @Override
    public Map<String, Object> exportData(Long userId) {
        Map<String, Object> data = new HashMap<>();

        // 1. 用户基本信息
        User user = getUser(userId);
        user.setPassword(null); // 数据脱敏：清除密码字段，防止敏感信息泄露
        data.put("user", user);

        // 2. 用户偏好设置
        data.put("settings", getSettings(userId));

        // 3. 每日计划数据（排除已逻辑删除的记录）
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

        // 5. 每日摘录数据
        data.put("excerpts", excerptMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.Excerpt>()
                        .eq(com.dailytracker.entity.Excerpt::getUserId, userId)
                        .eq(com.dailytracker.entity.Excerpt::getIsDeleted, 0)
        ));

        // 6. 目标计划数据
        data.put("goals", goalPlanMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.GoalPlan>()
                        .eq(com.dailytracker.entity.GoalPlan::getUserId, userId)
                        .eq(com.dailytracker.entity.GoalPlan::getIsDeleted, 0)
        ));

        // 7. 每日总结数据
        data.put("summaries", dailySummaryMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.DailySummary>()
                        .eq(com.dailytracker.entity.DailySummary::getUserId, userId)
                        .eq(com.dailytracker.entity.DailySummary::getIsDeleted, 0)
        ));

        // 8. 预算数据
        data.put("budgets", budgetMapper.selectList(
                new LambdaQueryWrapper<com.dailytracker.entity.Budget>()
                        .eq(com.dailytracker.entity.Budget::getUserId, userId)
        ));

        return data;
    }

    /**
     * 根据用户ID获取用户实体（私有辅助方法）
     *
     * 【设计说明】
     * 这是一个被多个公共方法复用的私有方法，封装了"查询用户 + 不存在校验"的通用逻辑。
     * 如果用户不存在，直接抛出 BusinessException，避免上层方法重复编写校验代码。
     * 这体现了 DRY 原则和"快速失败"（Fail-Fast）的设计思想。
     *
     * @param userId 用户ID
     * @return User 用户实体
     * @throws BusinessException 用户不存在时抛出异常
     */
    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }
}
