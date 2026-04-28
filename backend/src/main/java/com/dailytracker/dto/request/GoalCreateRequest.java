package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/更新目标请求 DTO
 */
@Data
public class GoalCreateRequest {

    /** 父目标ID */
    private Long parentId;

    /** 目标标题（必填） */
    @NotBlank(message = "目标标题不能为空")
    private String title;

    /** 目标描述 */
    private String description;

    /** 目标类型（必填）：FIVE_YEAR/YEARLY/MONTHLY/WEEKLY */
    @NotBlank(message = "目标类型不能为空")
    private String goalType;

    /** 分类：CAREER/STUDY/HEALTH/FINANCE/LIFE/OTHER */
    private String category = "OTHER";

    /** 开始日期（必填） */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /** 截止日期（必填） */
    @NotNull(message = "截止日期不能为空")
    private LocalDate endDate;

    /** 进度（0~100） */
    private Integer progress = 0;

    /** 状态：NOT_STARTED/IN_PROGRESS/COMPLETED/ABANDONED */
    private String status = "NOT_STARTED";

    /** 优先级 */
    private Integer priority = 0;

    /** 排序 */
    private Integer sortOrder = 0;
}
