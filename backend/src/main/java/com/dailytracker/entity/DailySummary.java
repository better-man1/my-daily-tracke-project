package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 每日总结实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_daily_summary")
public class DailySummary extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 总结日期 */
    private LocalDate summaryDate;

    /** 心情(1~5) */
    private Integer mood;

    /** 今日评分(1~10) */
    private Integer score;

    /** 今日成就 */
    private String achievement;

    /** 今日不足 */
    private String improvement;

    /** 明日计划 */
    private String tomorrowPlan;

    /** 感恩记录（JSON数组） */
    private String gratitude;

    /** 健康记录 */
    private String healthNote;

    /** 自由日记 */
    private String freeWriting;

    /** 标签（JSON数组） */
    private String tags;
}
