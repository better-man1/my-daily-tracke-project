-- V1: 初始化数据库表结构
-- 创建时间：2026-04-28

-- ==============================
-- 用户表
-- ==============================
CREATE TABLE IF NOT EXISTS t_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL COMMENT '用户名',
    password      VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    nickname      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar        VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    email         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    wx_openid     VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
    wx_unionid    VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
    signature     VARCHAR(200) DEFAULT NULL COMMENT '个人签名',
    status        TINYINT      DEFAULT 1 COMMENT '状态(1=正常,0=禁用)',
    last_login_at DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(1)   DEFAULT 0,
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_wx_openid (wx_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ==============================
-- 用户偏好设置表
-- ==============================
CREATE TABLE IF NOT EXISTS t_user_settings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    theme           VARCHAR(20)  DEFAULT 'light' COMMENT '主题(light/dark)',
    default_view    VARCHAR(20)  DEFAULT 'dashboard' COMMENT '默认首页视图',
    reminder_time   TIME         DEFAULT '21:00:00' COMMENT '每日提醒时间',
    week_start_day  TINYINT      DEFAULT 1 COMMENT '周起始日(1=周一,7=周日)',
    language        VARCHAR(10)  DEFAULT 'zh-CN' COMMENT '语言',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_settings_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好设置表';

-- ==============================
-- 目标计划表
-- ==============================
CREATE TABLE IF NOT EXISTS t_goal_plan (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    parent_id     BIGINT       DEFAULT NULL COMMENT '父目标ID',
    title         VARCHAR(200) NOT NULL COMMENT '目标标题',
    description   TEXT         DEFAULT NULL COMMENT '目标描述',
    goal_type     VARCHAR(20)  NOT NULL COMMENT '目标类型(FIVE_YEAR/YEARLY/MONTHLY/WEEKLY)',
    category      VARCHAR(20)  DEFAULT 'OTHER' COMMENT '分类(CAREER/STUDY/HEALTH/FINANCE/LIFE/OTHER)',
    start_date    DATE         NOT NULL COMMENT '开始日期',
    end_date      DATE         NOT NULL COMMENT '截止日期',
    progress      INT          DEFAULT 0 COMMENT '进度(0~100)',
    status        VARCHAR(20)  DEFAULT 'NOT_STARTED' COMMENT '状态(NOT_STARTED/IN_PROGRESS/COMPLETED/ABANDONED)',
    priority      INT          DEFAULT 0 COMMENT '优先级',
    sort_order    INT          DEFAULT 0 COMMENT '排序',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(1)   DEFAULT 0,
    INDEX idx_goal_user_type (user_id, goal_type),
    INDEX idx_goal_user_status (user_id, status),
    INDEX idx_goal_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标计划表';

-- ==============================
-- 目标关键结果表
-- ==============================
CREATE TABLE IF NOT EXISTS t_goal_kr (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id       BIGINT       NOT NULL COMMENT '目标ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    title         VARCHAR(200) NOT NULL COMMENT 'KR标题',
    target_value  DECIMAL(10,2) DEFAULT NULL COMMENT '目标值',
    current_value DECIMAL(10,2) DEFAULT 0 COMMENT '当前值',
    unit          VARCHAR(20)  DEFAULT NULL COMMENT '单位',
    progress      INT          DEFAULT 0 COMMENT '进度(0~100)',
    sort_order    INT          DEFAULT 0 COMMENT '排序',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_kr_goal (goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='目标关键结果表';

-- ==============================
-- 每日计划表
-- ==============================
CREATE TABLE IF NOT EXISTS t_daily_plan (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    goal_id         BIGINT       DEFAULT NULL COMMENT '关联目标ID',
    title           VARCHAR(200) NOT NULL COMMENT '任务标题',
    description     TEXT         DEFAULT NULL COMMENT '任务描述',
    plan_date       DATE         NOT NULL COMMENT '计划日期',
    priority        VARCHAR(5)   DEFAULT 'P2' COMMENT '优先级(P0/P1/P2/P3)',
    category        VARCHAR(20)  DEFAULT 'WORK' COMMENT '分类(WORK/STUDY/LIFE/HEALTH)',
    estimated_mins  INT          DEFAULT NULL COMMENT '预估时间(分钟)',
    actual_mins     INT          DEFAULT NULL COMMENT '实际时间(分钟)',
    status          VARCHAR(20)  DEFAULT 'TODO' COMMENT '状态(TODO/IN_PROGRESS/DONE/CANCELLED)',
    sort_order      INT          DEFAULT 0 COMMENT '排序权重',
    completed_at    DATETIME     DEFAULT NULL COMMENT '完成时间',
    is_template     TINYINT(1)   DEFAULT 0 COMMENT '是否为模板',
    template_name   VARCHAR(100) DEFAULT NULL COMMENT '模板名称',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)   DEFAULT 0,
    INDEX idx_plan_user_date (user_id, plan_date),
    INDEX idx_plan_user_status (user_id, status),
    INDEX idx_plan_goal (goal_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日计划表';

-- ==============================
-- 每日摘录表
-- ==============================
CREATE TABLE IF NOT EXISTS t_excerpt (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    content       TEXT         NOT NULL COMMENT '摘录内容',
    source_type   VARCHAR(20)  DEFAULT 'OTHER' COMMENT '来源类型(BOOK/ARTICLE/VIDEO/PODCAST/OTHER)',
    source_title  VARCHAR(200) DEFAULT NULL COMMENT '来源标题',
    source_url    VARCHAR(500) DEFAULT NULL COMMENT '来源链接',
    thought       TEXT         DEFAULT NULL COMMENT '个人感悟',
    images        JSON         DEFAULT NULL COMMENT '图片URL列表',
    is_favorite   TINYINT(1)   DEFAULT 0 COMMENT '是否收藏',
    excerpt_date  DATE         NOT NULL COMMENT '摘录日期',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(1)   DEFAULT 0,
    INDEX idx_excerpt_user_date (user_id, excerpt_date),
    INDEX idx_excerpt_user_fav (user_id, is_favorite),
    FULLTEXT INDEX ft_excerpt_content (content, thought)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日摘录表';

-- ==============================
-- 标签表
-- ==============================
CREATE TABLE IF NOT EXISTS t_tag (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL COMMENT '用户ID',
    name       VARCHAR(50) NOT NULL COMMENT '标签名',
    color      VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    usage_count INT        DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_user_name (user_id, name),
    INDEX idx_tag_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ==============================
-- 摘录-标签关联表
-- ==============================
CREATE TABLE IF NOT EXISTS t_excerpt_tag_rel (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    excerpt_id BIGINT NOT NULL COMMENT '摘录ID',
    tag_id     BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_excerpt_tag (excerpt_id, tag_id),
    INDEX idx_tag_excerpt (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摘录标签关联表';

-- ==============================
-- 记账分类表
-- ==============================
CREATE TABLE IF NOT EXISTS t_accounting_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       DEFAULT NULL COMMENT '用户ID(NULL=系统预设)',
    parent_id   BIGINT       DEFAULT NULL COMMENT '父分类ID',
    name        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    icon        VARCHAR(50)  DEFAULT NULL COMMENT '图标',
    type        VARCHAR(10)  NOT NULL COMMENT '类型(INCOME/EXPENSE)',
    sort_order  INT          DEFAULT 0 COMMENT '排序',
    is_system   TINYINT(1)   DEFAULT 0 COMMENT '是否系统预设',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cat_user_type (user_id, type),
    INDEX idx_cat_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账分类表';

-- ==============================
-- 记账明细表
-- ==============================
CREATE TABLE IF NOT EXISTS t_accounting (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    type          VARCHAR(10)   NOT NULL COMMENT '类型(INCOME/EXPENSE)',
    amount        DECIMAL(12,2) NOT NULL COMMENT '金额',
    category_id   BIGINT        NOT NULL COMMENT '分类ID',
    account_type  VARCHAR(20)   DEFAULT 'WECHAT' COMMENT '账户(CASH/WECHAT/ALIPAY/BANK)',
    remark        VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    images        JSON          DEFAULT NULL COMMENT '凭证图片',
    accounting_date DATE        NOT NULL COMMENT '记账日期',
    accounting_time TIME        DEFAULT NULL COMMENT '记账时间',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT(1)    DEFAULT 0,
    INDEX idx_acc_user_date (user_id, accounting_date),
    INDEX idx_acc_user_type (user_id, type),
    INDEX idx_acc_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账明细表';

-- ==============================
-- 预算表
-- ==============================
CREATE TABLE IF NOT EXISTS t_budget (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL COMMENT '用户ID',
    category_id   BIGINT        DEFAULT NULL COMMENT '分类ID(NULL=总预算)',
    budget_year   INT           NOT NULL COMMENT '预算年份',
    budget_month  INT           NOT NULL COMMENT '预算月份',
    amount        DECIMAL(12,2) NOT NULL COMMENT '预算金额',
    created_at    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_budget (user_id, category_id, budget_year, budget_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- ==============================
-- 每日总结表
-- ==============================
CREATE TABLE IF NOT EXISTS t_daily_summary (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    summary_date    DATE         NOT NULL COMMENT '总结日期',
    mood            TINYINT      DEFAULT 3 COMMENT '心情(1~5)',
    score           TINYINT      DEFAULT 5 COMMENT '今日评分(1~10)',
    achievement     TEXT         DEFAULT NULL COMMENT '今日成就',
    improvement     TEXT         DEFAULT NULL COMMENT '今日不足',
    tomorrow_plan   TEXT         DEFAULT NULL COMMENT '明日计划',
    gratitude       JSON         DEFAULT NULL COMMENT '感恩记录(JSON数组)',
    health_note     TEXT         DEFAULT NULL COMMENT '健康记录',
    free_writing    TEXT         DEFAULT NULL COMMENT '自由日记',
    tags            JSON         DEFAULT NULL COMMENT '标签(JSON数组)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)   DEFAULT 0,
    UNIQUE KEY uk_summary_user_date (user_id, summary_date),
    INDEX idx_summary_user_mood (user_id, mood)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日总结表';
