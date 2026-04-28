package com.dailytracker.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.AccountingCreateRequest;
import com.dailytracker.dto.request.BudgetCreateRequest;
import com.dailytracker.dto.response.AccountingResponse;
import com.dailytracker.dto.response.AccountingStatsResponse;
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
 * 每日记账控制器
 */
@Tag(name = "每日记账", description = "收支记账管理接口")
@RestController
@RequestMapping("/api/v1/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    @Operation(summary = "创建账目")
    @PostMapping
    public Result<AccountingResponse> create(@Valid @RequestBody AccountingCreateRequest request) {
        return Result.success(accountingService.create(request));
    }

    @Operation(summary = "账目列表（日期 + 类型筛选）")
    @GetMapping
    public Result<IPage<AccountingResponse>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {
        return Result.success(accountingService.page(pageNum, pageSize, startDate, endDate, type));
    }

    @Operation(summary = "账目详情")
    @GetMapping("/{id}")
    public Result<AccountingResponse> getById(@PathVariable Long id) {
        return Result.success(accountingService.getById(id));
    }

    @Operation(summary = "更新账目")
    @PutMapping("/{id}")
    public Result<AccountingResponse> update(@PathVariable Long id,
                                             @Valid @RequestBody AccountingCreateRequest request) {
        return Result.success(accountingService.update(id, request));
    }

    @Operation(summary = "删除账目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        accountingService.delete(id);
        return Result.success();
    }

    @Operation(summary = "日统计")
    @GetMapping("/statistics/daily")
    public Result<AccountingStatsResponse> dailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(accountingService.dailyStats(date));
    }

    @Operation(summary = "月统计")
    @GetMapping("/statistics/monthly")
    public Result<AccountingStatsResponse> monthlyStats(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(accountingService.monthlyStats(year, month));
    }

    @Operation(summary = "年统计")
    @GetMapping("/statistics/yearly")
    public Result<AccountingStatsResponse> yearlyStats(@RequestParam int year) {
        return Result.success(accountingService.yearlyStats(year));
    }

    @Operation(summary = "分类统计")
    @GetMapping("/statistics/category")
    public Result<List<AccountingStatsResponse.CategoryStat>> categoryStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {
        return Result.success(accountingService.categoryStats(startDate, endDate, type));
    }

    @Operation(summary = "设置预算")
    @PostMapping("/budget")
    public Result<Void> setBudget(@Valid @RequestBody BudgetCreateRequest request) {
        accountingService.setBudget(request);
        return Result.success();
    }

    @Operation(summary = "获取预算及执行情况")
    @GetMapping("/budget")
    public Result<Map<String, Object>> getBudget(
            @RequestParam int year,
            @RequestParam int month) {
        return Result.success(accountingService.getBudget(year, month));
    }

    @Operation(summary = "获取记账分类树")
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories(
            @RequestParam(required = false) String type) {
        return Result.success(accountingService.getCategories(type));
    }
}
