-- ============================================================
-- DailyTracker 数据库初始化脚本
-- 版本: v1.0
-- 日期: 2026-04-27
-- 说明: 包含建库、建表、索引、初始化分类数据
-- ============================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS daily_tracker
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE daily_tracker;

-- ============================================================
-- 2. 建表语句
-- ============================================================

-- 2.1 用户表
CREATE TABLE t_user (
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

-- 2.2 用户偏好设置表
CREATE TABLE t_user_settings (
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

-- 2.3 目标计划表
CREATE TABLE t_goal_plan (
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

-- 2.4 目标关键结果表
CREATE TABLE t_goal_kr (
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

-- 2.5 每日计划表
CREATE TABLE t_daily_plan (
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

-- 2.6 每日摘录表
CREATE TABLE t_excerpt (
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

-- 2.7 标签表
CREATE TABLE t_tag (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL COMMENT '用户ID',
    name       VARCHAR(50) NOT NULL COMMENT '标签名',
    color      VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    usage_count INT        DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_tag_user_name (user_id, name),
    INDEX idx_tag_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 2.8 摘录-标签关联表
CREATE TABLE t_excerpt_tag_rel (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    excerpt_id BIGINT NOT NULL COMMENT '摘录ID',
    tag_id     BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE KEY uk_excerpt_tag (excerpt_id, tag_id),
    INDEX idx_tag_excerpt (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摘录标签关联表';

-- 2.9 记账分类表
CREATE TABLE t_accounting_category (
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

-- 2.10 记账明细表
CREATE TABLE t_accounting (
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

-- 2.11 预算表
CREATE TABLE t_budget (
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

-- 2.12 每日总结表
CREATE TABLE t_daily_summary (
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

-- ============================================================
-- 3. 初始化数据 — 系统预设记账分类
-- ============================================================

-- 3.1 支出一级分类
INSERT INTO t_accounting_category (id, user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(1,  NULL, NULL, '餐饮美食', 'icon-food',        'EXPENSE', 1,  1),
(2,  NULL, NULL, '交通出行', 'icon-transport',    'EXPENSE', 2,  1),
(3,  NULL, NULL, '购物消费', 'icon-shopping',     'EXPENSE', 3,  1),
(4,  NULL, NULL, '居住物业', 'icon-house',        'EXPENSE', 4,  1),
(5,  NULL, NULL, '娱乐休闲', 'icon-entertainment','EXPENSE', 5,  1),
(6,  NULL, NULL, '医疗健康', 'icon-medical',      'EXPENSE', 6,  1),
(7,  NULL, NULL, '学习教育', 'icon-education',    'EXPENSE', 7,  1),
(8,  NULL, NULL, '人情社交', 'icon-social',       'EXPENSE', 8,  1),
(9,  NULL, NULL, '其他支出', 'icon-other',        'EXPENSE', 9,  1);

-- 3.2 支出二级分类
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
-- 餐饮美食
(NULL, 1, '早餐', 'icon-breakfast',  'EXPENSE', 1, 1),
(NULL, 1, '午餐', 'icon-lunch',      'EXPENSE', 2, 1),
(NULL, 1, '晚餐', 'icon-dinner',     'EXPENSE', 3, 1),
(NULL, 1, '零食', 'icon-snack',      'EXPENSE', 4, 1),
(NULL, 1, '饮品', 'icon-drink',      'EXPENSE', 5, 1),
-- 交通出行
(NULL, 2, '公交地铁', 'icon-metro',  'EXPENSE', 1, 1),
(NULL, 2, '打车',     'icon-taxi',   'EXPENSE', 2, 1),
(NULL, 2, '加油',     'icon-fuel',   'EXPENSE', 3, 1),
(NULL, 2, '停车',     'icon-parking','EXPENSE', 4, 1),
-- 购物消费
(NULL, 3, '日用品', 'icon-daily',    'EXPENSE', 1, 1),
(NULL, 3, '服饰',   'icon-clothes',  'EXPENSE', 2, 1),
(NULL, 3, '数码',   'icon-digital',  'EXPENSE', 3, 1),
(NULL, 3, '家居',   'icon-home',     'EXPENSE', 4, 1),
-- 居住物业
(NULL, 4, '房租',   'icon-rent',     'EXPENSE', 1, 1),
(NULL, 4, '水电',   'icon-utility',  'EXPENSE', 2, 1),
(NULL, 4, '物业',   'icon-property', 'EXPENSE', 3, 1),
(NULL, 4, '维修',   'icon-repair',   'EXPENSE', 4, 1),
-- 娱乐休闲
(NULL, 5, '电影',   'icon-movie',    'EXPENSE', 1, 1),
(NULL, 5, '游戏',   'icon-game',     'EXPENSE', 2, 1),
(NULL, 5, '旅游',   'icon-travel',   'EXPENSE', 3, 1),
(NULL, 5, '运动',   'icon-sport',    'EXPENSE', 4, 1),
-- 医疗健康
(NULL, 6, '门诊',   'icon-clinic',   'EXPENSE', 1, 1),
(NULL, 6, '药品',   'icon-medicine', 'EXPENSE', 2, 1),
(NULL, 6, '保健',   'icon-health',   'EXPENSE', 3, 1),
(NULL, 6, '体检',   'icon-checkup',  'EXPENSE', 4, 1),
-- 学习教育
(NULL, 7, '书籍',   'icon-book',     'EXPENSE', 1, 1),
(NULL, 7, '课程',   'icon-course',   'EXPENSE', 2, 1),
(NULL, 7, '考试',   'icon-exam',     'EXPENSE', 3, 1),
(NULL, 7, '培训',   'icon-training', 'EXPENSE', 4, 1),
-- 人情社交
(NULL, 8, '礼金',   'icon-gift',     'EXPENSE', 1, 1),
(NULL, 8, '聚餐',   'icon-party',    'EXPENSE', 2, 1),
(NULL, 8, '红包',   'icon-redpack',  'EXPENSE', 3, 1);

-- 3.3 收入一级分类
INSERT INTO t_accounting_category (id, user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(100, NULL, NULL, '工资薪酬', 'icon-salary',     'INCOME', 1, 1),
(101, NULL, NULL, '兼职收入', 'icon-parttime',   'INCOME', 2, 1),
(102, NULL, NULL, '理财收益', 'icon-investment',  'INCOME', 3, 1),
(103, NULL, NULL, '转账收入', 'icon-transfer',    'INCOME', 4, 1),
(104, NULL, NULL, '其他收入', 'icon-other-income','INCOME', 5, 1);

-- 3.4 收入二级分类
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
-- 工资薪酬
(NULL, 100, '月薪',   'icon-monthly',  'INCOME', 1, 1),
(NULL, 100, '奖金',   'icon-bonus',    'INCOME', 2, 1),
(NULL, 100, '补贴',   'icon-subsidy',  'INCOME', 3, 1),
-- 理财收益
(NULL, 102, '利息',   'icon-interest', 'INCOME', 1, 1),
(NULL, 102, '股票',   'icon-stock',    'INCOME', 2, 1),
(NULL, 102, '基金',   'icon-fund',     'INCOME', 3, 1);

-- ============================================================
-- 执行完毕
-- ============================================================
