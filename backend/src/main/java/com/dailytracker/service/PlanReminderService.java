package com.dailytracker.service;

import com.dailytracker.dto.request.ReminderSetRequest;

import java.util.List;

/**
 * 任务提醒服务接口
 */
public interface PlanReminderService {

    /**
     * 设置提醒
     *
     * @param planId  任务ID
     * @param request 提醒设置请求
     */
    void setReminder(Long planId, ReminderSetRequest request);

    /**
     * 删除提醒
     *
     * @param planId 任务ID
     */
    void deleteReminder(Long planId);

    /**
     * 获取任务的提醒信息
     *
     * @param planId 任务ID
     * @return 提醒信息
     */
    List<ReminderInfo> getReminders(Long planId);

    /**
     * 定时任务：发送提醒
     */
    void sendReminders();

    /**
     * 提醒信息
     */
    class ReminderInfo {
        private Long id;
        private String reminderType;
        private String reminderTime;
        private Boolean isSent;

        public ReminderInfo() {}

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
