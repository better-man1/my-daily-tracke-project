package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
// Result: 统一响应封装类，将API返回值包装为统一JSON格式（code + message + data）
import com.dailytracker.common.result.Result;
// ChangePasswordRequest: 修改密码请求DTO，包含旧密码和新密码字段
import com.dailytracker.dto.request.ChangePasswordRequest;
// UserProfileResponse: 用户个人信息响应DTO，包含昵称、邮箱、头像、签名等字段
import com.dailytracker.dto.response.UserProfileResponse;
// UserSettings: 用户偏好设置实体类，包含主题、语言、通知开关等设置项
import com.dailytracker.entity.UserSettings;
// UserService: 用户业务逻辑层接口，处理用户信息管理、密码修改、数据导出等
import com.dailytracker.service.UserService;
// SecurityUtils: 安全工具类，从Spring Security上下文中获取当前登录用户的ID
// 底层原理：从SecurityContextHolder -> SecurityContext -> Authentication -> Principal中提取用户信息
import com.dailytracker.util.SecurityUtils;
// FileStorageService: 文件存储服务接口，处理文件上传到对象存储（如阿里云OSS、腾讯云COS等）
import com.dailytracker.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
// MultipartFile: Spring提供的文件上传接口，封装了上传文件的内容、名称、大小等信息
import org.springframework.web.multipart.MultipartFile;

// Map: Java集合框架中的键值对映射接口，这里用于接收动态的请求体字段
import java.util.Map;

/**
 * 用户管理控制器（UserController）
 *
 * 【职责说明】
 * 本控制器负责处理登录用户个人信息管理相关的API请求，包括：
 *   1. 获取/更新个人资料（昵称、邮箱、手机号、个性签名）
 *   2. 修改登录密码
 *   3. 获取/更新偏好设置（主题、语言、通知等）
 *   4. 上传用户头像
 *   5. 导出用户全量数据（数据可携带性，符合GDPR等隐私法规要求）
 *
 * 【RESTful设计思想】
 * - 资源路径 /api/v1/users 中的"users"是资源名词
 * - 子资源通过路径层级表达：/profile（个人资料）、/password（密码）、/settings（设置）、/avatar（头像）
 * - 使用HTTP方法区分操作：GET获取、PUT更新、POST上传/导出
 * - 用户ID不从URL传递，而是从JWT令牌中提取（SecurityUtils.getCurrentUserId()）
 *   这是一种安全设计：防止用户A通过修改URL中的ID来访问用户B的数据
 *
 * 【API路径前缀】/api/v1/users
 */
@Tag(name = "用户管理", description = "个人信息、密码、偏好设置")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    // 依赖注入：UserService处理用户信息的增删改查、密码修改等业务逻辑
    private final UserService userService;
    // 依赖注入：FileStorageService处理文件上传到云存储的逻辑
    private final FileStorageService fileStorageService;

    /**
     * 获取当前登录用户的个人信息
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/users/profile
     *
     * 【参数说明】无需参数，用户ID从JWT令牌中自动提取
     *
     * 【返回值】Result<UserProfileResponse> - 包含用户昵称、邮箱、手机号、头像URL、签名等信息
     *
     * 【设计说明】
     * - 不在URL中暴露用户ID（如GET /users/1/profile），而是从Token中获取
     * - 这样用户只能查看自己的信息，无法查看他人信息（安全隔离）
     * - SecurityUtils.getCurrentUserId() 从Spring Security上下文中获取当前认证用户的ID
     */
    @Operation(summary = "获取个人信息")
    @GetMapping("/profile") // 映射HTTP GET请求到 /api/v1/users/profile
    public Result<UserProfileResponse> getProfile() {
        // 从JWT令牌（通过Spring Security上下文）中获取当前登录用户的ID
        // 前端无需传递用户ID，后端自动识别当前用户
        Long userId = SecurityUtils.getCurrentUserId();
        // 调用Service层查询用户详细信息
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 更新当前登录用户的个人信息
     *
     * 【HTTP方法】PUT
     * 【URL路径】/api/v1/users/profile
     *
     * 【参数说明】
     * @param body 请求体为Map<String, String>，包含可选的更新字段：
     *             - nickname: 新昵称
     *             - email: 新邮箱
     *             - phone: 新手机号
     *             - signature: 新个性签名
     *             使用Map而非专用DTO的原因：所有字段都是可选的，只有提供的字段才会被更新
     *             注意：更规范的做法是创建一个专用的UpdateProfileRequest DTO，并在字段上使用
     *             @JsonIgnoreProperties(ignoreUnknown = true)来忽略未知字段
     *
     * 【返回值】Result<Void> - 更新成功，不返回数据
     *
     * 【RESTful设计】
     * - 使用PUT方法：表示"更新/替换"资源的语义
     * - 路径与GET /profile相同，通过HTTP方法区分读（GET）和写（PUT）
     *   这是RESTful的"同一资源，不同方法"设计理念
     *
     * 【API设计最佳实践 - 部分更新 vs 全量更新】
     * - PUT通常表示全量更新（替换整个资源）
     * - PATCH表示部分更新（只更新提供的字段）
     * - 这里虽然使用PUT，但实际行为是部分更新（只更新非null字段），是一个常见的折中方案
     */
    @Operation(summary = "更新个人信息")
    @PutMapping("/profile") // 映射HTTP PUT请求到 /api/v1/users/profile
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 从Map中逐个提取字段，如果字段不存在则value为null，Service层会忽略null字段
        // body.get("nickname") - 如果请求体中没有nickname字段，返回null
        userService.updateProfile(
                userId,
                body.get("nickname"),   // 新昵称，可为null
                body.get("email"),      // 新邮箱，可为null
                body.get("phone"),      // 新手机号，可为null
                body.get("signature")   // 新个性签名，可为null
        );
        return Result.success();
    }

    /**
     * 修改当前登录用户的密码
     *
     * 【HTTP方法】PUT
     * 【URL路径】/api/v1/users/password
     *
     * 【参数说明】
     * @param request 修改密码请求DTO，包含：
     *                - oldPassword: 旧密码（用于验证身份，防止未授权修改）
     *                - newPassword: 新密码
     *                - confirmPassword: 确认新密码（需与newPassword一致）
     *                @Valid触发校验：确保所有密码字段不为空，新密码长度符合要求
     *
     * 【返回值】Result<Void> - 修改成功提示
     *
     * 【安全设计说明】
     * - 要求输入旧密码：防止用户离开电脑时被他人恶意修改密码
     * - 修改密码后通常需要使旧Token失效，强制用户重新登录
     * - 新密码需要满足复杂度要求（长度、大小写、特殊字符等）
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password") // 映射HTTP PUT请求到 /api/v1/users/password
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // Service层会：1.验证旧密码是否正确 2.加密新密码 3.更新数据库 4.可能清除用户的Token缓存
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取当前登录用户的偏好设置
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/users/settings
     *
     * 【参数说明】无需参数
     *
     * 【返回值】Result<UserSettings> - 用户偏好设置对象
     *           可能包含：主题（深色/浅色）、语言、每日提醒时间、通知开关等
     */
    @Operation(summary = "获取偏好设置")
    @GetMapping("/settings")
    public Result<UserSettings> getSettings() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getSettings(userId));
    }

    /**
     * 更新当前登录用户的偏好设置
     *
     * 【HTTP方法】PUT
     * 【URL路径】/api/v1/users/settings
     *
     * 【参数说明】
     * @param settings 用户偏好设置实体对象，包含所有设置字段
     *                 注意：这里直接使用Entity（UserSettings）而非DTO，是一个简化设计
     *                 更规范的做法是使用专门的SettingsUpdateRequest DTO，避免暴露实体内部字段
     *
     * 【返回值】Result<Void> - 更新成功
     */
    @Operation(summary = "更新偏好设置")
    @PutMapping("/settings")
    public Result<Void> updateSettings(@RequestBody UserSettings settings) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateSettings(userId, settings);
        return Result.success();
    }

    /**
     * 上传用户头像
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/users/avatar
     *
     * 【参数说明】
     * @param file 上传的头像文件（MultipartFile类型）
     *             @RequestParam("file") 从multipart/form-data请求中获取名为"file"的文件部分
     *             MultipartFile封装了文件内容、原始文件名、大小、ContentType等信息
     *
     * 【返回值】Result<String> - 返回头像文件的访问URL
     *
     * 【文件上传设计说明】
     * - Content-Type: 前端必须使用multipart/form-data格式提交（不是application/json）
     * - 文件校验：检查文件是否为空、文件大小、文件类型（只允许jpg/png等图片格式）
     * - 存储策略：文件上传到云存储（如OSS），数据库只保存文件的URL
     * - 安全考虑：限制文件大小（如最大2MB）、限制文件类型（防止上传可执行文件）
     *
     * 【操作步骤】
     * 1. 校验文件是否为空
     * 2. 将文件上传到对象存储，获取访问URL
     * 3. 更新用户表中的头像URL字段
     * 4. 返回新的头像URL给前端
     */
    @Operation(summary = "上传头像")
    @PostMapping("/avatar") // 映射HTTP POST请求到 /api/v1/users/avatar
    public Result<String> uploadAvatar(
            // @RequestParam("file"): 从multipart/form-data请求中获取名为"file"的文件
            // 前端表单中<input type="file" name="file">的name属性必须与这里的"file"一致
            @RequestParam("file") MultipartFile file) {
        // 校验上传文件是否为空（用户可能未选择文件就点击了上传）
        if (file.isEmpty()) {
            // 返回错误响应，HTTP状态码仍为200，通过code字段区分业务错误
            // 也有做法是抛出异常，由全局异常处理器返回400 Bad Request
            return Result.error("文件不能为空");
        }
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        // 步骤1：上传文件到对象存储服务（如阿里云OSS、腾讯云COS）
        // uploadFile方法内部会：生成唯一文件名、上传文件、返回文件的公网访问URL
        String avatarUrl = fileStorageService.uploadFile(file, "avatar");
        // "avatar"是存储路径前缀，最终URL形如：https://cdn.example.com/avatar/uuid.jpg

        // 步骤2：更新数据库中用户表的头像URL字段
        userService.updateAvatar(userId, avatarUrl);

        // 返回头像URL，前端可以立即用此URL显示新头像
        return Result.success("上传成功", avatarUrl);
    }

    /**
     * 导出当前用户的全量数据
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/users/export
     *
     * 【参数说明】无需参数
     *
     * 【返回值】Result<Map<String, Object>> - 包含用户所有模块的数据（计划、记账、目标、摘录等）
     *
     * 【设计说明】
     * - 使用POST方法：虽然不创建资源，但导出操作可能触发大量数据处理，
     *   POST比GET更适合处理可能产生大量响应的请求
     * - 数据可携带性：符合GDPR（通用数据保护条例）等隐私法规，用户有权获取自己的所有数据
     * - 返回Map而非具体DTO：因为导出的数据包含多个模块，结构灵活
     *
     * 【改进建议】
     * - 对于大量数据，可以考虑异步导出+下载链接的方式
     * - 支持选择导出格式（JSON、CSV、PDF等）
     * - 可以添加数据范围筛选（如只导出某个时间段的数据）
     */
    @Operation(summary = "导出全量数据")
    @PostMapping("/export")
    public Result<Map<String, Object>> exportData() {
        Long userId = SecurityUtils.getCurrentUserId();
        // Service层会聚合用户所有模块的数据：个人资料、计划、记账、目标、摘录、总结等
        return Result.success(userService.exportData(userId));
    }
}
