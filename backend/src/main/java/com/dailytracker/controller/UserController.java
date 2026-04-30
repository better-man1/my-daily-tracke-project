package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.ChangePasswordRequest;
import com.dailytracker.dto.response.UserProfileResponse;
import com.dailytracker.entity.UserSettings;
import com.dailytracker.service.UserService;
import com.dailytracker.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.dailytracker.service.FileStorageService;

import java.util.Map;

/**
 * 用户接口
 */
@Tag(name = "用户管理", description = "个人信息、密码、偏好设置")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<UserProfileResponse> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getProfile(userId));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(
                userId,
                body.get("nickname"),
                body.get("email"),
                body.get("phone"),
                body.get("signature")
        );
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return Result.success("密码修改成功", null);
    }

    @Operation(summary = "获取偏好设置")
    @GetMapping("/settings")
    public Result<UserSettings> getSettings() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getSettings(userId));
    }

    @Operation(summary = "更新偏好设置")
    @PutMapping("/settings")
    public Result<Void> updateSettings(@RequestBody UserSettings settings) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateSettings(userId, settings);
        return Result.success();
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 1. 上传文件到对象存储
        String avatarUrl = fileStorageService.uploadFile(file, "avatar");
        
        // 2. 更新用户表的头像URL
        userService.updateAvatar(userId, avatarUrl);
        
        return Result.success("上传成功", avatarUrl);
    }

    @Operation(summary = "导出全量数据")
    @PostMapping("/export")
    public Result<Map<String, Object>> exportData() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.exportData(userId));
    }
}
