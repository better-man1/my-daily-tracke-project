package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// SummaryCreateRequest: 创建/更新总结请求DTO，包含日期、心情、内容、反思等字段
import com.dailytracker.dto.request.SummaryCreateRequest;
// SummaryResponse: 总结响应DTO，包含总结的完整信息
import com.dailytracker.dto.response.SummaryResponse;
// SummaryService: 每日总结业务逻辑层接口
import com.dailytracker.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日总结控制器（SummaryController）
 *
 * 【职责说明】
 * 本控制器负责处理每日复盘总结相关的所有API请求，包括：
 *   1. 总结的CRUD操作（创建、查询、更新、删除）
 *   2. 总结列表的分页查询与日期范围筛选
 *   3. 获取今日总结（快捷接口，用于首页展示）
 *   4. 连续记录天数统计（打卡/streak功能）
 *   5. 情绪趋势分析（追踪心情变化）
 *
 * 【业务背景 - 每日复盘】
 * 每日总结/复盘是时间管理中非常重要的一环：
 *   - 回顾今天的完成情况：做了什么、没做什么
 *   - 记录心情和感受：情绪追踪有助于心理健康
 *   - 总结经验和教训：持续改进
 *   - 连续记录天数（Streak）：通过"连续打卡"激励用户坚持
 *
 * 【RESTful设计思想】
 * - CRUD部分遵循标准设计（与其他Controller相同）
 * - /today 是快捷路径：避免前端每次都要传入当前日期
 * - /streak 和 /mood-trend 是统计子资源：数据源自总结记录，但经过聚合计算
 *
 * 【API路径前缀】/api/v1/summaries
 */
@Tag(name = "每日总结", description = "每日复盘总结管理接口")
@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * 创建新的每日总结
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/summaries
     *
     * 【参数说明】
     * @param request 总结创建请求DTO，包含：
     *                - summaryDate: 总结日期（必填），一天只能有一条总结
     *                - mood: 心情评分（如1-5分或HAPPY/NORMAL/SAD等枚举）
     *                - content: 总结内容（今天做了什么、学了什么等）
     *                - reflection: 反思与感悟
     *                - gratitude: 感恩记录（今天值得感恩的事）
     *                - improvement: 明天可以改进的地方
     *                @Valid 触发校验
     *
     * 【返回值】Result<SummaryResponse> - 创建成功后的总结信息
     *
     * 【设计说明】
     * - 一天只能创建一条总结（唯一约束：userId + summaryDate）
     * - 如果当天已有总结，应该调用更新接口而非创建接口
     * - 建议在Service层添加唯一性校验，已存在时抛出明确的业务异常
     */
    @Operation(summary = "创建总结")
    @PostMapping
    public Result<SummaryResponse> create(@Valid @RequestBody SummaryCreateRequest request) {
        return Result.success(summaryService.create(request));
    }

    /**
     * 分页查询总结列表，支持日期范围筛选
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/summaries
     *
     * 【参数说明】
     * @param pageNum   页码（默认1）
     * @param pageSize  每页条数（默认20）
     * @param startDate 日期范围-开始（可选）
     * @param endDate   日期范围-结束（可选）
     *
     * 【返回值】Result<List<SummaryResponse>> - 总结列表
     *
     * 【设计说明】
     * - 返回List而非IPage：这里使用了自定义的分页封装方式
     *   （Service层可能返回Page<SummaryResponse>，然后提取List返回）
     * - 按日期倒序排列（最新的总结排在前面）
     *
     * 【注意】
     * - 虽然参数中有pageNum和pageSize，但返回类型是List而非IPage
     *   这意味着可能缺少总记录数等分页元信息
     *   更规范的做法是返回IPage<SummaryResponse>或自定义的PageResult<SummaryResponse>
     */
    @Operation(summary = "总结列表")
    @GetMapping
    public Result<List<SummaryResponse>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(summaryService.list(pageNum, pageSize, startDate, endDate));
    }

    /**
     * 获取指定总结的详细信息
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/summaries/{id}
     *
     * @param id 总结ID
     * @return 总结的完整信息
     */
    @Operation(summary = "总结详情")
    @GetMapping("/{id}")
    public Result<SummaryResponse> getById(@PathVariable Long id) {
        return Result.success(summaryService.getById(id));
    }

    /**
     * 更新指定总结
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/summaries/{id}
     *
     * @param id      总结ID
     * @param request 更新内容
     * @return 更新后的总结信息
     *
     * 【使用场景】
     * - 用户修改当天已提交的总结
     * - 补充反思和感悟
     * - 修改心情评分
     */
    @Operation(summary = "更新总结")
    @PutMapping("/{id}")
    public Result<SummaryResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody SummaryCreateRequest request) {
        return Result.success(summaryService.update(id, request));
    }

    /**
     * 删除指定总结
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/summaries/{id}
     *
     * @param id 要删除的总结ID
     * @return 删除成功
     *
     * 【注意】
     * - 删除总结可能影响连续记录天数（streak）的统计
     * - 通常使用逻辑删除（软删除），保留数据用于后续分析
     */
    @Operation(summary = "删除总结")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        summaryService.delete(id);
        return Result.success();
    }

    /**
     * 获取今日总结（快捷接口）
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/summaries/today
     *
     * 【参数说明】无需参数，自动获取当天的总结
     *
     * 【返回值】Result<SummaryResponse> - 今日的总结信息
     *           如果今天还没有写总结，可能返回null或特定状态码
     *
     * 【设计说明】
     * - 提供专用的 /today 快捷路径：前端不需要自己计算当前日期
     * - 这是一种"便捷API"设计：为最常用的查询场景提供简化接口
     * - 减少前端工作量，也减少出错可能
     *
     * 【应用场景】
     * - APP首页展示今日总结状态（已写/未写）
     * - 点击进入今日总结的编辑页面
     */
    @Operation(summary = "获取今日总结")
    @GetMapping("/today")
    public Result<SummaryResponse> getToday() {
        // Service层内部获取当前日期，查询当天的总结记录
        return Result.success(summaryService.getToday());
    }

    /**
     * 获取连续记录天数（Streak）
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/summaries/streak
     *
     * 【参数说明】无需参数
     *
     * 【返回值】Result<Map<String, Object>> - 连续记录统计
     *           可能包含：
     *           - currentStreak: 当前连续天数（从今天往前数，连续有记录的天数）
     *           - longestStreak: 历史最长连续天数
     *           - lastRecordDate: 最后一次记录的日期
     *
     * 【Streak（连续记录）计算逻辑】
     * - 从今天开始往前逐天检查：
     *   - 如果今天有总结记录 -> 继续检查昨天
     *   - 如果某天没有记录 -> 停止，当前连续天数 = 已检查的连续天数
     * - 示例：今天周一有记录，昨天周日有记录，前天周六没记录 -> currentStreak = 2
     *
     * 【应用场景】
     * - 类似GitHub的贡献绿点图，激励用户每天坚持记录
     * - 展示在个人中心或数据统计页面
     * - 可以配合成就系统（如连续7天、30天、100天获得徽章）
     */
    @Operation(summary = "连续记录天数")
    @GetMapping("/streak")
    public Result<Map<String, Object>> getStreak() {
        return Result.success(summaryService.getStreak());
    }

    /**
     * 获取情绪趋势数据
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/summaries/mood-trend
     *
     * 【参数说明】
     * @param days 查询最近N天的情绪数据，默认30天
     *             @RequestParam(defaultValue = "30") 如果不传则默认查询最近30天
     *
     * 【返回值】Result<List<Map<String, Object>>> - 情绪趋势数据列表
     *           每条记录包含：日期、心情评分/类型
     *           例如：[{"date":"2024-01-01","mood":4},{"date":"2024-01-02","mood":3},...]
     *
     * 【应用场景】
     * - 前端折线图展示情绪变化趋势
     * - 帮助用户了解自己的情绪波动规律
     * - 可以结合日历热力图展示（类似GitHub贡献图）
     *
     * 【设计说明】
     * - 使用"天数"而非"日期范围"作为参数：更直观
     *   用户通常想看"最近30天"而非"2024-01-01到2024-01-30"
     * - 默认30天：提供合理的默认值，覆盖大部分使用场景
     */
    @Operation(summary = "情绪趋势（近N天）")
    @GetMapping("/mood-trend")
    public Result<List<Map<String, Object>>> getMoodTrend(
            @RequestParam(defaultValue = "30") int days) { // 默认查询最近30天的情绪数据
        return Result.success(summaryService.getMoodTrend(days));
    }
}
