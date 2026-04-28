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
}
