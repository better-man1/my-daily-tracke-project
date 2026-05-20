package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
import com.dailytracker.common.result.Result;
// ReminderSetRequest: 设置提醒请求DTO，包含提醒时间、提醒方式等字段
import com.dailytracker.dto.request.ReminderSetRequest;
// PlanReminderService: 任务提醒业务逻辑层接口
import com.dailytracker.service.PlanReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// List: Java集合接口，用于返回多个提醒信息的列表
import java.util.List;

/**
 * 任务提醒控制器（PlanReminderController）
 *
 * 【职责说明】
 * 本控制器负责管理任务（计划）的提醒功能，包括：
 *   1. 为指定任务设置提醒（支持多个提醒时间）
 *   2. 删除指定任务的所有提醒
 *   3. 查询指定任务的提醒信息列表
 *
 * 【RESTful设计思想 - 关联资源的路径设计】
 * 提醒（Reminder）是任务（Plan）的附属资源，没有独立存在的意义：
 *   - 路径设计：/api/v1/plan-reminders/{planId}
 *   - 所有操作都围绕 planId（任务ID）进行
 *   - 提醒不能脱离任务单独存在
 *
 * 【提醒机制设计说明】
 * 提醒功能通常涉及多个组件的协作：
 *   1. 用户设置提醒时间和方式（通过本接口）
 *   2. 系统将提醒信息存储到数据库
 *   3. 定时任务（如使用@Scheduled或Quartz）定期扫描即将到期的提醒
 *   4. 到达提醒时间时，通过通知渠道（APP推送、邮件、短信等）发送提醒
 *   5. 微信小程序通常使用订阅消息或模板消息实现提醒推送
 *
 * 【API路径前缀】/api/v1/plan-reminders
 */
@Tag(name = "任务提醒", description = "任务提醒管理接口")
@RestController
@RequestMapping("/api/v1/plan-reminders")
@RequiredArgsConstructor
public class PlanReminderController {

    private final PlanReminderService reminderService;

    /**
     * 为指定任务设置提醒
     *
     * 【HTTP方法】POST - 创建新的提醒资源
     * 【URL路径】POST /api/v1/plan-reminders/{planId}
     *
     * 【参数说明】
     * @param planId  任务ID（路径参数），指定为哪个任务设置提醒
     * @param request 提醒设置请求DTO，包含：
     *                - reminderTime: 提醒时间（如任务开始前15分钟、30分钟、1小时等）
     *                - reminderType: 提醒方式（如APP推送、微信消息、邮件等）
     *                - enabled: 是否启用
     *                @Valid 触发校验：确保提醒时间不为空
     *
     * 【返回值】Result<Void> - 设置成功
     *
     * 【设计说明】
     * - 一个任务可以设置多个提醒（如提前15分钟和提前1小时各提醒一次）
     * - 如果已存在提醒，此接口可以更新或新增提醒（取决于Service层实现）
     */
    @Operation(summary = "设置提醒")
    @PostMapping("/{planId}") // 路径中的{planId}指定了提醒关联的任务
    public Result<Void> setReminder(
            // @PathVariable: 从URL路径中提取planId参数
            @PathVariable Long planId,
            @Valid @RequestBody ReminderSetRequest request) {
        // 调用Service层保存提醒设置
        reminderService.setReminder(planId, request);
        return Result.success();
    }

    /**
     * 删除指定任务的所有提醒
     *
     * 【HTTP方法】DELETE - 删除提醒资源
     * 【URL路径】DELETE /api/v1/plan-reminders/{planId}
     *
     * 【参数说明】@param planId 任务ID
     * 【返回值】Result<Void> - 删除成功
     *
     * 【设计说明】
     * - 删除某个任务的所有提醒，而非单个提醒
     * - 如果需要删除单个提醒，可以设计 DELETE /plan-reminders/{planId}/{reminderId}
     * - 这里采用简化设计：按任务维度管理提醒，不提供单个提醒的精细管理
     */
    @Operation(summary = "删除提醒")
    @DeleteMapping("/{planId}")
    public Result<Void> deleteReminder(@PathVariable Long planId) {
        // 删除该任务关联的所有提醒记录
        reminderService.deleteReminder(planId);
        return Result.success();
    }

    /**
     * 获取指定任务的提醒信息列表
     *
     * 【HTTP方法】GET - 查询提醒资源
     * 【URL路径】GET /api/v1/plan-reminders/{planId}
     *
     * 【参数说明】@param planId 任务ID
     * 【返回值】Result<List<PlanReminderService.ReminderInfo>> - 该任务的所有提醒信息列表
     *
     * 【设计说明】
     * - 返回类型使用Service内部类 ReminderInfo：这是Service层定义的内部数据结构
     *   包含提醒时间、提醒方式、是否已触发等信息
     * - 更规范的做法是将ReminderInfo提取为独立的DTO类
     * - 返回List：因为一个任务可以有多个提醒
     *
     * 【注意】
     * - 使用PlanReminderService.ReminderInfo（Service内部类）作为返回类型
     *   这是一种简化设计，更好的做法是定义独立的ReminderResponse DTO
     *   这样Controller层的返回类型不直接依赖Service层的内部类
     */
    @Operation(summary = "获取任务的提醒信息")
    @GetMapping("/{planId}")
    public Result<List<PlanReminderService.ReminderInfo>> getReminders(@PathVariable Long planId) {
        return Result.success(reminderService.getReminders(planId));
    }
}
