package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务提醒实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_plan_reminder")
public class PlanReminder extends BaseEntity {

    /** 任务ID */
    private Long planId;

    /** 用户ID */
    private Long userId;

    /** 提醒时间 */
    private LocalDateTime reminderTime;

    /** 提醒类型：START/DUE/CUSTOM */
    private String reminderType;

    /** 是否已发送 */
    private Boolean isSent;
}
