package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
