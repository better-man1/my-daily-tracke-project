package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据看板控制器
 */
@Tag(name = "数据看板", description = "多维度数据统计聚合接口")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "今日概览数据")
    @GetMapping("/today")
    public Result<Map<String, Object>> getToday() {
        return Result.success(dashboardService.getToday());
    }

    @Operation(summary = "本周统计数据")
    @GetMapping("/week")
    public Result<Map<String, Object>> getWeek() {
        return Result.success(dashboardService.getWeek());
    }

    @Operation(summary = "本月统计数据")
    @GetMapping("/month")
    public Result<Map<String, Object>> getMonth() {
        return Result.success(dashboardService.getMonth());
    }

    @Operation(summary = "年度统计数据")
    @GetMapping("/year")
    public Result<Map<String, Object>> getYear() {
        return Result.success(dashboardService.getYear());
    }
}
