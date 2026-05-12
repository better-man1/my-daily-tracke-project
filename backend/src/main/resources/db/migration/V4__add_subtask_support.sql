-- V4: 添加子任务支持
-- 创建时间：2026-05-11

-- ==============================
-- 为每日计划表添加子任务字段
-- ==============================
ALTER TABLE t_daily_plan
ADD COLUMN parent_id BIGINT DEFAULT NULL COMMENT '父任务ID',
ADD COLUMN subtask_count INT DEFAULT 0 COMMENT '子任务数量',
ADD COLUMN completed_subtask_count INT DEFAULT 0 COMMENT '已完成子任务数量',
ADD INDEX idx_plan_parent (parent_id);
