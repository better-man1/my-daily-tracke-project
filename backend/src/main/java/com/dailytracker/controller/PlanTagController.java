package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.TagCreateRequest;
import com.dailytracker.dto.response.TagResponse;
import com.dailytracker.service.PlanTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计划标签控制器
 */
@Tag(name = "计划标签", description = "计划标签管理接口")
@RestController
@RequestMapping("/api/v1/plan-tags")
@RequiredArgsConstructor
public class PlanTagController {

    private final PlanTagService tagService;

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagResponse> create(@Valid @RequestBody TagCreateRequest request) {
        return Result.success(tagService.create(request));
    }

    @Operation(summary = "获取标签列表")
    @GetMapping
    public Result<List<TagResponse>> list() {
        return Result.success(tagService.list());
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TagCreateRequest request) {
        return Result.success(tagService.update(id, request));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }

    @Operation(summary = "为任务添加标签")
    @PostMapping("/plans/{planId}")
    public Result<Void> addTagsToPlan(
            @PathVariable Long planId,
            @RequestBody List<Long> tagIds) {
        tagService.addTagsToPlan(planId, tagIds);
        return Result.success();
    }

    @Operation(summary = "从任务移除标签")
    @DeleteMapping("/plans/{planId}")
    public Result<Void> removeTagsFromPlan(
            @PathVariable Long planId,
            @RequestBody List<Long> tagIds) {
        tagService.removeTagsFromPlan(planId, tagIds);
        return Result.success();
    }

    @Operation(summary = "获取任务的标签")
    @GetMapping("/plans/{planId}")
    public Result<List<TagResponse>> getPlanTags(@PathVariable Long planId) {
        return Result.success(tagService.getPlanTags(planId));
    }
}
