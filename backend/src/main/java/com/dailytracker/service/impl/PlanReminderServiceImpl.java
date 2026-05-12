package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.ReminderSetRequest;
import com.dailytracker.entity.DailyPlan;
import com.dailytracker.entity.PlanReminder;
import com.dailytracker.mapper.DailyPlanMapper;
import com.dailytracker.mapper.PlanReminderMapper;
import com.dailytracker.service.PlanReminderService;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务提醒服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanReminderServiceImpl implements PlanReminderService {

    private final PlanReminderMapper reminderMapper;
    private final DailyPlanMapper planMapper;

    @Override
    @Transactional
    public void setReminder(Long planId, ReminderSetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 验证任务存在且属于当前用户
        DailyPlan plan = planMapper.selectOne(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getId, planId)
                        .eq(DailyPlan::getUserId, userId)
        );
        if (plan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 删除旧提醒
        reminderMapper.delete(new LambdaQueryWrapper<PlanReminder>().eq(PlanReminder::getPlanId, planId));

        // 计算提醒时间
        LocalDateTime reminderTime = calculateReminderTime(plan, request);

        // 创建新提醒
        PlanReminder reminder = new PlanReminder();
        reminder.setPlanId(planId);
        reminder.setUserId(userId);
        reminder.setReminderType(request.getReminderType());
        reminder.setReminderTime(reminderTime);
        reminder.setIsSent(false);
        reminderMapper.insert(reminder);

        log.info("设置提醒: planId={}, type={}, time={}", planId, request.getReminderType(), reminderTime);
    }

    @Override
    @Transactional
    public void deleteReminder(Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        reminderMapper.delete(
                new LambdaQueryWrapper<PlanReminder>()
                        .eq(PlanReminder::getPlanId, planId)
                        .eq(PlanReminder::getUserId, userId)
        );
        log.info("删除提醒: planId={}", planId);
    }

    @Override
    public List<ReminderInfo> getReminders(Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<PlanReminder> reminders = reminderMapper.selectList(
                new LambdaQueryWrapper<PlanReminder>()
                        .eq(PlanReminder::getPlanId, planId)
                        .eq(PlanReminder::getUserId, userId)
                        .orderByAsc(PlanReminder::getReminderTime)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return reminders.stream()
                .map(r -> new ReminderInfo(
                        r.getId(),
                        r.getReminderType(),
                        r.getReminderTime().format(formatter),
                        r.getIsSent()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendReminders() {
        // 查询需要发送的提醒（未发送且时间已到）
        List<PlanReminder> pendingReminders = reminderMapper.selectList(
                new LambdaQueryWrapper<PlanReminder>()
                        .eq(PlanReminder::getIsSent, false)
                        .le(PlanReminder::getReminderTime, LocalDateTime.now())
        );

        for (PlanReminder reminder : pendingReminders) {
            try {
                // 获取任务信息
                DailyPlan plan = planMapper.selectById(reminder.getPlanId());
                if (plan == null) continue;

                // 这里应该调用实际的通知服务（如邮件、短信、WebSocket推送等）
                // 暂时只记录日志
                log.info("发送提醒: userId={}, planId={}, title={}, type={}",
                        reminder.getUserId(),
                        plan.getId(),
                        plan.getTitle(),
                        reminder.getReminderType()
                );

                // 标记为已发送
                reminder.setIsSent(true);
                reminderMapper.updateById(reminder);
            } catch (Exception e) {
                log.error("发送提醒失败: reminderId={}", reminder.getId(), e);
            }
        }
    }

    /**
     * 计算提醒时间
     */
    private LocalDateTime calculateReminderTime(DailyPlan plan, ReminderSetRequest request) {
        if ("CUSTOM".equals(request.getReminderType())) {
            if (request.getReminderTime() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "自定义提醒需要指定时间");
            }
            return request.getReminderTime();
        }

        LocalDateTime baseTime = null;
        if ("START".equals(request.getReminderType())) {
            // 任务开始时间提醒
            if (plan.getStartTime() != null) {
                baseTime = LocalDateTime.of(plan.getPlanDate(), plan.getStartTime());
            } else {
                // 如果没有设置开始时间，使用计划日期的早上9点
                baseTime = plan.getPlanDate().atTime(9, 0);
            }
        } else if ("DUE".equals(request.getReminderType())) {
            // 任务截止时间提醒
            if (plan.getEndTime() != null) {
                baseTime = LocalDateTime.of(plan.getPlanDate(), plan.getEndTime());
            } else {
                // 如果没有设置结束时间，使用计划日期的晚上6点
                baseTime = plan.getPlanDate().atTime(18, 0);
            }
        }

        if (baseTime == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无法确定提醒时间");
        }

        // 减去提前提醒的分钟数
        int advanceMinutes = request.getAdvanceMinutes() != null ? request.getAdvanceMinutes() : 0;
        return baseTime.minusMinutes(advanceMinutes);
    }
}
