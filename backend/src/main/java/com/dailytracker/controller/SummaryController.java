package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.SummaryCreateRequest;
import com.dailytracker.dto.response.SummaryResponse;
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
 * 每日总结控制器
 */
@Tag(name = "每日总结", description = "每日复盘总结管理接口")
@RestController
@RequestMapping("/api/v1/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @Operation(summary = "创建总结")
    @PostMapping
    public Result<SummaryResponse> create(@Valid @RequestBody SummaryCreateRequest request) {
        return Result.success(summaryService.create(request));
    }

    @Operation(summary = "总结列表")
    @GetMapping
    public Result<List<SummaryResponse>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(summaryService.list(pageNum, pageSize, startDate, endDate));
    }

    @Operation(summary = "总结详情")
    @GetMapping("/{id}")
    public Result<SummaryResponse> getById(@PathVariable Long id) {
        return Result.success(summaryService.getById(id));
    }

    @Operation(summary = "更新总结")
    @PutMapping("/{id}")
    public Result<SummaryResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody SummaryCreateRequest request) {
        return Result.success(summaryService.update(id, request));
    }

    @Operation(summary = "删除总结")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        summaryService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取今日总结")
    @GetMapping("/today")
    public Result<SummaryResponse> getToday() {
        return Result.success(summaryService.getToday());
    }

    @Operation(summary = "连续记录天数")
    @GetMapping("/streak")
    public Result<Map<String, Object>> getStreak() {
        return Result.success(summaryService.getStreak());
    }

    @Operation(summary = "情绪趋势（近N天）")
    @GetMapping("/mood-trend")
    public Result<List<Map<String, Object>>> getMoodTrend(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(summaryService.getMoodTrend(days));
    }
}
