-- V2: 初始化基础数据
-- 创建时间：2026-04-28

-- ==============================
-- 预设记账分类数据（支出）
-- ==============================

-- 一级分类：支出
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, NULL, '餐饮美食', 'food', 'EXPENSE', 1, 1),
(NULL, NULL, '交通出行', 'transport', 'EXPENSE', 2, 1),
(NULL, NULL, '购物消费', 'shopping', 'EXPENSE', 3, 1),
(NULL, NULL, '居住物业', 'home', 'EXPENSE', 4, 1),
(NULL, NULL, '娱乐休闲', 'entertainment', 'EXPENSE', 5, 1),
(NULL, NULL, '医疗健康', 'health', 'EXPENSE', 6, 1),
(NULL, NULL, '学习教育', 'education', 'EXPENSE', 7, 1),
(NULL, NULL, '人情社交', 'social', 'EXPENSE', 8, 1),
(NULL, NULL, '其他支出', 'other', 'EXPENSE', 9, 1);

-- 二级分类：餐饮美食（parent_id=1）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 1, '早餐', 'breakfast', 'EXPENSE', 1, 1),
(NULL, 1, '午餐', 'lunch', 'EXPENSE', 2, 1),
(NULL, 1, '晚餐', 'dinner', 'EXPENSE', 3, 1),
(NULL, 1, '零食', 'snack', 'EXPENSE', 4, 1),
(NULL, 1, '饮品', 'drink', 'EXPENSE', 5, 1);

-- 二级分类：交通出行（parent_id=2）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 2, '公交地铁', 'bus', 'EXPENSE', 1, 1),
(NULL, 2, '打车', 'taxi', 'EXPENSE', 2, 1),
(NULL, 2, '加油', 'gas', 'EXPENSE', 3, 1),
(NULL, 2, '停车', 'parking', 'EXPENSE', 4, 1);

-- 二级分类：购物消费（parent_id=3）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 3, '日用品', 'daily', 'EXPENSE', 1, 1),
(NULL, 3, '服饰', 'clothes', 'EXPENSE', 2, 1),
(NULL, 3, '数码', 'digital', 'EXPENSE', 3, 1),
(NULL, 3, '家居', 'furniture', 'EXPENSE', 4, 1);

-- 二级分类：居住物业（parent_id=4）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 4, '房租', 'rent', 'EXPENSE', 1, 1),
(NULL, 4, '水电', 'utility', 'EXPENSE', 2, 1),
(NULL, 4, '物业', 'property', 'EXPENSE', 3, 1),
(NULL, 4, '维修', 'repair', 'EXPENSE', 4, 1);

-- 二级分类：娱乐休闲（parent_id=5）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 5, '电影', 'movie', 'EXPENSE', 1, 1),
(NULL, 5, '游戏', 'game', 'EXPENSE', 2, 1),
(NULL, 5, '旅游', 'travel', 'EXPENSE', 3, 1),
(NULL, 5, '运动', 'sport', 'EXPENSE', 4, 1);

-- 二级分类：医疗健康（parent_id=6）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 6, '门诊', 'clinic', 'EXPENSE', 1, 1),
(NULL, 6, '药品', 'medicine', 'EXPENSE', 2, 1),
(NULL, 6, '保健', 'healthcare', 'EXPENSE', 3, 1),
(NULL, 6, '体检', 'checkup', 'EXPENSE', 4, 1);

-- 二级分类：学习教育（parent_id=7）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 7, '书籍', 'book', 'EXPENSE', 1, 1),
(NULL, 7, '课程', 'course', 'EXPENSE', 2, 1),
(NULL, 7, '考试', 'exam', 'EXPENSE', 3, 1),
(NULL, 7, '培训', 'training', 'EXPENSE', 4, 1);

-- 二级分类：人情社交（parent_id=8）
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, 8, '礼金', 'gift', 'EXPENSE', 1, 1),
(NULL, 8, '聚餐', 'party', 'EXPENSE', 2, 1),
(NULL, 8, '红包', 'redpacket', 'EXPENSE', 3, 1);

-- ==============================
-- 预设记账分类数据（收入）
-- ==============================

-- 一级分类：收入
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system) VALUES
(NULL, NULL, '工资薪酬', 'salary', 'INCOME', 1, 1),
(NULL, NULL, '兼职收入', 'parttime', 'INCOME', 2, 1),
(NULL, NULL, '理财收益', 'investment', 'INCOME', 3, 1),
(NULL, NULL, '转账收入', 'transfer', 'INCOME', 4, 1),
(NULL, NULL, '其他收入', 'other', 'INCOME', 5, 1);

-- 二级分类：工资薪酬（parent_id 根据实际插入后的id，此处使用占位）
-- 注意：实际项目中建议使用 Flyway 管理，并在脚本中用明确的 ID 或子查询
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '月薪', 'monthly', 'INCOME', 1, 1 FROM t_accounting_category WHERE name = '工资薪酬' AND is_system = 1 LIMIT 1;

INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '奖金', 'bonus', 'INCOME', 2, 1 FROM t_accounting_category WHERE name = '工资薪酬' AND is_system = 1 LIMIT 1;

INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '补贴', 'subsidy', 'INCOME', 3, 1 FROM t_accounting_category WHERE name = '工资薪酬' AND is_system = 1 LIMIT 1;

-- 理财收益子分类
INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '利息', 'interest', 'INCOME', 1, 1 FROM t_accounting_category WHERE name = '理财收益' AND is_system = 1 LIMIT 1;

INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '股票', 'stock', 'INCOME', 2, 1 FROM t_accounting_category WHERE name = '理财收益' AND is_system = 1 LIMIT 1;

INSERT INTO t_accounting_category (user_id, parent_id, name, icon, type, sort_order, is_system)
SELECT NULL, id, '基金', 'fund', 'INCOME', 3, 1 FROM t_accounting_category WHERE name = '理财收益' AND is_system = 1 LIMIT 1;
