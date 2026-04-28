package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 目标关键结果实体（不含软删除，独立表结构）
 */
@Data
@TableName("t_goal_kr")
public class GoalKr implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标ID */
    private Long goalId;

    /** 用户ID */
    private Long userId;

    /** KR标题 */
    private String title;

    /** 目标值 */
    private BigDecimal targetValue;

    /** 当前值 */
    private BigDecimal currentValue;

    /** 单位 */
    private String unit;

    /** 进度(0~100) */
    private Integer progress;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
