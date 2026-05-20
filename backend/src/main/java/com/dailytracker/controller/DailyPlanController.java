package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// PlanBatchUpdateRequest: 批量更新任务请求DTO，包含任务ID列表和要批量修改的字段
import com.dailytracker.dto.request.PlanBatchUpdateRequest;
// PlanCreateRequest: 创建任务请求DTO，包含任务名称、日期、优先级、分类、时间块等字段
import com.dailytracker.dto.request.PlanCreateRequest;
// RepeatUpdateRequest: 更新重复规则请求DTO，包含重复频率、间隔、结束条件等字段
import com.dailytracker.dto.request.RepeatUpdateRequest;
// PlanResponse: 任务响应DTO，包含任务的完整信息（含子任务、标签等关联数据）
import com.dailytracker.dto.response.PlanResponse;
// DailyPlanService: 每日计划业务逻辑层接口
import com.dailytracker.service.DailyPlanService;
import io.swagger.v3.oas.annotations.Operation;
// @Parameter: Swagger/OpenAPI注解，用于描述方法参数的含义，显示在API文档中
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日计划控制器（DailyPlanController）
 *
 * 【职责说明】
 * 本控制器是系统中最复杂的控制器之一，负责处理每日计划/任务的所有API请求，包括：
 *   1. 任务的基础CRUD操作（创建、查询、更新、删除）
 *   2. 任务状态管理（待办、进行中、已完成、已取消）
 *   3. 任务排序（批量更新排序）
 *   4. 任务顺延（将未完成的任务推迟到下一天）
 *   5. 任务模板（保存为模板、从模板创建）
 *   6. 重复任务管理（设置重复规则、生成重复实例、停止重复）
 *   7. 子任务管理（创建子任务、获取子任务、更新子任务状态、子任务转主任务）
 *   8. 数据分析（完成率趋势、分类分布、优先级分布、时间分配统计）
 *   9. 时间块管理（时间冲突检测、时间块列表）
 *  10. 批量操作（批量更新、批量删除、批量顺延、批量完成）
 *
 * 【RESTful设计思想 - 复杂资源的嵌套路径设计】
 * 本控制器展示了RESTful API中嵌套资源路径的设计模式：
 *   - 主资源：/api/v1/daily-plans（任务）
 *   - 子资源通过路径嵌套表达：
 *     /daily-plans/{id}/status      -> 任务的状态（子资源）
 *     /daily-plans/{id}/subtasks    -> 任务的子任务（子资源）
 *     /daily-plans/{id}/repeat      -> 任务的重复规则（子资源）
 *     /daily-plans/{id}/templates   -> 任务模板（子资源）
 *   - 动作型操作使用动词路径：
 *     /daily-plans/batch-sort       -> 批量排序
 *     /daily-plans/batch-update     -> 批量更新
 *     /daily-plans/batch-delete     -> 批量删除
 *     /daily-plans/batch-postpone   -> 批量顺延
 *     /daily-plans/batch-complete   -> 批量完成
 *
 * 【API路径前缀】/api/v1/daily-plans
 */
@Tag(name = "每日计划", description = "每日任务计划管理接口")
@RestController
@RequestMapping("/api/v1/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

    // ==================== 基础CRUD操作 ====================

    /**
     * 创建一条新的计划任务
     *
     * 【HTTP方法】POST - 创建新资源
     * 【URL路径】POST /api/v1/daily-plans
     *
     * 【参数说明】
     * @param request 创建任务请求DTO，包含：
     *                - title: 任务标题（必填）
     *                - planDate: 计划日期（默认今天）
     *                - priority: 优先级（HIGH/MEDIUM/LOW）
     *                - category: 分类（如工作、学习、生活等）
     *                - startTime/endTime: 时间块（可选，用于时间管理）
     *                - description: 任务描述
     *                @Valid 触发校验：确保标题不为空等
     *
     * 【返回值】Result<PlanResponse> - 创建成功后的完整任务信息
     */
    @Operation(summary = "创建任务")
    @PostMapping
    public Result<PlanResponse> create(@Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.create(request));
    }

    /**
     * 按日期获取任务列表
     *
     * 【HTTP方法】GET - 查询资源
     * 【URL路径】GET /api/v1/daily-plans
     *
     * 【参数说明】
     * @param planDate 计日期（可选），格式：yyyy-MM-dd，默认返回今天的任务
     *                 @Parameter: Swagger注解，为API文档添加参数描述
     *                 @DateTimeFormat: 自动将字符串"2024-01-15"转为LocalDate对象
     *
     * 【返回值】Result<List<PlanResponse>> - 该日期的所有任务列表（按排序字段排列）
     *
     * 【设计说明】
     * - 不使用分页：因为单日的任务数量通常不多（一般不超过30条），全量返回即可
     * - 与AccountingController的GET不同：这里返回List而非IPage，因为没有分页需求
     * - 同一URL路径 /daily-plans，GET获取列表 vs POST创建新任务，通过HTTP方法区分
     */
    @Operation(summary = "任务列表（按日期）")
    @GetMapping
    public Result<List<PlanResponse>> list(
            // @Parameter: Swagger注解，描述参数的含义和默认行为，显示在API文档的参数说明中
            @Parameter(description = "日期，格式：yyyy-MM-dd，默认今天")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.listByDate(planDate));
    }

    /**
     * 获取指定任务的详细信息
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/{id}
     *
     * 【参数说明】@param id 任务ID，路径参数
     * 【返回值】Result<PlanResponse> - 任务的完整信息（含子任务、标签等）
     */
    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<PlanResponse> getById(@PathVariable Long id) {
        return Result.success(dailyPlanService.getById(id));
    }

    /**
     * 更新指定任务的信息
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/{id}
     *
     * 【参数说明】
     * @param id      任务ID（路径参数）
     * @param request 更新内容（请求体，使用与创建相同的DTO）
     *
     * 【返回值】Result<PlanResponse> - 更新后的完整任务信息
     */
    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<PlanResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.update(id, request));
    }

    /**
     * 删除指定任务
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/daily-plans/{id}
     *
     * 【注意】删除主任务时，通常会级联删除其子任务
     */
    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dailyPlanService.delete(id);
        return Result.success();
    }

    // ==================== 状态与排序操作 ====================

    /**
     * 更新任务状态
     *
     * 【HTTP方法】PUT - 更新资源的某个属性
     * 【URL路径】PUT /api/v1/daily-plans/{id}/status
     *
     * 【RESTful设计说明】
     * - 将"状态"视为任务的子资源：/daily-plans/{id}/status
     * - 使用PUT方法更新状态（也可以使用PATCH表示部分更新）
     * - 这种"主资源/子资源属性"的路径设计在REST中很常见
     *
     * 【参数说明】
     * @param id     任务ID（路径参数）
     * @param status 新状态值（查询参数），如：TODO（待办）、IN_PROGRESS（进行中）、COMPLETED（已完成）
     *
     * 【返回值】Result<Void> - 更新成功
     */
    @Operation(summary = "更新任务状态")
    @PutMapping("/{id}/status") // 路径设计：{id}/status 表示"ID为{id}的任务的状态"
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) { // status通过查询参数传递，如 ?status=COMPLETED
        dailyPlanService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 批量更新任务排序
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/batch-sort
     *
     * 【参数说明】
     * @param sortMap 排序映射，key=任务ID，value=新的排序号
     *                例如：{1: 0, 2: 1, 3: 2} 表示ID为1的任务排第0位，以此类推
     *                前端拖拽排序后，将新的顺序通过此接口保存
     *
     * 【返回值】Result<Void> - 排序更新成功
     *
     * 【设计说明】
     * - 路径使用 /batch-sort 而非 /batch（因为batch语义不够明确）
     * - 使用Map<Long, Integer>而非DTO：简化设计，排序数据结构简单
     */
    @Operation(summary = "批量更新排序")
    @PutMapping("/batch-sort")
    public Result<Void> batchSort(@RequestBody Map<Long, Integer> sortMap) {
        dailyPlanService.batchUpdateSort(sortMap);
        return Result.success();
    }

    // ==================== 任务顺延 ====================

    /**
     * 将任务顺延至下一天
     *
     * 【HTTP方法】POST - 顺延是一种"动作"，使用POST
     * 【URL路径】POST /api/v1/daily-plans/{id}/postpone
     *
     * 【参数说明】@param id 要顺延的任务ID
     * 【返回值】Result<PlanResponse> - 顺延后的任务信息（日期已更新为明天）
     *
     * 【设计说明】
     * - 使用POST而非PUT：顺延是一个"动作"而非简单的"更新"
     * - 路径 /postpone 使用动词，表达"顺延"这个操作（RESTful中动词路径用于非CRUD操作）
     * - 返回更新后的对象，前端可以据此更新界面上的日期显示
     */
    @Operation(summary = "任务顺延至下一天")
    @PostMapping("/{id}/postpone")
    public Result<PlanResponse> postpone(@PathVariable Long id) {
        return Result.success(dailyPlanService.postpone(id));
    }

    // ==================== 统计分析 ====================

    /**
     * 获取指定日期的任务统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/statistics
     *
     * 【参数说明】@param planDate 日期（可选），默认今天
     * 【返回值】Result<Map<String, Object>> - 统计数据
     *           包含：总任务数、已完成数、完成率、各优先级/分类的任务数等
     */
    @Operation(summary = "任务统计（按日期）")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.getStatistics(planDate));
    }

    // ==================== 模板管理 ====================

    /**
     * 将已有任务保存为模板
     *
     * 【HTTP方法】POST - 创建新资源（模板）
     * 【URL路径】POST /api/v1/daily-plans/{id}/templates
     *
     * 【参数说明】
     * @param id           要保存为模板的任务ID
     * @param templateName 模板名称（查询参数），如"工作日晨间惯例"
     *
     * 【返回值】Result<Void> - 保存成功
     *
     * 【RESTful设计】
     * - /daily-plans/{id}/templates 表示"任务{ id}的模板"
     * - POST表示"在该任务下创建一个模板"
     * - 模板是任务的子资源，通过路径嵌套体现归属关系
     */
    @Operation(summary = "保存为模板")
    @PostMapping("/{id}/templates")
    public Result<Void> saveAsTemplate(
            @PathVariable Long id,
            @RequestParam String templateName) { // 模板名称通过查询参数传递
        dailyPlanService.saveAsTemplate(id, templateName);
        return Result.success();
    }

    /**
     * 获取所有任务模板列表
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/templates
     *
     * 【返回值】Result<List<PlanResponse>> - 所有模板列表
     *
     * 【设计说明】
     * - 模板列表不关联特定的任务，因此路径为 /templates（无{id}前缀）
     * - 与 POST /{id}/templates 形成对比：一个创建模板，一个查询模板列表
     */
    @Operation(summary = "模板列表")
    @GetMapping("/templates")
    public Result<List<PlanResponse>> listTemplates() {
        return Result.success(dailyPlanService.listTemplates());
    }

    // ==================== 重复任务管理 ====================

    /**
     * 生成指定时间范围内的重复任务实例
     *
     * 【HTTP方法】POST - 触发生成动作
     * 【URL路径】POST /api/v1/daily-plans/{id}/repeat/instances
     *
     * 【参数说明】
     * @param id        带有重复规则的任务ID
     * @param startDate 生成实例的开始日期
     * @param endDate   生成实例的结束日期
     *
     * 【返回值】Result<List<PlanResponse>> - 生成的所有任务实例列表
     *
     * 【重复任务设计说明】
     * - 重复任务分两层：重复规则（RepeatRule）+ 任务实例（Instance）
     * - 重复规则定义：每天/每周/每月重复，间隔频率，结束条件等
     * - 任务实例是具体的某一天的任务，由重复规则按日期展开生成
     * - 本接口用于手动触发生成某个时间范围内的重复任务实例
     */
    @Operation(summary = "生成重复任务实例")
    @PostMapping("/{id}/repeat/instances")
    public Result<List<PlanResponse>> generateRepeatInstances(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.generateRepeatInstances(id, startDate, endDate));
    }

    /**
     * 更新任务的重复规则
     *
     * 【HTTP方法】PUT - 更新重复规则
     * 【URL路径】PUT /api/v1/daily-plans/{id}/repeat
     *
     * 【参数说明】
     * @param id      任务ID
     * @param request 重复规则更新请求，包含：
     *                - frequency: 频率（DAILY/WEEKLY/MONTHLY/YEARLY）
     *                - interval: 间隔（每N天/周/月/年重复一次）
     *                - endDate: 重复结束日期（可选）
     *                - daysOfWeek: 每周哪几天重复（如[1,3,5]表示周一三五）
     *
     * 【返回值】Result<PlanResponse> - 更新后的任务信息（含新的重复规则）
     */
    @Operation(summary = "更新重复规则")
    @PutMapping("/{id}/repeat")
    public Result<PlanResponse> updateRepeatRule(
            @PathVariable Long id,
            @Valid @RequestBody RepeatUpdateRequest request) {
        return Result.success(dailyPlanService.updateRepeatRule(id, request));
    }

    /**
     * 停止任务的重复
     *
     * 【HTTP方法】DELETE - 删除重复规则
     * 【URL路径】DELETE /api/v1/daily-plans/{id}/repeat
     *
     * 【参数说明】@param id 任务ID
     * 【返回值】Result<Void> - 停止成功
     *
     * 【设计说明】
     * - DELETE /{id}/repeat 表示"删除任务的重复规则"
     * - 停止后，已生成的任务实例不受影响，只是不再生成新的实例
     */
    @Operation(summary = "停止重复")
    @DeleteMapping("/{id}/repeat")
    public Result<Void> stopRepeat(@PathVariable Long id) {
        dailyPlanService.stopRepeat(id);
        return Result.success();
    }

    // ==================== 子任务管理 ====================

    /**
     * 在指定任务下创建子任务
     *
     * 【HTTP方法】POST - 创建子资源
     * 【URL路径】POST /api/v1/daily-plans/{parentId}/subtasks
     *
     * 【参数说明】
     * @param parentId 父任务ID（路径参数）
     * @param request  子任务创建请求（与创建主任务相同的DTO）
     *
     * 【返回值】Result<PlanResponse> - 创建的子任务信息
     *
     * 【RESTful设计 - 嵌套资源】
     * - /daily-plans/{parentId}/subtasks 表示"父任务下的子任务集合"
     * - POST表示"在父任务的子任务集合中创建一条新的子任务"
     * - 这是RESTful中"一对多关系"的标准路径设计：
     *   父资源/{parentId}/子资源集合
     */
    @Operation(summary = "创建子任务")
    @PostMapping("/{parentId}/subtasks")
    public Result<PlanResponse> createSubtask(
            @PathVariable Long parentId,
            @Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.createSubtask(parentId, request));
    }

    /**
     * 获取指定任务的所有子任务
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/{parentId}/subtasks
     *
     * 【参数说明】@param parentId 父任务ID
     * 【返回值】Result<List<PlanResponse>> - 子任务列表
     */
    @Operation(summary = "获取子任务列表")
    @GetMapping("/{parentId}/subtasks")
    public Result<List<PlanResponse>> getSubtasks(@PathVariable Long parentId) {
        return Result.success(dailyPlanService.getSubtasks(parentId));
    }

    /**
     * 更新子任务状态
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/subtasks/{id}/status
     *
     * 【参数说明】
     * @param id     子任务ID（路径参数）
     * @param status 新状态（查询参数）
     *
     * 【返回值】Result<Void> - 更新成功
     *
     * 【设计说明】
     * - 路径为 /subtasks/{id}/status 而非 /{parentId}/subtasks/{id}/status
     *   因为子任务有独立的ID，不需要通过父任务ID定位
     * - 这是扁平化路径设计的体现：虽然子任务属于父任务，但操作时可以直接通过子任务ID访问
     */
    @Operation(summary = "更新子任务状态")
    @PutMapping("/subtasks/{id}/status")
    public Result<Void> updateSubtaskStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        dailyPlanService.updateSubtaskStatus(id, status);
        return Result.success();
    }

    /**
     * 将子任务提升为独立的主任务
     *
     * 【HTTP方法】POST - 转换操作
     * 【URL路径】POST /api/v1/daily-plans/subtasks/{id}/convert
     *
     * 【参数说明】@param id 要转换的子任务ID
     * 【返回值】Result<Void> - 转换成功
     *
     * 【设计说明】
     * - POST /subtasks/{id}/convert 使用动词"convert"表示"转换"操作
     * - 转换后子任务变为独立的主任务，与原父任务解除关系
     */
    @Operation(summary = "子任务转为主任务")
    @PostMapping("/subtasks/{id}/convert")
    public Result<Void> convertToMainTask(@PathVariable Long id) {
        dailyPlanService.convertToMainTask(id);
        return Result.success();
    }

    // ==================== 数据分析接口 ====================

    /**
     * 获取任务完成率趋势数据
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/analytics/trend
     *
     * 【参数说明】
     * @param startDate 开始日期（必填）
     * @param endDate   结束日期（必填）
     *
     * 【返回值】Result<List<Map<String, Object>>> - 每日的完成率数据
     *           每条记录包含：日期、总任务数、已完成数、完成率
     *
     * 【应用场景】
     * - 前端折线图展示每日任务完成率的变化趋势
     * - 帮助用户了解自己的执行力和时间管理改善情况
     */
    @Operation(summary = "完成率趋势")
    @GetMapping("/analytics/trend")
    public Result<List<Map<String, Object>>> getCompletionTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getCompletionTrend(startDate, endDate));
    }

    /**
     * 获取任务分类分布统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/analytics/category
     *
     * 【返回值】各分类的任务数量和占比，用于前端饼图展示
     */
    @Operation(summary = "分类分布")
    @GetMapping("/analytics/category")
    public Result<Map<String, Object>> getCategoryDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getCategoryDistribution(startDate, endDate));
    }

    /**
     * 获取任务优先级分布统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/analytics/priority
     *
     * 【返回值】各优先级（高/中/低）的任务数量和占比
     */
    @Operation(summary = "优先级分布")
    @GetMapping("/analytics/priority")
    public Result<Map<String, Object>> getPriorityDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getPriorityDistribution(startDate, endDate));
    }

    /**
     * 获取时间分配统计
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/analytics/time
     *
     * 【返回值】各任务的时间分配情况（基于时间块数据）
     *
     * 【应用场景】
     * - 分析用户的时间都花在了哪些任务上
     * - 帮助优化时间管理策略
     */
    @Operation(summary = "时间分配统计")
    @GetMapping("/analytics/time")
    public Result<Map<String, Object>> getTimeDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getTimeDistribution(startDate, endDate));
    }

    // ==================== 时间块管理 ====================

    /**
     * 检测指定日期的时间冲突
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/timeblock/conflicts
     *
     * 【参数说明】@param planDate 要检测的日期
     * 【返回值】Result<List<PlanResponse>> - 存在时间冲突的任务列表
     *
     * 【时间冲突说明】
     * - 当两个任务的时间块存在重叠时，即为时间冲突
     * - 例如：任务A的10:00-11:00与任务B的10:30-11:30存在冲突
     * - 前端通常用红色标记冲突的任务，提醒用户调整时间
     */
    @Operation(summary = "检测时间冲突")
    @GetMapping("/timeblock/conflicts")
    public Result<List<PlanResponse>> detectTimeConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.detectTimeConflicts(planDate));
    }

    /**
     * 获取指定日期的时间块列表
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/daily-plans/timeblock
     *
     * 【参数说明】@param planDate 要查询的日期
     * 【返回值】Result<List<PlanResponse>> - 包含时间块信息的任务列表
     *
     * 【应用场景】
     * - 前端日历视图/时间轴视图中展示每个任务的占用时间段
     */
    @Operation(summary = "获取时间块列表")
    @GetMapping("/timeblock")
    public Result<List<PlanResponse>> getTimeBlocks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.getTimeBlocks(planDate));
    }

    // ==================== 批量操作 ====================

    /**
     * 批量更新任务属性（优先级、分类、状态）
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/batch-update
     *
     * 【参数说明】
     * @param request 批量更新请求DTO，包含：
     *                - ids: 要更新的任务ID列表
     *                - priority: 新的优先级（可选）
     *                - category: 新的分类（可选）
     *                - status: 新的状态（可选）
     *
     * 【返回值】Result<Void> - 批量更新成功
     *
     * 【批量操作设计说明】
     * - 路径使用 /batch- 前缀，明确表达"批量"语义
     * - 请求体包含ID列表 + 要修改的字段，Service层只更新非null字段
     */
    @Operation(summary = "批量更新任务")
    @PutMapping("/batch-update")
    public Result<Void> batchUpdate(@RequestBody PlanBatchUpdateRequest request) {
        dailyPlanService.batchUpdate(
                request.getIds(),       // 要更新的任务ID列表
                request.getPriority(),  // 新的优先级（可为null）
                request.getCategory(),  // 新的分类（可为null）
                request.getStatus()     // 新的状态（可为null）
        );
        return Result.success();
    }

    /**
     * 批量删除任务
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/daily-plans/batch-delete
     *
     * 【参数说明】
     * @param ids 要删除的任务ID列表（请求体）
     *            注意：DELETE请求通常不带请求体，但这里为了传递ID列表使用了请求体
     *            这是一种务实的折中方案（虽然不完全符合REST规范）
     *
     * 【返回值】Result<Void> - 批量删除成功
     */
    @Operation(summary = "批量删除任务")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        dailyPlanService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 批量顺延任务
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/batch-postpone
     *
     * 【参数说明】@param ids 要顺延的任务ID列表
     * 【返回值】Result<Void> - 批量顺延成功
     */
    @Operation(summary = "批量顺延任务")
    @PutMapping("/batch-postpone")
    public Result<Void> batchPostpone(@RequestBody List<Long> ids) {
        dailyPlanService.batchPostpone(ids);
        return Result.success();
    }

    /**
     * 批量完成任务
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/daily-plans/batch-complete
     *
     * 【参数说明】@param ids 要完成标记的任务ID列表
     * 【返回值】Result<Void> - 批量完成成功
     *
     * 【设计说明】
     * - 提供专用的批量完成接口，而非使用 batchUpdate(ids, status=COMPLETED)
     * - 因为"完成"是最常用的批量操作，提供专用接口简化前端调用
     */
    @Operation(summary = "批量完成任务")
    @PutMapping("/batch-complete")
    public Result<Void> batchComplete(@RequestBody List<Long> ids) {
        dailyPlanService.batchComplete(ids);
        return Result.success();
    }
}
