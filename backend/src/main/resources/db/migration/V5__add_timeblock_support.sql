-- V5: 添加时间块支持
-- 创建时间：2026-05-11

-- ==============================
-- 为每日计划表添加时间块字段
-- ==============================
ALTER TABLE t_daily_plan
ADD COLUMN start_time TIME DEFAULT NULL COMMENT '开始时间',
ADD COLUMN end_time TIME DEFAULT NULL COMMENT '结束时间',
ADD COLUMN is_timeblock TINYINT(1) DEFAULT 0 COMMENT '是否为时间块',
ADD INDEX idx_timeblock (plan_date, start_time, end_time);
