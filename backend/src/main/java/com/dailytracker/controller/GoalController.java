package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// GoalCreateRequest: 创建/更新目标请求DTO，包含目标名称、类型、分类、截止日期等字段
import com.dailytracker.dto.request.GoalCreateRequest;
// GoalResponse: 目标响应DTO，包含目标的完整信息
import com.dailytracker.dto.response.GoalResponse;
// GoalService: 目标业务逻辑层接口
import com.dailytracker.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 目标管理控制器（GoalController）
 *
 * 【职责说明】
 * 本控制器负责处理目标管理相关的所有API请求，包括：
 *   1. 目标的CRUD操作（创建、查询、更新、删除）
 *   2. 目标进度更新（手动更新完成百分比）
 *   3. 目标树形结构查询（含子目标的层级展示）
 *   4. 目标统计分析（整体完成情况汇总）
 *
 * 【RESTful设计思想 - 层级资源】
 * 目标管理是一个典型的层级资源场景：
 *   - 五年规划（最顶层）
 *     - 年度目标（第二层）
 *       - 月度目标（第三层）
 *         - 周计划（第四层）
 *           - 每日任务（第五层，在DailyPlanController中管理）
 *
 * 这种层级关系通过parentId字段实现（自引用外键），而非通过URL嵌套。
 * 因为层级可能很深（5层），URL嵌套会导致路径过长。
 *
 * 【API路径前缀】/api/v1/goals
 */
@Tag(name = "目标管理", description = "五年/年度/月度/周计划目标管理接口")
@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    /**
     * 创建新目标
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/goals
     *
     * 【参数说明】
     * @param request 创建目标请求DTO，包含：
     *                - title: 目标标题（必填），如"2024年读完50本书"
     *                - goalType: 目标类型（FIVE_YEAR/ANNUAL/MONTHLY/WEEKLY）
     *                - category: 分类（如职业发展、健康、学习等）
     *                - parentId: 父目标ID（可选，用于创建子目标）
     *                - deadline: 截止日期
     *                - description: 目标描述
     *
     * 【返回值】Result<GoalResponse> - 创建成功后的目标信息
     */
    @Operation(summary = "创建目标")
    @PostMapping
    public Result<GoalResponse> create(@Valid @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.create(request));
    }

    /**
     * 获取目标列表，支持多条件筛选
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/goals
     *
     * 【参数说明】（全部为可选的查询参数）
     * @param goalType 目标类型筛选，如 FIVE_YEAR（五年规划）、ANNUAL（年度）、MONTHLY（月度）
     * @param category 分类筛选，如"职业"、"健康"、"学习"
     * @param status   状态筛选，如 NOT_STARTED（未开始）、IN_PROGRESS（进行中）、COMPLETED（已完成）
     *
     * 【返回值】Result<List<GoalResponse>> - 符合条件的所有目标列表
     *
     * 【设计说明】
     * - 所有筛选参数都是可选的（required=false），不传则返回所有目标
     * - 多个筛选条件之间是AND关系（同时满足）
     * - 返回List而非分页：目标数量通常不会太多，全量返回即可
     */
    @Operation(summary = "目标列表（支持类型/分类/状态筛选）")
    @GetMapping
    public Result<List<GoalResponse>> list(
            @RequestParam(required = false) String goalType,    // 可选：按目标类型筛选
            @RequestParam(required = false) String category,    // 可选：按分类筛选
            @RequestParam(required = false) String status) {    // 可选：按状态筛选
        return Result.success(goalService.list(goalType, category, status));
    }

    /**
     * 获取指定目标的详细信息
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/goals/{id}
     *
     * @param id 目标ID（路径参数）
     * @return 目标的完整信息（含子目标列表）
     */
    @Operation(summary = "目标详情")
    @GetMapping("/{id}")
    public Result<GoalResponse> getById(@PathVariable Long id) {
        return Result.success(goalService.getById(id));
    }

    /**
     * 更新指定目标
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/goals/{id}
     *
     * @param id      目标ID
     * @param request 更新内容
     * @return 更新后的目标信息
     */
    @Operation(summary = "更新目标")
    @PutMapping("/{id}")
    public Result<GoalResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.update(id, request));
    }

    /**
     * 删除指定目标
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/goals/{id}
     *
     * @param id 要删除的目标ID
     * @return 删除成功
     *
     * 【注意】删除父目标时，需要考虑子目标的处理策略：
     * - 级联删除：同时删除所有子目标
     * - 提升子目标：将子目标变为独立目标
     * 具体策略由Service层实现
     */
    @Operation(summary = "删除目标")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goalService.delete(id);
        return Result.success();
    }

    /**
     * 更新目标进度
     *
     * 【HTTP方法】PUT - 更新目标的某个属性（进度）
     * 【URL路径】PUT /api/v1/goals/{id}/progress
     *
     * 【参数说明】
     * @param id        目标ID
     * @param progress  新的进度值（0-100的整数），通过查询参数传递
     *                  例如：?progress=75 表示目标完成了75%
     *
     * 【返回值】Result<Void> - 更新成功
     *
     * 【RESTful设计】
     * - /goals/{id}/progress 将"进度"视为目标的子资源
     * - 使用PUT方法更新进度值
     * - 进度通过查询参数传递而非请求体，因为只有一个简单数值
     *   这是一种简化设计：对于简单的更新操作，可以避免创建专门的DTO
     */
    @Operation(summary = "更新目标进度")
    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progress) { // progress通过查询参数传递，如 ?progress=75
        goalService.updateProgress(id, progress);
        return Result.success();
    }

    /**
     * 获取目标树形结构
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/goals/tree
     *
     * 【参数说明】
     * @param goalType 目标类型（可选），只获取特定层级的树形结构
     *
     * 【返回值】Result<List<GoalResponse>> - 树形结构的目标列表
     *           每个节点包含自身信息 + children子节点列表
     *
     * 【树形结构说明】
     * - 返回的数据是嵌套的JSON结构：
     *   [
     *     {
     *       "id": 1, "title": "五年规划", "children": [
     *         {"id": 2, "title": "2024年度目标", "children": [
     *           {"id": 3, "title": "1月目标", "children": []}
     *         ]}
     *       ]
     *     }
     *   ]
     * - 前端可以用树形组件（如Element UI的Tree）直接渲染
     *
     * 【与list接口的区别】
     * - GET /goals 返回扁平列表（所有目标平铺）
     * - GET /goals/tree 返回树形结构（父子关系嵌套）
     * 两种视图满足不同的前端展示需求
     */
    @Operation(summary = "目标树形结构（含子目标）")
    @GetMapping("/tree")
    public Result<List<GoalResponse>> getTree(
            @RequestParam(required = false) String goalType) {
        return Result.success(goalService.getTree(goalType));
    }

    /**
     * 获取目标的整体统计分析
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/goals/statistics
     *
     * 【返回值】Result<Map<String, Object>> - 目标统计数据
     *           可能包含：
     *           - totalGoals: 目标总数
     *           - completedGoals: 已完成目标数
     *           - averageProgress: 平均进度
     *           - byType: 按类型分布的统计
     *           - byCategory: 按分类分布的统计
     *           - overdueCount: 逾期目标数
     */
    @Operation(summary = "目标统计分析")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(goalService.getStatistics());
    }
}
