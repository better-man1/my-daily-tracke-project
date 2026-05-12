package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.PlanBatchUpdateRequest;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.request.RepeatUpdateRequest;
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

    @Operation(summary = "生成重复任务实例")
    @PostMapping("/{id}/repeat/instances")
    public Result<List<PlanResponse>> generateRepeatInstances(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.generateRepeatInstances(id, startDate, endDate));
    }

    @Operation(summary = "更新重复规则")
    @PutMapping("/{id}/repeat")
    public Result<PlanResponse> updateRepeatRule(
            @PathVariable Long id,
            @Valid @RequestBody RepeatUpdateRequest request) {
        return Result.success(dailyPlanService.updateRepeatRule(id, request));
    }

    @Operation(summary = "停止重复")
    @DeleteMapping("/{id}/repeat")
    public Result<Void> stopRepeat(@PathVariable Long id) {
        dailyPlanService.stopRepeat(id);
        return Result.success();
    }

    @Operation(summary = "创建子任务")
    @PostMapping("/{parentId}/subtasks")
    public Result<PlanResponse> createSubtask(
            @PathVariable Long parentId,
            @Valid @RequestBody PlanCreateRequest request) {
        return Result.success(dailyPlanService.createSubtask(parentId, request));
    }

    @Operation(summary = "获取子任务列表")
    @GetMapping("/{parentId}/subtasks")
    public Result<List<PlanResponse>> getSubtasks(@PathVariable Long parentId) {
        return Result.success(dailyPlanService.getSubtasks(parentId));
    }

    @Operation(summary = "更新子任务状态")
    @PutMapping("/subtasks/{id}/status")
    public Result<Void> updateSubtaskStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        dailyPlanService.updateSubtaskStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "子任务转为主任务")
    @PostMapping("/subtasks/{id}/convert")
    public Result<Void> convertToMainTask(@PathVariable Long id) {
        dailyPlanService.convertToMainTask(id);
        return Result.success();
    }

    @Operation(summary = "完成率趋势")
    @GetMapping("/analytics/trend")
    public Result<List<Map<String, Object>>> getCompletionTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getCompletionTrend(startDate, endDate));
    }

    @Operation(summary = "分类分布")
    @GetMapping("/analytics/category")
    public Result<Map<String, Object>> getCategoryDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getCategoryDistribution(startDate, endDate));
    }

    @Operation(summary = "优先级分布")
    @GetMapping("/analytics/priority")
    public Result<Map<String, Object>> getPriorityDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getPriorityDistribution(startDate, endDate));
    }

    @Operation(summary = "时间分配统计")
    @GetMapping("/analytics/time")
    public Result<Map<String, Object>> getTimeDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(dailyPlanService.getTimeDistribution(startDate, endDate));
    }

    @Operation(summary = "检测时间冲突")
    @GetMapping("/timeblock/conflicts")
    public Result<List<PlanResponse>> detectTimeConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.detectTimeConflicts(planDate));
    }

    @Operation(summary = "获取时间块列表")
    @GetMapping("/timeblock")
    public Result<List<PlanResponse>> getTimeBlocks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate) {
        return Result.success(dailyPlanService.getTimeBlocks(planDate));
    }

    @Operation(summary = "批量更新任务")
    @PutMapping("/batch-update")
    public Result<Void> batchUpdate(@RequestBody PlanBatchUpdateRequest request) {
        dailyPlanService.batchUpdate(
                request.getIds(),
                request.getPriority(),
                request.getCategory(),
                request.getStatus()
        );
        return Result.success();
    }

    @Operation(summary = "批量删除任务")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        dailyPlanService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "批量顺延任务")
    @PutMapping("/batch-postpone")
    public Result<Void> batchPostpone(@RequestBody List<Long> ids) {
        dailyPlanService.batchPostpone(ids);
        return Result.success();
    }

    @Operation(summary = "批量完成任务")
    @PutMapping("/batch-complete")
    public Result<Void> batchComplete(@RequestBody List<Long> ids) {
        dailyPlanService.batchComplete(ids);
        return Result.success();
    }
}
