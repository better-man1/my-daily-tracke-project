package com.dailytracker.service;

import com.dailytracker.dto.request.ReminderSetRequest;

import java.util.List;

/**
 * 任务提醒服务接口（Plan Reminder Service Interface）
 *
 * 【职责说明】
 * 本接口负责任务提醒功能的业务逻辑，包括：
 * - 为任务设置提醒（支持多种提醒类型）
 * - 删除任务的提醒
 * - 查询任务的提醒信息
 * - 定时扫描并发送到期的提醒
 *
 * 【设计模式】
 * - 观察者模式（Observer Pattern）的变体：
 *   sendReminders 方法作为定时任务，定期检查是否有需要发送的提醒，
 *   当条件满足（时间到达）时触发通知动作。这是"轮询式"的事件检测。
 *   在更高级的实现中，可以使用消息队列（如 RabbitMQ）实现"事件驱动"的通知。
 *
 * - 内部类嵌套 DTO：
 *   ReminderInfo 作为内部类定义在接口中，因为它与接口紧密相关，
 *   且只在提醒相关的方法中使用。这种设计可以减少独立类的数量，提高内聚性。
 *
 * 【扩展说明】
 * 当前的提醒发送只是记录日志。在完整的实现中，应该集成实际的通知渠道：
 * - 邮件通知（JavaMail）
 * - 短信通知（阿里云短信 SDK）
 * - WebSocket 实时推送（前端在线时）
 * - 微信模板消息（小程序场景）
 * 可以通过引入 NotificationService 接口来抽象通知渠道。
 */
public interface PlanReminderService {

    /**
     * 设置提醒
     *
     * 【业务流程】
     * 1. 校验任务存在且属于当前用户
     * 2. 删除该任务的旧提醒（替换策略，每次只保留最新的提醒）
     * 3. 根据提醒类型和时间参数计算实际的提醒时间
     * 4. 创建新的提醒记录并保存
     *
     * @param planId  任务ID
     * @param request 提醒设置请求 DTO，包含提醒类型、自定义时间、提前分钟数等
     */
    void setReminder(Long planId, ReminderSetRequest request);

    /**
     * 删除提醒
     *
     * 删除指定任务的所有提醒记录。
     *
     * @param planId 任务ID
     */
    void deleteReminder(Long planId);

    /**
     * 获取任务的提醒信息
     *
     * @param planId 任务ID
     * @return 提醒信息列表，按提醒时间升序排列
     */
    List<ReminderInfo> getReminders(Long planId);

    /**
     * 定时任务：发送提醒
     *
     * 【执行机制】
     * 由 Spring 的 @Scheduled 注解驱动，每分钟执行一次。
     * 查询所有"未发送且提醒时间已到"的提醒记录，逐条处理：
     * 1. 获取关联的任务信息
     * 2. 执行实际的通知发送（当前为日志记录）
     * 3. 将提醒标记为已发送（isSent = true）
     *
     * 【容错设计】
     * 每条提醒的处理都包裹在 try-catch 中，
     * 单条提醒发送失败不会影响其他提醒的处理。
     */
    void sendReminders();

    /**
     * 提醒信息 DTO（内部类）
     *
     * 用于在 Service 层和 Controller 层之间传递提醒信息。
     * 包含提醒的核心字段：ID、类型、时间、发送状态。
     */
    class ReminderInfo {
        /** 提醒记录ID */
        private Long id;
        /** 提醒类型（如 START=任务开始时、DUE=任务截止时、CUSTOM=自定义时间） */
        private String reminderType;
        /** 提醒时间（格式化后的字符串，如 "2026-05-20 09:00"） */
        private String reminderTime;
        /** 是否已发送（true=已发送，false=未发送） */
        private Boolean isSent;

        /** 无参构造器（框架反射需要） */
        public ReminderInfo() {}

        /** 全参构造器（便于快速创建对象） */
        public ReminderInfo(Long id, String reminderType, String reminderTime, Boolean isSent) {
            this.id = id;
            this.reminderType = reminderType;
            this.reminderTime = reminderTime;
            this.isSent = isSent;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getReminderType() { return reminderType; }
        public void setReminderType(String reminderType) { this.reminderType = reminderType; }

        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

        public Boolean getIsSent() { return isSent; }
        public void setIsSent(Boolean isSent) { this.isSent = isSent; }
    }
}
