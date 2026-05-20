package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.entity.*;
import com.dailytracker.mapper.*;
import com.dailytracker.service.DashboardService;
import com.dailytracker.util.DateUtils;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据看板服务实现类（Dashboard Service Implementation）
 *
 * 【类设计说明】
 * 本类是 DashboardService 接口的具体实现，负责从多个数据源（Mapper）中聚合统计数据，
 * 为前端首页看板提供一体化的数据接口。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成日志记录器
 * @Service    - Spring 注解，标记为业务层组件
 * @RequiredArgsConstructor - Lombok 注解，自动生成构造器注入
 *
 * 【设计特点】
 * - 聚合服务（Aggregation Service）：不直接管理数据，而是从多个 Mapper 中汇总信息
 * - 只读服务：所有方法均为查询操作，不需要事务管理
 * - 使用 Java 8 Stream API 进行内存中的数据聚合和统计计算
 *
 * 【依赖注入的字段说明】
 * 注入了 5 个 Mapper，分别对应 5 个业务模块的数据源：
 * - dailyPlanMapper: 每日计划数据
 * - accountingMapper: 记账数据
 * - excerptMapper: 摘录数据
 * - summaryMapper: 每日总结数据
 * - goalPlanMapper: 目标计划数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 每日计划数据访问层 */
    private final DailyPlanMapper dailyPlanMapper;
    /** 记账数据访问层 */
    private final AccountingMapper accountingMapper;
    /** 摘录数据访问层 */
    private final ExcerptMapper excerptMapper;
    /** 每日总结数据访问层 */
    private final DailySummaryMapper summaryMapper;
    /** 目标计划数据访问层 */
    private final GoalPlanMapper goalPlanMapper;

    /**
     * 获取今日概览数据
     *
     * 【数据聚合逻辑】
     * 从 5 个不同的数据源分别查询今日相关数据，组装为一个完整的概览视图：
     * 1. 计划完成率：今日任务总数 / 已完成数
     * 2. 记账汇总：今日收入 / 支出 / 余额
     * 3. 摘录数量：今日新增摘录数
     * 4. 总结状态：今日是否已写总结，心情和评分
     * 5. 目标进度：进行中目标的数量和平均进度
     *
     * @return 今日概览数据 Map
     */
    @Override
    public Map<String, Object> getToday() {
        // 从 Spring Security 上下文中获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = DateUtils.today(); // 获取今天的日期（统一通过工具类获取，便于测试）
        Map<String, Object> result = new LinkedHashMap<>(); // 使用 LinkedHashMap 保持插入顺序

        // 1. 计划完成率统计
        // 查询今日所有非模板任务（isTemplate=0 排除模板任务）
        List<DailyPlan> todayPlans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, today)
                        .eq(DailyPlan::getIsTemplate, 0));
        long totalPlans = todayPlans.size();
        // 使用 Stream API 统计已完成的任务数量（状态为 "DONE"）
        long donePlans = todayPlans.stream().filter(p -> "DONE".equals(p.getStatus())).count();
        Map<String, Object> planStats = new LinkedHashMap<>();
        planStats.put("total", totalPlans);
        planStats.put("done", donePlans);
        planStats.put("completionRate", totalPlans == 0 ? 0 : Math.round(donePlans * 100.0 / totalPlans)); // 计算百分比（四舍五入）
        result.put("plan", planStats);

        // 2. 记账汇总
        // 查询今日所有记账记录
        List<Accounting> todayAccounting = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .eq(Accounting::getAccountingDate, today));
        // 使用 Stream API + BigDecimal 精确计算收入总额（避免浮点数精度问题）
        BigDecimal income = todayAccounting.stream()
                .filter(a -> "INCOME".equals(a.getType())) // 过滤收入类型
                .map(Accounting::getAmount)                 // 提取金额
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // 累加求和
        // 计算支出总额
        BigDecimal expense = todayAccounting.stream()
                .filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> accountingStats = new LinkedHashMap<>();
        accountingStats.put("income", income);
        accountingStats.put("expense", expense);
        accountingStats.put("balance", income.subtract(expense));
        result.put("accounting", accountingStats);

        // 3. 今日摘录数量
        long excerptCount = excerptMapper.selectCount(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .eq(Excerpt::getExcerptDate, today));
        result.put("excerptCount", excerptCount);

        // 4. 今日总结状态
        DailySummary todaySummary = summaryMapper.selectOne(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .eq(DailySummary::getSummaryDate, today));
        Map<String, Object> summaryStats = new LinkedHashMap<>();
        summaryStats.put("hasSummary", todaySummary != null);
        summaryStats.put("mood", todaySummary != null ? todaySummary.getMood() : null);
        summaryStats.put("score", todaySummary != null ? todaySummary.getScore() : null);
        result.put("summary", summaryStats);

        // 5. 进行中目标的进度统计
        List<GoalPlan> activeGoals = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getUserId, userId)
                        .eq(GoalPlan::getStatus, "IN_PROGRESS")); // 只查询进行中的目标
        OptionalDouble avgGoalProgress = activeGoals.stream()
                .mapToInt(GoalPlan::getProgress).average();
        Map<String, Object> goalStats = new LinkedHashMap<>();
        goalStats.put("activeCount", activeGoals.size());
        goalStats.put("avgProgress", avgGoalProgress.isPresent() ? Math.round(avgGoalProgress.getAsDouble()) : 0);
        result.put("goal", goalStats);

        result.put("date", today);
        return result;
    }

    /**
     * 获取本周统计数据
     *
     * 【聚合逻辑】
     * 查询本周（周一到周日）的各项统计数据，支持按日期分组展示趋势。
     *
     * @return 本周统计数据 Map
     */
    @Override
    public Map<String, Object> getWeek() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate weekStart = DateUtils.weekStart(); // 本周起始日期（周一）
        LocalDate weekEnd = DateUtils.weekEnd();     // 本周结束日期（周日）
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekStart", weekStart);
        result.put("weekEnd", weekEnd);

        // 本周每日计划完成趋势（按日期分组统计）
        // ge = greater than or equal, le = less than or equal
        List<DailyPlan> weekPlans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .ge(DailyPlan::getPlanDate, weekStart)   // 大于等于本周开始日期
                        .le(DailyPlan::getPlanDate, weekEnd)     // 小于等于本周结束日期
                        .eq(DailyPlan::getIsTemplate, 0));

        // 使用 Stream groupingBy 按日期分组统计每日任务数量
        Map<LocalDate, Long> planByDate = weekPlans.stream()
                .collect(Collectors.groupingBy(DailyPlan::getPlanDate, Collectors.counting()));
        // 按日期分组统计每日已完成任务数量
        Map<LocalDate, Long> donePlanByDate = weekPlans.stream()
                .filter(p -> "DONE".equals(p.getStatus()))
                .collect(Collectors.groupingBy(DailyPlan::getPlanDate, Collectors.counting()));
        result.put("planByDate", planByDate);
        result.put("donePlanByDate", donePlanByDate);

        // 本周收支汇总
        List<Accounting> weekAcc = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, weekStart)
                        .le(Accounting::getAccountingDate, weekEnd));
        // 精确计算本周收入总额
        BigDecimal weekIncome = weekAcc.stream().filter(a -> "INCOME".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weekExpense = weekAcc.stream().filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("weekIncome", weekIncome);
        result.put("weekExpense", weekExpense);

        // 本周摘录数
        long weekExcerpts = excerptMapper.selectCount(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .ge(Excerpt::getExcerptDate, weekStart)
                        .le(Excerpt::getExcerptDate, weekEnd));
        result.put("weekExcerpts", weekExcerpts);

        return result;
    }

    /**
     * 获取本月统计数据
     *
     * 包含月度计划统计、收支汇总、总结天数和平均心情指数。
     */
    @Override
    public Map<String, Object> getMonth() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate monthStart = DateUtils.monthStart();
        LocalDate monthEnd = DateUtils.monthEnd();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("monthStart", monthStart);
        result.put("monthEnd", monthEnd);

        // 本月计划统计
        List<DailyPlan> monthPlans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .ge(DailyPlan::getPlanDate, monthStart)
                        .le(DailyPlan::getPlanDate, monthEnd)
                        .eq(DailyPlan::getIsTemplate, 0));
        long monthTotal = monthPlans.size();
        long monthDone = monthPlans.stream().filter(p -> "DONE".equals(p.getStatus())).count();
        result.put("monthPlanTotal", monthTotal);
        result.put("monthPlanDone", monthDone);
        result.put("monthCompletionRate", monthTotal == 0 ? 0 : Math.round(monthDone * 100.0 / monthTotal));

        // 本月收支
        List<Accounting> monthAcc = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, monthStart)
                        .le(Accounting::getAccountingDate, monthEnd));
        BigDecimal monthIncome = monthAcc.stream().filter(a -> "INCOME".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthExpense = monthAcc.stream().filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("monthIncome", monthIncome);
        result.put("monthExpense", monthExpense);
        result.put("monthBalance", monthIncome.subtract(monthExpense));

        // 本月总结天数 & 平均心情指数
        List<DailySummary> monthSummaries = summaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .ge(DailySummary::getSummaryDate, monthStart)
                        .le(DailySummary::getSummaryDate, monthEnd));
        result.put("summaryDays", monthSummaries.size());
        // 使用 OptionalDouble 安全地计算平均心情值（防止空集合的除零错误）
        OptionalDouble avgMood = monthSummaries.stream().mapToInt(DailySummary::getMood).average();
        result.put("avgMood", avgMood.isPresent() ? String.format("%.1f", avgMood.getAsDouble()) : null);

        return result;
    }

    /**
     * 获取年度统计数据
     *
     * 包含年度收支汇总、年度目标完成情况、总结打卡天数和摘录总数。
     */
    @Override
    public Map<String, Object> getYear() {
        Long userId = SecurityUtils.getCurrentUserId();
        int year = DateUtils.today().getYear();
        LocalDate yearStart = DateUtils.yearStart();
        LocalDate yearEnd = DateUtils.yearEnd();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);

        // 年度收支（按月分组）
        List<Accounting> yearAcc = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, yearStart)
                        .le(Accounting::getAccountingDate, yearEnd));
        BigDecimal yearIncome = yearAcc.stream().filter(a -> "INCOME".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yearExpense = yearAcc.stream().filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("yearIncome", yearIncome);
        result.put("yearExpense", yearExpense);
        result.put("yearBalance", yearIncome.subtract(yearExpense));

        // 年度目标完成情况（只统计年度类型的目标）
        List<GoalPlan> yearGoals = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getUserId, userId)
                        .eq(GoalPlan::getGoalType, "YEARLY")); // 筛选年度目标
        result.put("yearlyGoalTotal", yearGoals.size());
        result.put("yearlyGoalDone", yearGoals.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count());

        // 年度总结打卡天数
        long summaryDays = summaryMapper.selectCount(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .ge(DailySummary::getSummaryDate, yearStart)
                        .le(DailySummary::getSummaryDate, yearEnd));
        result.put("summaryDays", summaryDays);

        // 年度摘录数量
        long excerptCount = excerptMapper.selectCount(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .ge(Excerpt::getExcerptDate, yearStart)
                        .le(Excerpt::getExcerptDate, yearEnd));
        result.put("excerptCount", excerptCount);

        return result;
    }

    /**
     * 获取自定义时间段的趋势分析数据
     *
     * 【聚合维度】
     * 1. 计划完成趋势：按日期分组的任务总数和完成数
     * 2. 收支趋势：按日期分组的收入和支出
     * 3. 摘录趋势：按日期分组的摘录数量
     *
     * 使用 Collectors.groupingBy + Collectors.collectingAndThen 实现复杂的嵌套聚合。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势分析数据
     */
    @Override
    public Map<String, Object> getTrend(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. 计划完成趋势
        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .ge(DailyPlan::getPlanDate, startDate)
                        .le(DailyPlan::getPlanDate, endDate)
                        .eq(DailyPlan::getIsTemplate, 0));
        
        Map<LocalDate, Map<String, Long>> planTrend = plans.stream()
                .collect(Collectors.groupingBy(DailyPlan::getPlanDate, Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, Long> m = new HashMap<>();
                            m.put("total", (long) list.size());
                            m.put("done", list.stream().filter(p -> "DONE".equals(p.getStatus())).count());
                            return m;
                        }
                )));
        result.put("planTrend", planTrend);

        // 2. 收支趋势
        List<Accounting> accs = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, startDate)
                        .le(Accounting::getAccountingDate, endDate));
        
        Map<LocalDate, Map<String, BigDecimal>> financeTrend = accs.stream()
                .collect(Collectors.groupingBy(Accounting::getAccountingDate, Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Map<String, BigDecimal> m = new HashMap<>();
                            m.put("income", list.stream().filter(a -> "INCOME".equals(a.getType())).map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            m.put("expense", list.stream().filter(a -> "EXPENSE".equals(a.getType())).map(Accounting::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
                            return m;
                        }
                )));
        result.put("financeTrend", financeTrend);

        // 3. 摘录趋势
        List<Excerpt> excerpts = excerptMapper.selectList(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .ge(Excerpt::getExcerptDate, startDate)
                        .le(Excerpt::getExcerptDate, endDate));
        
        Map<LocalDate, Long> excerptTrend = excerpts.stream()
                .collect(Collectors.groupingBy(Excerpt::getExcerptDate, Collectors.counting()));
        result.put("excerptTrend", excerptTrend);

        return result;
    }
}
