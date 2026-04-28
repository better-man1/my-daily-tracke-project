package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.GoalCreateRequest;
import com.dailytracker.dto.response.GoalResponse;
import com.dailytracker.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 目标管理控制器
 */
@Tag(name = "目标管理", description = "五年/年度/月度/周计划目标管理接口")
@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "创建目标")
    @PostMapping
    public Result<GoalResponse> create(@Valid @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.create(request));
    }

    @Operation(summary = "目标列表（支持类型/分类/状态筛选）")
    @GetMapping
    public Result<List<GoalResponse>> list(
            @RequestParam(required = false) String goalType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return Result.success(goalService.list(goalType, category, status));
    }

    @Operation(summary = "目标详情")
    @GetMapping("/{id}")
    public Result<GoalResponse> getById(@PathVariable Long id) {
        return Result.success(goalService.getById(id));
    }

    @Operation(summary = "更新目标")
    @PutMapping("/{id}")
    public Result<GoalResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody GoalCreateRequest request) {
        return Result.success(goalService.update(id, request));
    }

    @Operation(summary = "删除目标")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goalService.delete(id);
        return Result.success();
    }

    @Operation(summary = "更新目标进度")
    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(@PathVariable Long id,
                                       @RequestParam Integer progress) {
        goalService.updateProgress(id, progress);
        return Result.success();
    }

    @Operation(summary = "目标树形结构（含子目标）")
    @GetMapping("/tree")
    public Result<List<GoalResponse>> getTree(
            @RequestParam(required = false) String goalType) {
        return Result.success(goalService.getTree(goalType));
    }

    @Operation(summary = "目标统计分析")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(goalService.getStatistics());
    }
}
