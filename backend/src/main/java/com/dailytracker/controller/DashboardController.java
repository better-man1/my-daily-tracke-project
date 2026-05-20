package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// DashboardService: 数据看板业务逻辑层接口，负责聚合多个模块的统计数据
import com.dailytracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
// @DateTimeFormat: Spring日期格式化注解，将字符串类型的日期参数自动转换为LocalDate对象
// iso = DateTimeFormat.ISO.DATE 表示使用ISO 8601日期格式（yyyy-MM-dd）
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 数据看板控制器（DashboardController）
 *
 * 【职责说明】
 * 本控制器负责提供数据看板（Dashboard）的聚合统计API，将多个业务模块的数据汇总展示，包括：
 *   1. 今日概览数据（今天的任务完成情况、记账总额、情绪等）
 *   2. 本周统计数据（本周任务完成率、收支汇总等）
 *   3. 本月统计数据（本月各项指标的汇总）
 *   4. 年度统计数据（全年数据汇总）
 *   5. 自定义时间范围的趋势分析
 *
 * 【RESTful设计思想】
 * - 只读资源：Dashboard是只读的聚合视图，不涉及数据修改，因此全部使用GET方法
 * - 时间维度作为路径而非参数：/today、/week、/month、/year 是预定义的时间维度
 *   这种设计比 /dashboard?period=today 更直观，也更符合REST的路径语义
 * - 自定义范围使用查询参数：/trend?startDate=...&endDate=...，因为参数是动态的
 *
 * 【聚合模式说明】
 * - Dashboard通常需要从多个Service中聚合数据（如DailyPlanService、AccountingService等）
 * - 返回Map<String, Object>：因为聚合数据结构灵活，字段随需求变化
 *   更规范的做法是创建DashboardResponse DTO类，定义明确的返回字段
 *
 * 【API路径前缀】/api/v1/dashboard
 */
@Tag(name = "数据看板", description = "多维度数据统计聚合接口")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取今日概览数据
     *
     * 【HTTP方法】GET（只读查询）
     * 【URL路径】/api/v1/dashboard/today
     *
     * 【参数说明】无需参数，自动获取当天的数据
     *
     * 【返回值】Result<Map<String, Object>> - 今日聚合数据
     *           可能包含的字段：
     *           - totalTasks: 今日任务总数
     *           - completedTasks: 已完成任务数
     *           - completionRate: 完成率（百分比）
     *           - totalIncome: 今日收入总额
     *           - totalExpense: 今日支出总额
     *           - mood: 今日心情评分
     *           - streak: 连续记录天数
     *
     * 【设计说明】
     * - 使用独立路径/today而非查询参数，更简洁直观
     * - 所有统计数据的聚合逻辑封装在DashboardService中
     *   Controller层只负责"调用Service + 返回结果"，保持Controller轻量
     */
    @Operation(summary = "今日概览数据")
    @GetMapping("/today") // 映射HTTP GET请求到 /api/v1/dashboard/today
    public Result<Map<String, Object>> getToday() {
        // 直接调用Service获取今日聚合数据，Controller层不做任何业务逻辑处理
        return Result.success(dashboardService.getToday());
    }

    /**
     * 获取本周统计数据
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/dashboard/week
     *
     * 【参数说明】无需参数，自动获取本周一至本周日的数据
     *
     * 【返回值】Result<Map<String, Object>> - 本周聚合统计
     *           可能包含：周任务完成率趋势、周收支汇总、周目标进度等
     */
    @Operation(summary = "本周统计数据")
    @GetMapping("/week")
    public Result<Map<String, Object>> getWeek() {
        return Result.success(dashboardService.getWeek());
    }

    /**
     * 获取本月统计数据
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/dashboard/month
     *
     * 【参数说明】无需参数，自动获取当月1日至月末的数据
     *
     * 【返回值】Result<Map<String, Object>> - 本月聚合统计
     */
    @Operation(summary = "本月统计数据")
    @GetMapping("/month")
    public Result<Map<String, Object>> getMonth() {
        return Result.success(dashboardService.getMonth());
    }

    /**
     * 获取年度统计数据
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/dashboard/year
     *
     * 【参数说明】无需参数，自动获取当年1月1日至12月31日的数据
     *
     * 【返回值】Result<Map<String, Object>> - 年度聚合统计
     *           可能包含：月度趋势、年度目标完成情况、年度收支总览等
     */
    @Operation(summary = "年度统计数据")
    @GetMapping("/year")
    public Result<Map<String, Object>> getYear() {
        return Result.success(dashboardService.getYear());
    }

    /**
     * 趋势分析（支持自定义时间范围）
     *
     * 【HTTP方法】GET
     * 【URL路径】/api/v1/dashboard/trend
     *
     * 【参数说明】
     * @param startDate 开始日期，必填，格式：yyyy-MM-dd（如2024-01-01）
     *                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 自动将字符串转为LocalDate
     *                  @RequestParam 标记为必填参数（不设置required=false时默认为必填）
     * @param endDate   结束日期，必填，格式同上
     *
     * 【返回值】Result<Map<String, Object>> - 指定时间范围的趋势数据
     *           可能包含：每日/每周的数据变化趋势，用于绘制折线图等
     *
     * 【参数校验说明】
     * - @RequestParam 默认required=true（必填），如果客户端未提供会返回400错误
     * - @DateTimeFormat 自动将"2024-01-15"字符串转为LocalDate对象
     *   如果格式不匹配，Spring会抛出TypeMismatchException，返回400错误
     * - 更严格的做法：在Service层校验startDate不能晚于endDate、时间跨度不能超过1年等
     *
     * 【GET请求中传递日期参数的方式】
     * 方式1（本接口使用）：查询参数 /trend?startDate=2024-01-01&endDate=2024-01-31
     * 方式2：路径参数 /trend/2024-01-01/2024-01-31（需要修改@RequestMapping）
     * 方式1更灵活，更符合GET请求的惯例
     */
    @Operation(summary = "趋势分析（支持自定义时间范围）")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(
            // @RequestParam: 从URL查询参数中获取值，如 ?startDate=2024-01-01
            // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE): 将"2024-01-01"自动转换为LocalDate对象
            // 不设置required=false，所以startDate是必填参数
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dashboardService.getTrend(startDate, endDate));
    }
}
