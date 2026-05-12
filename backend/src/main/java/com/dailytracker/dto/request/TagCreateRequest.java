package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建标签请求 DTO
 */
@Data
public class TagCreateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String name;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "颜色格式错误，请使用十六进制颜色代码，如 #6366f1")
    private String color;
}
