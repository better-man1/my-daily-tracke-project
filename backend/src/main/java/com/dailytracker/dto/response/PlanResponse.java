package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 每日计划响应 DTO
 */
@Data
public class PlanResponse {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 关联目标ID */
    private Long goalId;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 计划日期 */
    private LocalDate planDate;

    /** 优先级：P0/P1/P2/P3 */
    private String priority;

    /** 分类：WORK/STUDY/LIFE/HEALTH */
    private String category;

    /** 预估时间（分钟） */
    private Integer estimatedMins;

    /** 实际时间（分钟） */
    private Integer actualMins;

    /** 状态：TODO/IN_PROGRESS/DONE/CANCELLED */
    private String status;

    /** 排序权重 */
    private Integer sortOrder;

    /** 完成时间 */
    private LocalDateTime completedAt;

    /** 是否为模板 */
    private Integer isTemplate;

    /** 模板名称 */
    private String templateName;

    /** 重复类型：NONE/DAILY/WEEKLY/MONTHLY/CUSTOM */
    private String repeatType;

    /** 重复模式（JSON格式） */
    private String repeatPattern;

    /** 重复结束日期 */
    private LocalDate repeatEndDate;

    /** 父任务ID */
    private Long parentId;

    /** 子任务数量 */
    private Integer subtaskCount;

    /** 已完成子任务数量 */
    private Integer completedSubtaskCount;

    /** 开始时间 */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /** 结束时间 */
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /** 是否为时间块 */
    private Integer isTimeblock;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
