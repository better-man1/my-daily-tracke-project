package com.dailytracker.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建/更新每日总结请求 DTO
 */
@Data
public class SummaryCreateRequest {

    /** 总结日期（必填） */
    @NotNull(message = "总结日期不能为空")
    private LocalDate summaryDate;

    /** 心情（1~5） */
    @Min(value = 1, message = "心情评分最小为1")
    @Max(value = 5, message = "心情评分最大为5")
    private Integer mood = 3;

    /** 今日评分（1~10） */
    @Min(value = 1, message = "今日评分最小为1")
    @Max(value = 10, message = "今日评分最大为10")
    private Integer score = 5;

    /** 今日成就 */
    private String achievement;

    /** 今日不足 */
    private String improvement;

    /** 明日计划 */
    private String tomorrowPlan;

    /** 感恩记录（字符串列表） */
    private List<String> gratitude;

    /** 健康记录 */
    private String healthNote;

    /** 自由日记 */
    private String freeWriting;

    /** 标签列表 */
    private List<String> tags;
}
