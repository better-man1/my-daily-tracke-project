package com.dailytracker.service;

import java.util.Map;

/**
 * 数据看板服务接口
 */
public interface DashboardService {

    /** 今日概览数据 */
    Map<String, Object> getToday();

    /** 本周统计数据 */
    Map<String, Object> getWeek();

    /** 本月统计数据 */
    Map<String, Object> getMonth();

    /** 年度统计数据 */
    Map<String, Object> getYear();

    /**
     * 趋势分析（自定义时间范围）
     * @param startDate 开始日期（必填）
     * @param endDate   结束日期（必填）
     * @param type      统计类型： plan / accounting / excerpt / summary（null=全部）
     */
    Map<String, Object> getTrend(java.time.LocalDate startDate, java.time.LocalDate endDate, String type);
}
