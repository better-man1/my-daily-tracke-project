package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/更新每日计划请求 DTO
 */
@Data
public class PlanCreateRequest {

    /** 任务标题（必填） */
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 200, message = "任务标题不能超过200字符")
    private String title;

    /** 任务描述 */
    private String description;

    /** 计划日期（必填，格式：yyyy-MM-dd） */
    @NotNull(message = "计划日期不能为空")
    private LocalDate planDate;

    /** 优先级：P0/P1/P2/P3，默认P2 */
    @Pattern(regexp = "P[0-3]", message = "优先级格式错误，应为P0/P1/P2/P3")
    private String priority = "P2";

    /** 分类：WORK/STUDY/LIFE/HEALTH，默认WORK */
    @Pattern(regexp = "WORK|STUDY|LIFE|HEALTH", message = "分类格式错误")
    private String category = "WORK";

    /** 预估时间（分钟） */
    private Integer estimatedMins;

    /** 实际时间（分钟） */
    private Integer actualMins;

    /** 关联目标ID */
    private Long goalId;

    /** 排序权重 */
    private Integer sortOrder = 0;

    /** 开始时间 (格式 HH:mm) */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private java.time.LocalTime startTime;

    /** 结束时间 (格式 HH:mm) */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private java.time.LocalTime endTime;

    /** 是否为时间块 (1:是, 0:否) */
    private Integer isTimeblock = 0;
}
