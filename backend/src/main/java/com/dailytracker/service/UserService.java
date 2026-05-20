package com.dailytracker.service;

import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.UserSettings;

import java.util.Map;

/**
 * 用户服务接口（User Service Interface）
 *
 * 【职责说明】
 * 本接口负责用户信息的全生命周期管理，包括：
 * - 个人信息的查看与修改（昵称、邮箱、手机、签名等）
 * - 密码修改（需要验证旧密码）
 * - 用户偏好设置的读取与更新（主题、语言、默认视图等）
 * - 头像更新
 * - 用户全量数据导出（数据可移植性，支持 GDPR 等合规需求）
 *
 * 【与 AuthService 的职责划分】
 * - AuthService：关注"认证"（登录、注册、令牌管理）
 * - UserService：关注"用户信息管理"（个人资料、偏好设置）
 * 这种划分遵循"单一职责原则"（SRP），使得每个服务接口的内聚性更强。
 *
 * 【设计模式】
 * - 面向接口编程：Controller 层通过注入 UserService 接口来调用方法，
 *   具体实现由 Spring 容器自动注入（通常为 UserServiceImpl）。
 * - DTO 模式：方法参数和返回值使用 DTO（Data Transfer Object）对象，
 *   而非直接暴露实体类（Entity），有效保护内部数据结构。
 */
public interface UserService {

    /**
     * 获取用户个人信息
     *
     * 【业务流程】
     * 1. 根据用户ID从数据库查询用户实体
     * 2. 校验用户是否存在
     * 3. 将实体转换为响应 DTO（过滤掉密码等敏感字段）并返回
     *
     * @param userId 用户ID（由 SecurityUtils 从当前登录上下文中获取）
     * @return UserProfileResponse 用户个人信息响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 用户不存在时抛出异常
     */
    UserProfileResponse getProfile(Long userId);

    /**
     * 更新个人信息（昵称、邮箱、手机、签名）
     *
     * 【业务流程】
     * 1. 构建更新实体，设置用户ID及待更新字段
     * 2. 调用 MyBatis-Plus 的 updateById 方法进行部分字段更新
     *
     * 【设计说明】
     * 采用"部分更新"策略，仅更新传入的非空字段，避免覆盖已有数据。
     * 方法使用 @Transactional 注解确保数据库操作的原子性。
     *
     * @param userId    用户ID
     * @param nickname  新昵称（可为 null，表示不更新）
     * @param email     新邮箱（可为 null）
     * @param phone     新手机号（可为 null）
     * @param signature 新个性签名（可为 null）
     */
    void updateProfile(Long userId, String nickname, String email, String phone, String signature);

    /**
     * 修改密码
     *
     * 【业务流程】
     * 1. 查询当前用户信息
     * 2. 使用 PasswordEncoder 验证旧密码是否正确
     * 3. 对新密码进行加密并更新到数据库
     *
     * 【安全设计】
     * - 旧密码验证：防止未授权的密码修改（即使攻击者拿到了 sessionId）
     * - 密码加密存储：使用 BCrypt 算法，每次加密结果不同（含随机盐值）
     *
     * @param userId  用户ID
     * @param request 修改密码请求 DTO，包含旧密码和新密码
     * @throws com.dailytracker.common.exception.BusinessException 旧密码不正确时抛出异常
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 获取用户偏好设置
     *
     * 【业务流程】
     * 1. 根据用户ID查询偏好设置记录
     * 2. 如果记录不存在，返回默认设置（首次使用的用户）
     *
     * 【设计说明】
     * 采用"懒初始化"策略：用户首次访问偏好设置时，不立即创建数据库记录，
     * 而是返回内存中的默认值。只有当用户主动修改设置时，才持久化到数据库。
     * 这样可以减少不必要的数据库操作。
     *
     * @param userId 用户ID
     * @return UserSettings 用户偏好设置实体
     */
    UserSettings getSettings(Long userId);

    /**
     * 更新用户偏好设置
     *
     * 【业务流程】
     * 1. 查询当前用户是否已有偏好设置记录
     * 2. 如果不存在 -> 执行 INSERT（首次保存设置）
     * 3. 如果已存在 -> 执行 UPDATE（更新已有设置）
     *
     * 这种"存在则更新，不存在则插入"的模式也称为 "Upsert" 操作。
     *
     * @param userId   用户ID
     * @param settings 新的偏好设置实体
     */
    void updateSettings(Long userId, UserSettings settings);

    /**
     * 更新头像
     *
     * 【业务流程】
     * 1. 构建用户实体，仅设置 ID 和新的头像 URL
     * 2. 调用 updateById 进行部分更新（MyBatis-Plus 默认忽略 null 字段）
     *
     * @param userId    用户ID
     * @param avatarUrl 头像URL（通常由文件上传服务返回的 MinIO 文件访问地址）
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 导出用户全量数据
     *
     * 【业务流程】
     * 1. 查询用户基本信息（并脱敏处理，如清除密码字段）
     * 2. 查询用户的偏好设置
     * 3. 查询用户的每日计划数据
     * 4. 查询用户的记账数据
     * 5. 查询用户的每日摘录数据
     * 6. 查询用户的目标计划数据
     * 7. 查询用户的每日总结数据
     * 8. 查询用户的预算数据
     * 9. 将所有数据组装为 Map 返回
     *
     * 【设计说明】
     * 该方法一次性读取用户的所有关联数据，适用于数据备份、迁移或合规导出场景。
     * 返回类型为 Map<String, Object>，是一种灵活但类型安全性较弱的设计。
     * 在更严格的架构中，可以考虑定义专门的 ExportDataResponse DTO。
     *
     * @param userId 用户ID
     * @return 数据Map，包含用户所有业务数据的键值对集合
     */
    Map<String, Object> exportData(Long userId);
}
