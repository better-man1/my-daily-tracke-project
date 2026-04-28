package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 目标计划实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_goal_plan")
public class GoalPlan extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 父目标ID */
    private Long parentId;

    /** 目标标题 */
    private String title;

    /** 目标描述 */
    private String description;

    /** 目标类型：FIVE_YEAR/YEARLY/MONTHLY/WEEKLY */
    private String goalType;

    /** 分类：CAREER/STUDY/HEALTH/FINANCE/LIFE/OTHER */
    private String category;

    /** 开始日期 */
    private LocalDate startDate;

    /** 截止日期 */
    private LocalDate endDate;

    /** 进度(0~100) */
    private Integer progress;

    /** 状态：NOT_STARTED/IN_PROGRESS/COMPLETED/ABANDONED */
    private String status;

    /** 优先级 */
    private Integer priority;

    /** 排序 */
    private Integer sortOrder;
}
