package com.dailytracker.controller;

import com.dailytracker.common.result.Result;
import com.dailytracker.dto.request.ReminderSetRequest;
import com.dailytracker.service.PlanReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务提醒控制器
 */
@Tag(name = "任务提醒", description = "任务提醒管理接口")
@RestController
@RequestMapping("/api/v1/plan-reminders")
@RequiredArgsConstructor
public class PlanReminderController {

    private final PlanReminderService reminderService;

    @Operation(summary = "设置提醒")
    @PostMapping("/{planId}")
    public Result<Void> setReminder(
            @PathVariable Long planId,
            @Valid @RequestBody ReminderSetRequest request) {
        reminderService.setReminder(planId, request);
        return Result.success();
    }

    @Operation(summary = "删除提醒")
    @DeleteMapping("/{planId}")
    public Result<Void> deleteReminder(@PathVariable Long planId) {
        reminderService.deleteReminder(planId);
        return Result.success();
    }

    @Operation(summary = "获取任务的提醒信息")
    @GetMapping("/{planId}")
    public Result<List<PlanReminderService.ReminderInfo>> getReminders(@PathVariable Long planId) {
        return Result.success(reminderService.getReminders(planId));
    }
}
