-- V7: 添加标签系统支持
-- 创建时间：2026-05-11

-- ==============================
-- 创建标签表
-- ==============================
CREATE TABLE IF NOT EXISTS t_plan_tag (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    name        VARCHAR(50)  NOT NULL COMMENT '标签名称',
    color       VARCHAR(20)  DEFAULT '#6366f1' COMMENT '标签颜色',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_tag_user_name (user_id, name),
    INDEX idx_tag_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划标签表';

-- ==============================
-- 创建标签关联表
-- ==============================
CREATE TABLE IF NOT EXISTS t_plan_tag_relation (
    plan_id     BIGINT NOT NULL COMMENT '任务ID',
    tag_id      BIGINT NOT NULL COMMENT '标签ID',

    PRIMARY KEY (plan_id, tag_id),
    INDEX idx_relation_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划标签关联表';
