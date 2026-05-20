package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
// IPage: MyBatis-Plus提供的分页接口，包含总记录数、总页数、当前页数据列表等分页信息
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.common.result.Result;
// AccountingCreateRequest: 创建/更新账目请求DTO，包含金额、类型（收入/支出）、分类、备注等字段
import com.dailytracker.dto.request.AccountingCreateRequest;
// BudgetCreateRequest: 设置预算请求DTO，包含年度、月份、预算金额等字段
import com.dailytracker.dto.request.BudgetCreateRequest;
// AccountingResponse: 账目响应DTO，包含账目的完整信息
import com.dailytracker.dto.response.AccountingResponse;
// AccountingStatsResponse: 账目统计响应DTO，包含收支汇总、分类统计等
import com.dailytracker.dto.response.AccountingStatsResponse;
// AccountingService: 账目业务逻辑层接口
import com.dailytracker.service.AccountingService;
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
 * 每日记账控制器（AccountingController）
 *
 * 【职责说明】
 * 本控制器负责处理每日记账相关的所有API请求，包括：
 *   1. 账目的CRUD操作（创建、查询、更新、删除）
 *   2. 账目列表的分页查询与筛选（按日期范围、收支类型）
 *   3. 多维度统计（日统计、月统计、年统计、分类统计）
 *   4. 预算管理（设置预算、查询预算执行情况）
 *   5. 记账分类管理（获取分类树）
 *
 * 【RESTful设计思想 - 标准CRUD映射】
 * 本控制器是RESTful CRUD设计的典型示例：
 *   POST   /api/v1/accounting       -> 创建账目（Create）
 *   GET    /api/v1/accounting       -> 查询账目列表（Read - List）
 *   GET    /api/v1/accounting/{id}  -> 查询账目详情（Read - Detail）
 *   PUT    /api/v1/accounting/{id}  -> 更新账目（Update）
 *   DELETE /api/v1/accounting/{id}  -> 删除账目（Delete）
 *
 * 【RESTful URL设计规范】
 * - 资源用名词复数：/accounting 表示"账目"资源集合
 * - 子资源用路径层级：/statistics/daily、/statistics/monthly 表示"统计"下的子维度
 * - 非CRUD操作用动词名词化：/budget 表示"预算"资源，/categories 表示"分类"资源
 * - 路径参数{ id}标识具体资源：/accounting/123 表示ID为123的账目
 *
 * 【分页设计说明】
 * - 使用MyBatis-Plus的IPage接口封装分页信息
 * - 分页参数：pageNum（页码，从1开始）、pageSize（每页条数，默认20）
 * - 前端可通过 pageNum 和 pageSize 参数灵活控制分页
 *
 * 【API路径前缀】/api/v1/accounting
 */
@Tag(name = "每日记账", description = "收支记账管理接口")
@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    /**
     * 创建一条新的账目记录
     *
     * 【HTTP方法】POST - 表示"创建新资源"
     * 【URL路径】POST /api/v1/accounting
     *
     * 【参数说明】
     * @param request 创建账目请求DTO，包含：
     *                - amount: 金额（必填，BigDecimal类型确保精度）
     *                - type: 类型（INCOME收入/EXPENSE支出）
     *                - categoryId: 分类ID（如餐饮、交通、工资等）
     *                - date: 记账日期
     *                - remark: 备注说明
     *                @Valid 触发JSR-303校验（金额不能为负数、类型不能为空等）
     *                @RequestBody 将JSON请求体反序列化为Java对象
     *
     * 【返回值】Result<AccountingResponse> - 包含创建成功后的完整账目信息（含自动生成的ID）
     *
     * 【设计说明】
     * - POST /accounting 不带路径参数，表示在"账目集合"中创建一条新记录
     * - 返回创建后的完整对象（含ID），方便前端直接使用，无需再次查询
     * - REST最佳实践：创建成功应返回201 Created状态码，这里简化为200 + 业务code
     */
    @Operation(summary = "创建账目")
    @PostMapping // 注意：没有额外路径，直接映射到 /api/v1/accounting
    public Result<AccountingResponse> create(@Valid @RequestBody AccountingCreateRequest request) {
        return Result.success(accountingService.create(request));
    }

    /**
     * 分页查询账目列表，支持日期范围和类型筛选
     *
     * 【HTTP方法】GET - 表示"查询/读取资源"
     * 【URL路径】GET /api/v1/accounting
     *
     * 【参数说明】（全部通过URL查询参数传递）
     * @param pageNum   当前页码，默认第1页
     *                  @RequestParam(defaultValue = "1") 如果未提供则使用默认值1
     * @param pageSize  每页条数，默认20条
     *                  默认值不宜过大，避免一次查询过多数据导致性能问题
     * @param startDate 筛选开始日期（可选），格式：yyyy-MM-dd
     *                  @RequestParam(required = false) 表示该参数是可选的
     * @param endDate   筛选结束日期（可选）
     * @param type      收支类型筛选（可选），如"INCOME"或"EXPENSE"
     *
     * 【返回值】Result<IPage<AccountingResponse>> - 分页结果，包含：
     *           - records: 当前页的数据列表
     *           - total: 总记录数
     *           - pages: 总页数
     *           - current: 当前页码
     *           - size: 每页条数
     *
     * 【请求示例】
     * GET /api/v1/accounting?pageNum=1&pageSize=20&startDate=2024-01-01&endDate=2024-01-31&type=EXPENSE
     *
     * 【分页设计最佳实践】
     * - pageNum从1开始（对用户更友好），而非从0开始
     * - 提供合理的默认值（pageSize=20），避免一次性返回过多数据
     * - 限制pageSize最大值（如不超过100），防止恶意请求导致性能问题
     * - 使用数据库分页（LIMIT OFFSET），而非查询全部后在内存中分页
     */
    @Operation(summary = "账目列表（日期 + 类型筛选）")
    @GetMapping // 映射HTTP GET请求到 /api/v1/accounting（与POST /accounting路径相同，通过HTTP方法区分）
    public Result<IPage<AccountingResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,     // 页码，默认第1页
            @RequestParam(defaultValue = "20") int pageSize,   // 每页条数，默认20
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, // 可选：开始日期
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,   // 可选：结束日期
            @RequestParam(required = false) String type) {     // 可选：收支类型
        return Result.success(accountingService.page(pageNum, pageSize, startDate, endDate, type));
    }

    /**
     * 获取指定账目的详细信息
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/{id}
     *
     * 【参数说明】
     * @param id 账目ID，从URL路径中提取
     *           @PathVariable 将URL中的路径变量（{id}）绑定到方法参数
     *           例如请求 /accounting/123，id的值就是123
     *
     * 【返回值】Result<AccountingResponse> - 该账目的完整信息
     *
     * 【RESTful设计说明】
     * - 路径参数{ id}用于标识具体的资源实例
     * - GET /accounting 列出所有账目 vs GET /accounting/123 获取特定账目
     * - 这是RESTful中"资源集合"和"资源实例"的标准设计
     */
    @Operation(summary = "账目详情")
    @GetMapping("/{id}") // {id}是路径变量（路径模板），运行时替换为实际的账目ID
    public Result<AccountingResponse> getById(
            // @PathVariable: 将URL路径中{id}的值绑定到方法参数id
            // Spring自动将字符串"123"转换为Long类型的123
            @PathVariable Long id) {
        return Result.success(accountingService.getById(id));
    }

    /**
     * 更新指定的账目记录
     *
     * 【HTTP方法】PUT - 表示"更新/替换"资源
     * 【URL路径】PUT /api/v1/accounting/{id}
     *
     * 【参数说明】
     * @param id      要更新的账目ID，从URL路径获取
     * @param request 更新内容，JSON请求体
     *                使用与创建相同的DTO（AccountingCreateRequest），因为更新字段与创建字段相同
     *
     * 【返回值】Result<AccountingResponse> - 更新后的完整账目信息
     *
     * 【RESTful设计说明】
     * - PUT /accounting/{ id} 表示"更新ID为{id}的账目"
     * - 路径参数标识"更新哪个"，请求体指定"更新为什么"
     * - 返回更新后的完整对象，前端可直接刷新界面
     */
    @Operation(summary = "更新账目")
    @PutMapping("/{id}")
    public Result<AccountingResponse> update(
            @PathVariable Long id,                                  // 路径参数：要更新的账目ID
            @Valid @RequestBody AccountingCreateRequest request) {   // 请求体：新的账目数据
        return Result.success(accountingService.update(id, request));
    }

    /**
     * 删除指定的账目记录
     *
     * 【HTTP方法】DELETE - 表示"删除资源"
     * 【URL路径】DELETE /api/v1/accounting/{id}
     *
     * 【参数说明】
     * @param id 要删除的账目ID，从URL路径获取
     *
     * 【返回值】Result<Void> - 删除成功，不返回数据
     *
     * 【RESTful设计说明】
     * - DELETE方法语义明确：删除指定资源
     * - 删除成功后通常返回204 No Content或200 OK
     * - 注意：删除操作是幂等的（删除一个已删除的资源不会报错）
     *
     * 【安全考虑】
     * - 应校验当前用户是否有权删除该账目（防止越权删除他人数据）
     * - 可以考虑使用逻辑删除（设置deleted标志位）而非物理删除，便于数据恢复
     */
    @Operation(summary = "删除账目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountingService.delete(id);
        return Result.success();
    }

    /**
     * 获取指定日期的账目统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/statistics/daily
     *
     * 【参数说明】
     * @param date 统计日期（可选），格式：yyyy-MM-dd
     *              如果不提供，默认统计当天的数据
     *
     * 【返回值】Result<AccountingStatsResponse> - 日统计数据
     *           包含：当日总收入、总支出、结余、各分类金额明细等
     */
    @Operation(summary = "日统计")
    @GetMapping("/statistics/daily") // /statistics/daily 是二级子路径：统计 -> 日统计
    public Result<AccountingStatsResponse> dailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(accountingService.dailyStats(date));
    }

    /**
     * 获取指定月份的账目统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/statistics/monthly
     *
     * 【参数说明】
     * @param year  年份（必填），如2024
     * @param month 月份（必填），1-12
     *
     * 【返回值】Result<AccountingStatsResponse> - 月统计数据
     *           包含：月度总收入、总支出、结余、日均收支、分类汇总等
     */
    @Operation(summary = "月统计")
    @GetMapping("/statistics/monthly")
    public Result<AccountingStatsResponse> monthlyStats(
            @RequestParam int year,   // 年份，必填
            @RequestParam int month) { // 月份，必填
        return Result.success(accountingService.monthlyStats(year, month));
    }

    @Operation(summary = "月统计（兼容旧路径）")
    @GetMapping("/monthly-statistics")
    public Result<AccountingStatsResponse> monthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(accountingService.monthlyStats(year, month));
    }

    /**
     * 获取指定年份的账目统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/statistics/yearly
     *
     * 【参数说明】
     * @param year 年份（必填），如2024
     *
     * 【返回值】Result<AccountingStatsResponse> - 年度统计数据
     *           包含：年度总收入、总支出、月度趋势、最大单笔收支等
     */
    @Operation(summary = "年统计")
    @GetMapping("/statistics/yearly")
    public Result<AccountingStatsResponse> yearlyStats(@RequestParam int year) {
        return Result.success(accountingService.yearlyStats(year));
    }

    /**
     * 按分类统计收支
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/statistics/category
     *
     * 【参数说明】
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @param type      收支类型（可选），"INCOME"或"EXPENSE"
     *
     * 【返回值】Result<List<AccountingStatsResponse.CategoryStat>> - 分类统计列表
     *           每个元素包含：分类名称、金额、占比百分比等
     *
     * 【应用场景】
     * - 前端饼图/环形图展示各分类的支出占比
     * - 帮助用户了解"钱花在哪了"
     */
    @Operation(summary = "分类统计")
    @GetMapping("/statistics/category")
    public Result<List<AccountingStatsResponse.CategoryStat>> categoryStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {
        return Result.success(accountingService.categoryStats(startDate, endDate, type));
    }

    /**
     * 设置月度预算
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/accounting/budget
     *
     * 【参数说明】
     * @param request 预算设置请求DTO，包含：
     *                - year: 年份
     *                - month: 月份
     *                - amount: 预算金额
     *                - type: 预算类型（月总预算/分类预算等）
     *
     * 【返回值】Result<Void> - 设置成功
     *
     * 【设计说明】
     * - 预算是一个独立于账目的子资源，因此使用独立的路径 /budget
     * - POST /budget 表示"创建/设置"预算（如果已存在则更新，即upsert语义）
     */
    @Operation(summary = "设置预算")
    @PostMapping("/budget")
    public Result<Void> setBudget(@Valid @RequestBody BudgetCreateRequest request) {
        accountingService.setBudget(request);
        return Result.success();
    }

    /**
     * 获取指定月份的预算及执行情况
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/budget?year=2024&month=1
     *
     * 【参数说明】
     * @param year  年份（必填）
     * @param month 月份（必填）
     *
     * 【返回值】Result<Map<String, Object>> - 预算执行情况
     *           包含：预算总额、已用金额、剩余金额、使用百分比、是否超支等
     *
     * 【设计说明】
     * - 同一个路径 /budget，GET查询、POST设置，通过HTTP方法区分操作
     *   这是RESTful的典型设计：相同路径，不同HTTP方法代表不同操作
     */
    @Operation(summary = "获取预算及执行情况")
    @GetMapping("/budget")
    public Result<Map<String, Object>> getBudget(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(accountingService.getBudget(year, month));
    }

    /**
     * 获取记账分类树
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/accounting/categories
     *
     * 【参数说明】
     * @param type 收支类型（可选），筛选收入分类或支出分类
     *
     * 【返回值】Result<List<Map<String, Object>>> - 分类树结构
     *           可能是树形结构（一级分类 -> 二级分类），用于前端的分类选择器
     *
     * 【设计说明】
     * - /categories 是独立的资源路径，获取分类元数据（不涉及具体账目）
     * - 分类数据变化频率低，适合做缓存优化
     */
    @Operation(summary = "获取记账分类树")
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories(
            @RequestParam(required = false) String type) {
        return Result.success(accountingService.getCategories(type));
    }
}
