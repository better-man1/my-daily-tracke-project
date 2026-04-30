package com.dailytracker.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "微信登录请求")
public class WxLoginRequest {

    @NotBlank(message = "code不能为空")
    @Schema(description = "微信登录凭证code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
