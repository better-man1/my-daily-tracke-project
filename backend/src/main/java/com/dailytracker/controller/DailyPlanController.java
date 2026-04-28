package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.response.PlanResponse;
import com.dailytracker.service.DailyPlanService;
import io.swagger.v3.oas.annotations.Operation;
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
 * 每日计划控制器
 */
@Tag(name = "每日计划", description = "每日任务计划管理接口")
@RestController
@RequestMapping("/api/v1/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

    @Operation(summary = "创建任务")
    @PostMapping
    public Result<PlanResponse> create(@Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.create(request));
    }

    @Operation(summary = "任务列表（按日期）")
    @GetMapping
    public Result<List<PlanResponse>> list(
            @Parameter(description = "日期，格式：yyyy-MM-dd，默认今天")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.listByDate(planDate));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<PlanResponse> getById(@PathVariable Long id) {
        return Result.success(dailyPlanService.getById(id));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public Result<PlanResponse> update(@PathVariable Long id,
                                       @Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.update(id, request));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dailyPlanService.delete(id);
        return Result.success();
    }

    @Operation(summary = "更新任务状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status) {
        dailyPlanService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "批量更新排序")
    @PutMapping("/batch-sort")
    public Result<Void> batchSort(@RequestBody Map<Long, Integer> sortMap) {
        dailyPlanService.batchUpdateSort(sortMap);
        return Result.success();
    }

    @Operation(summary = "任务顺延至下一天")
    @PostMapping("/{id}/postpone")
    public Result<PlanResponse> postpone(@PathVariable Long id) {
        return Result.success(dailyPlanService.postpone(id));
    }

    @Operation(summary = "任务统计（按日期）")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.getStatistics(planDate));
    }

    @Operation(summary = "保存为模板")
    @PostMapping("/{id}/templates")
    public Result<Void> saveAsTemplate(@PathVariable Long id,
                                       @RequestParam String templateName) {
        dailyPlanService.saveAsTemplate(id, templateName);
        return Result.success();
    }

    @Operation(summary = "模板列表")
    @GetMapping("/templates")
    public Result<List<PlanResponse>> listTemplates() {
        return Result.success(dailyPlanService.listTemplates());
    }
}
