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
 * 数据看板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DailyPlanMapper dailyPlanMapper;
    private final AccountingMapper accountingMapper;
    private final ExcerptMapper excerptMapper;
    private final DailySummaryMapper summaryMapper;
    private final GoalPlanMapper goalPlanMapper;

    @Override
    public Map<String, Object> getToday() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = DateUtils.today();
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 计划完成率
        List<DailyPlan> todayPlans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, today)
                        .eq(DailyPlan::getIsTemplate, 0));
        long totalPlans = todayPlans.size();
        long donePlans = todayPlans.stream().filter(p -> "DONE".equals(p.getStatus())).count();
        Map<String, Object> planStats = new LinkedHashMap<>();
        planStats.put("total", totalPlans);
        planStats.put("done", donePlans);
        planStats.put("completionRate", totalPlans == 0 ? 0 : Math.round(donePlans * 100.0 / totalPlans));
        result.put("plan", planStats);

        // 2. 记账汇总
        List<Accounting> todayAccounting = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .eq(Accounting::getAccountingDate, today));
        BigDecimal income = todayAccounting.stream()
                .filter(a -> "INCOME".equals(a.getType()))
                .map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = todayAccounting.stream()
                .filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> accountingStats = new LinkedHashMap<>();
        accountingStats.put("income", income);
        accountingStats.put("expense", expense);
        accountingStats.put("balance", income.subtract(expense));
        result.put("accounting", accountingStats);

        // 3. 摘录数量
        long excerptCount = excerptMapper.selectCount(
                new LambdaQueryWrapper<Excerpt>()
                        .eq(Excerpt::getUserId, userId)
                        .eq(Excerpt::getExcerptDate, today));
        result.put("excerptCount", excerptCount);

        // 4. 总结状态
        DailySummary todaySummary = summaryMapper.selectOne(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .eq(DailySummary::getSummaryDate, today));
        Map<String, Object> summaryStats = new LinkedHashMap<>();
        summaryStats.put("hasSummary", todaySummary != null);
        summaryStats.put("mood", todaySummary != null ? todaySummary.getMood() : null);
        summaryStats.put("score", todaySummary != null ? todaySummary.getScore() : null);
        result.put("summary", summaryStats);

        // 5. 目标进度（进行中的目标）
        List<GoalPlan> activeGoals = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getUserId, userId)
                        .eq(GoalPlan::getStatus, "IN_PROGRESS"));
        OptionalDouble avgGoalProgress = activeGoals.stream()
                .mapToInt(GoalPlan::getProgress).average();
        Map<String, Object> goalStats = new LinkedHashMap<>();
        goalStats.put("activeCount", activeGoals.size());
        goalStats.put("avgProgress", avgGoalProgress.isPresent() ? Math.round(avgGoalProgress.getAsDouble()) : 0);
        result.put("goal", goalStats);

        result.put("date", today);
        return result;
    }

    @Override
    public Map<String, Object> getWeek() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate weekStart = DateUtils.weekStart();
        LocalDate weekEnd = DateUtils.weekEnd();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekStart", weekStart);
        result.put("weekEnd", weekEnd);

        // 本周每日计划完成趋势（按日分组）
        List<DailyPlan> weekPlans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .ge(DailyPlan::getPlanDate, weekStart)
                        .le(DailyPlan::getPlanDate, weekEnd)
                        .eq(DailyPlan::getIsTemplate, 0));

        Map<LocalDate, Long> planByDate = weekPlans.stream()
                .collect(Collectors.groupingBy(DailyPlan::getPlanDate, Collectors.counting()));
        Map<LocalDate, Long> donePlanByDate = weekPlans.stream()
                .filter(p -> "DONE".equals(p.getStatus()))
                .collect(Collectors.groupingBy(DailyPlan::getPlanDate, Collectors.counting()));
        result.put("planByDate", planByDate);
        result.put("donePlanByDate", donePlanByDate);

        // 本周收支
        List<Accounting> weekAcc = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, weekStart)
                        .le(Accounting::getAccountingDate, weekEnd));
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

        // 本月总结天数 & 情绪趋势
        List<DailySummary> monthSummaries = summaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .ge(DailySummary::getSummaryDate, monthStart)
                        .le(DailySummary::getSummaryDate, monthEnd));
        result.put("summaryDays", monthSummaries.size());
        OptionalDouble avgMood = monthSummaries.stream().mapToInt(DailySummary::getMood).average();
        result.put("avgMood", avgMood.isPresent() ? String.format("%.1f", avgMood.getAsDouble()) : null);

        return result;
    }

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

        // 年度目标完成情况
        List<GoalPlan> yearGoals = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getUserId, userId)
                        .eq(GoalPlan::getGoalType, "YEARLY"));
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
