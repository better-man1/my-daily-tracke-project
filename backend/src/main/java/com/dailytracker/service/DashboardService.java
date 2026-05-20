package com.dailytracker.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 数据看板服务接口（Dashboard Service Interface）
 *
 * 【职责说明】
 * 本接口负责系统首页数据看板的聚合展示，从多个业务模块中提取统计数据，
 * 包括：计划完成情况、记账收支、摘录数量、总结状态、目标进度等。
 * 是一个典型的"聚合服务"（Aggregation Service），它不直接管理数据，
 * 而是从其他服务和数据源中汇总信息。
 *
 * 【设计模式】
 * - 门面模式（Facade Pattern）的变体：
 *   DashboardService 作为统一入口，对外提供简洁的聚合接口，
 *   内部协调多个 Mapper/Service 来组装数据，降低了前端的调用复杂度。
 *   Controller 只需调用一个方法即可获取完整的看板数据。
 *
 * - 只读服务：
 *   该接口的所有方法均为查询操作，不涉及数据修改，因此不需要事务管理。
 *
 * 【返回值设计】
 * 所有方法均返回 Map<String, Object>，这是一种灵活但类型安全性较弱的设计。
 * 适用于快速迭代的聚合数据场景。在更严格的架构中，可以定义专门的 DTO 类。
 */
public interface DashboardService {

    /**
     * 获取今日概览数据
     *
     * 【返回数据包含】
     * - plan: 今日计划完成率（total/done/completionRate）
     * - accounting: 今日收支（income/expense/balance）
     * - excerptCount: 今日摘录数量
     * - summary: 今日总结状态（hasSummary/mood/score）
     * - goal: 进行中目标信息（activeCount/avgProgress）
     * - date: 当前日期
     *
     * @return 今日概览数据 Map
     */
    Map<String, Object> getToday();

    /**
     * 获取本周统计数据
     *
     * 【返回数据包含】
     * - weekStart/weekEnd: 本周起止日期
     * - planByDate: 每日计划数量（按日期分组）
     * - donePlanByDate: 每日已完成计划数量（按日期分组）
     * - weekIncome/weekExpense: 本周收支汇总
     * - weekExcerpts: 本周摘录总数
     *
     * @return 本周统计数据 Map
     */
    Map<String, Object> getWeek();

    /**
     * 获取本月统计数据
     *
     * 【返回数据包含】
     * - monthStart/monthEnd: 本月起止日期
     * - monthPlanTotal/monthPlanDone: 本月计划总数与已完成数
     * - monthCompletionRate: 本月计划完成率
     * - monthIncome/monthExpense/monthBalance: 本月收支
     * - summaryDays: 本月写总结的天数
     * - avgMood: 本月平均心情指数
     *
     * @return 本月统计数据 Map
     */
    Map<String, Object> getMonth();

    /**
     * 获取年度统计数据
     *
     * 【返回数据包含】
     * - year: 年份
     * - yearIncome/yearExpense/yearBalance: 年度收支汇总
     * - yearlyGoalTotal/yearlyGoalDone: 年度目标总数与已完成数
     * - summaryDays: 年度总结打卡天数
     * - excerptCount: 年度摘录总数
     *
     * @return 年度统计数据 Map
     */
    Map<String, Object> getYear();

    /**
     * 获取自定义时间段的趋势分析数据
     *
     * 【返回数据包含】
     * - planTrend: 计划完成趋势（按日期分组，含 total/done）
     * - financeTrend: 收支趋势（按日期分组，含 income/expense）
     * - excerptTrend: 摘录数量趋势（按日期分组）
     *
     * 【设计说明】
     * 该方法支持自定义时间范围查询，为前端图表提供数据源。
     * 趋势数据使用 Map<LocalDate, Map<String, Object>> 的嵌套结构，
     * 便于前端按日期维度渲染折线图、柱状图等可视化组件。
     *
     * @param startDate 开始日期（含）
     * @param endDate   结束日期（含）
     * @return 趋势分析数据 Map
     */
    Map<String, Object> getTrend(LocalDate startDate, LocalDate endDate);
}
