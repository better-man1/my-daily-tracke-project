package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// TagCreateRequest: 创建/更新标签请求DTO，包含标签名称、颜色等字段
import com.dailytracker.dto.request.TagCreateRequest;
// TagResponse: 标签响应DTO，包含标签的完整信息
import com.dailytracker.dto.response.TagResponse;
// PlanTagService: 计划标签业务逻辑层接口
import com.dailytracker.service.PlanTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计划标签控制器（PlanTagController）
 *
 * 【职责说明】
 * 本控制器负责管理每日计划的标签功能，包括：
 *   1. 标签的CRUD操作（创建、查询、更新、删除）
 *   2. 为任务添加标签（多对多关联）
 *   3. 从任务移除标签
 *   4. 查询任务的标签列表
 *
 * 【RESTful设计思想 - 多对多关联的资源管理】
 * 标签和任务之间是多对多关系：
 *   - 一个标签可以关联多个任务
 *   - 一个任务可以有多个标签
 * 多对多关联通常通过中间表实现，对应的API设计：
 *   POST   /plan-tags/plans/{planId}  -> 为任务添加标签（在中间表中创建关联记录）
 *   DELETE /plan-tags/plans/{planId}  -> 从任务移除标签（从中间表中删除关联记录）
 *   GET    /plan-tags/plans/{planId}  -> 获取任务的标签（查询中间表）
 *
 * 【API路径结构】
 *   /api/v1/plan-tags              -> 标签自身的CRUD
 *   /api/v1/plan-tags/plans/{planId} -> 标签与任务的关联操作
 *
 * 【与ExcerptController中标签管理的区别】
 *   - ExcerptController中的 /excerpts/tags 是摘录模块的专用标签
 *   - 本Controller中的 /plan-tags 是计划模块的专用标签
 *   - 两套标签独立管理，互不影响（不同的数据库表）
 *
 * 【API路径前缀】/api/v1/plan-tags
 */
@Tag(name = "计划标签", description = "计划标签管理接口")
@RestController
@RequestMapping("/api/v1/plan-tags")
@RequiredArgsConstructor
public class PlanTagController {

    private final PlanTagService tagService;

    // ==================== 标签CRUD ====================

    /**
     * 创建新标签
     *
     * 【HTTP方法】POST
     * 【URL路径】POST /api/v1/plan-tags
     *
     * 【参数说明】
     * @param request 标签创建请求DTO，包含：
     *                - name: 标签名称（必填），如"重要"、"紧急"、"学习"
     *                - color: 标签颜色（可选），如"#FF0000"，用于前端展示
     *
     * 【返回值】Result<TagResponse> - 创建的标签信息
     */
    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagResponse> create(@Valid @RequestBody TagCreateRequest request) {
        return Result.success(tagService.create(request));
    }

    /**
     * 获取所有标签列表
     *
     * 【HTTP方法】GET
     * 【URL路径】GET /api/v1/plan-tags
     *
     * 【返回值】Result<List<TagResponse>> - 所有标签的列表
     *
     * 【设计说明】
     * - 不分页：标签数量通常不多（一般不超过50个），全量返回
     * - 返回的标签列表用于前端的标签选择器、标签筛选器等组件
     */
    @Operation(summary = "获取标签列表")
    @GetMapping
    public Result<List<TagResponse>> list() {
        return Result.success(tagService.list());
    }

    /**
     * 更新标签信息
     *
     * 【HTTP方法】PUT
     * 【URL路径】PUT /api/v1/plan-tags/{id}
     *
     * @param id      标签ID
     * @param request 更新内容
     * @return 更新后的标签信息
     */
    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TagCreateRequest request) {
        return Result.success(tagService.update(id, request));
    }

    /**
     * 删除标签
     *
     * 【HTTP方法】DELETE
     * 【URL路径】DELETE /api/v1/plan-tags/{id}
     *
     * @param id 要删除的标签ID
     * @return 删除成功
     *
     * 【注意】删除标签时，通常会同时清除中间表中该标签与所有任务的关联记录
     */
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }

    // ==================== 标签与任务的关联操作 ====================

    /**
     * 为指定任务添加标签（建立关联）
     *
     * 【HTTP方法】POST - 创建关联记录
     * 【URL路径】POST /api/v1/plan-tags/plans/{planId}
     *
     * 【参数说明】
     * @param planId 任务ID（路径参数）
     * @param tagIds 要添加的标签ID列表（请求体）
     *               例如：[1, 3, 5] 表示为该任务添加ID为1、3、5的标签
     *
     * 【返回值】Result<Void> - 添加成功
     *
     * 【RESTful设计 - 关联资源管理】
     * - POST /plan-tags/plans/{planId} 表示"为任务{planId}添加标签关联"
     * - 请求体是标签ID列表（List<Long>），而非DTO，简化设计
     * - Service层会：1.校验标签ID是否存在 2.去重（避免重复关联） 3.在中间表插入记录
     */
    @Operation(summary = "为任务添加标签")
    @PostMapping("/plans/{planId}") // 路径层级：标签 -> 计划 -> 具体计划ID
    public Result<Void> addTagsToPlan(
            @PathVariable Long planId,
            // @RequestBody List<Long>: 直接接收JSON数组，如 [1, 2, 3]
            // 无需额外的DTO包装类，简洁直观
            @RequestBody List<Long> tagIds) {
        // 在plan_tag中间表中插入关联记录
        tagService.addTagsToPlan(planId, tagIds);
        return Result.success();
    }

    /**
     * 从指定任务移除标签（删除关联）
     *
     * 【HTTP方法】DELETE - 删除关联记录
     * 【URL路径】DELETE /api/v1/plan-tags/plans/{planId}
     *
     * 【参数说明】
     * @param planId 任务ID
     * @param tagIds 要移除的标签ID列表（请求体）
     *
     * 【返回值】Result<Void> - 移除成功
     *
     * 【设计说明】
     * - DELETE请求通常不带请求体（HTTP规范中DELETE的请求体语义不明确）
     * - 但这里需要传递标签ID列表，使用请求体是最简便的方式
     * - 这是一种务实的API设计，虽然不完全符合REST教条，但实际使用很普遍
     * - 替代方案：DELETE /plans/{planId}/tags?tagIds=1,2,3（使用查询参数）
     */
    @Operation(summary = "从任务移除标签")
    @DeleteMapping("/plans/{planId}")
    public Result<Void> removeTagsFromPlan(
            @PathVariable Long planId,
            @RequestBody List<Long> tagIds) {
        // 从plan_tag中间表中删除对应的关联记录
        tagService.removeTagsFromPlan(planId, tagIds);
        return Result.success();
    }

    /**
     * 获取指定任务的所有标签
     *
     * 【HTTP方法】GET - 查询关联
     * 【URL路径】GET /api/v1/plan-tags/plans/{planId}
     *
     * 【参数说明】@param planId 任务ID
     * 【返回值】Result<List<TagResponse>> - 该任务关联的所有标签列表
     *
     * 【RESTful路径复用说明】
     * 同一个路径 /plan-tags/plans/{planId}，三种HTTP方法三种操作：
     *   POST   -> 添加标签（创建关联）
     *   DELETE -> 移除标签（删除关联）
     *   GET    -> 查询标签（查询关联）
     * 这是RESTful中"相同路径，不同方法"设计理念的标准应用
     */
    @Operation(summary = "获取任务的标签")
    @GetMapping("/plans/{planId}")
    public Result<List<TagResponse>> getPlanTags(@PathVariable Long planId) {
        // 查询plan_tag中间表，返回该任务关联的所有标签详细信息
        return Result.success(tagService.getPlanTags(planId));
    }
}
