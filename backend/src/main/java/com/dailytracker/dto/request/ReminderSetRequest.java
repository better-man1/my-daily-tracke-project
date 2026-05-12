package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设置提醒请求 DTO
 */
@Data
public class ReminderSetRequest {

    /** 提醒类型：START/DUE/CUSTOM */
    @NotNull(message = "提醒类型不能为空")
    private String reminderType;

    /** 提醒时间 */
    private LocalDateTime reminderTime;

    /** 提前多少分钟提醒（当reminderType为START或DUE时使用） */
    private Integer advanceMinutes;
}
