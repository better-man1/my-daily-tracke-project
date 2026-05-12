-- V3: 添加重复任务支持
-- 创建时间：2026-05-11

-- ==============================
-- 为每日计划表添加重复任务字段
-- ==============================
ALTER TABLE t_daily_plan
ADD COLUMN repeat_type VARCHAR(20) DEFAULT NULL COMMENT '重复类型(NONE/DAILY/WEEKLY/MONTHLY/CUSTOM)',
ADD COLUMN repeat_pattern VARCHAR(200) DEFAULT NULL COMMENT '重复模式(每周一三等，JSON格式)',
ADD COLUMN repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期',
ADD INDEX idx_repeat (repeat_type, repeat_end_date);
