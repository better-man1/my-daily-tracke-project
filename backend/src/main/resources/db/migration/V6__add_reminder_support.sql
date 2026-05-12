-- V6: 添加提醒通知支持
-- 创建时间：2026-05-11

-- ==============================
-- 创建任务提醒表
-- ==============================
CREATE TABLE IF NOT EXISTS t_plan_reminder (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    plan_id         BIGINT       NOT NULL COMMENT '任务ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    reminder_time   DATETIME     NOT NULL COMMENT '提醒时间',
    reminder_type   VARCHAR(20)  NOT NULL COMMENT '提醒类型(START/DUE/CUSTOM)',
    is_sent         TINYINT(1)   DEFAULT 0 COMMENT '是否已发送',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_reminder_plan (plan_id),
    INDEX idx_reminder_user (user_id),
    INDEX idx_reminder_time (reminder_time),
    INDEX idx_reminder_sent (is_sent)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务提醒表';
