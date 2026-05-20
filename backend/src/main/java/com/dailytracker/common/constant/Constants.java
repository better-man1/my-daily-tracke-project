package com.dailytracker.common.constant;

/**
 * 全局常量定义接口
 *
 * 【类的用途】
 * 集中管理系统中使用的所有常量字符串和数值，避免在代码中出现"魔法值"
 * （即硬编码的字符串和数字），提高代码的可维护性和可读性。
 *
 * 【设计思想】
 * 1. 使用接口（interface）而非类（class）来定义常量：
 *    - 接口中的字段默认是 public static final 的，无需显式声明
 *    - 书写更加简洁
 *    - 这是 Java 中定义常量的一种惯用模式
 *
 * 2. 按业务模块分组组织常量，便于查找和维护
 *
 * 3. 所有常量使用全大写 + 下划线命名（UPPER_SNAKE_CASE），这是 Java 常量的命名规范
 *
 * 【在架构中的位置】
 * 属于通用层，被所有模块引用。
 *
 * 【什么是"魔法值"？（反面示例）】
 * // 不好的写法 - "TODO" 就是一个魔法值，其他开发者不知道它的含义
 * if (status.equals("TODO")) { ... }
 *
 * // 好的写法 - 使用常量，含义清晰
 * if (status.equals(Constants.PLAN_STATUS_TODO)) { ... }
 *
 * 【扩展说明】
 * 对于更复杂的项目，可以考虑使用枚举类（enum）替代字符串常量，
 * 枚举具有类型安全性，可以在编译时检查错误。
 */
public interface Constants {

    // =================== Redis Key 前缀 ===================
    // Redis 中存储数据时使用的 Key 前缀，遵循 "业务:模块:标识" 的命名规范
    // 好处：通过前缀可以快速识别 Key 的用途，也便于批量管理（如按前缀删除）

    /**
     * 用户 Token 缓存前缀
     * 完整 Key 格式：user:token:{userId}
     * 用途：存储用户的 JWT Token 或 Refresh Token，用于 Token 黑名单/续期等场景
     */
    String REDIS_KEY_USER_TOKEN = "user:token:";

    /**
     * 用户信息缓存前缀
     * 完整 Key 格式：user:info:{userId}
     * 用途：缓存用户的基本信息，减少数据库查询次数
     */
    String REDIS_KEY_USER_INFO = "user:info:";

    /**
     * 看板今日数据缓存前缀
     * 完整 Key 格式：dashboard:today:{userId}
     * 用途：缓存用户今日的看板数据（如期数统计、今日任务等）
     * 通常设置较短的过期时间（如5分钟），保证数据相对实时
     */
    String REDIS_KEY_DASHBOARD_TODAY = "dashboard:today:";

    // =================== 默认值 ===================

    /** 默认每页条数 - 与 BasePageQuery 中的默认值保持一致 */
    int DEFAULT_PAGE_SIZE = 20;

    /** 最大每页条数 - 防止一次查询过多数据 */
    int MAX_PAGE_SIZE = 100;

    // =================== 每日计划状态 ===================
    // 定义计划（Plan）的所有可能状态，使用有限状态机（FSM）的思想

    /** 待办 - 计划已创建但尚未开始执行 */
    String PLAN_STATUS_TODO        = "TODO";

    /** 进行中 - 计划正在执行中 */
    String PLAN_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 已完成 - 计划已成功完成 */
    String PLAN_STATUS_DONE        = "DONE";

    /** 已取消 - 计划被取消（不再执行） */
    String PLAN_STATUS_CANCELLED   = "CANCELLED";

    // =================== 目标状态 ===================

    /** 未开始 - 目标已创建但尚未开始 */
    String GOAL_STATUS_NOT_STARTED = "NOT_STARTED";

    /** 进行中 - 目标正在推进中 */
    String GOAL_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 已完成 - 目标已达成 */
    String GOAL_STATUS_COMPLETED   = "COMPLETED";

    /** 已放弃 - 目标被放弃（不再追求） */
    String GOAL_STATUS_ABANDONED   = "ABANDONED";

    // =================== 目标类型 ===================
    // 定义不同时间维度的目标类型，支持多层级目标管理

    /** 五年规划 - 长期战略目标 */
    String GOAL_TYPE_FIVE_YEAR = "FIVE_YEAR";

    /** 年度目标 - 每年的目标 */
    String GOAL_TYPE_YEARLY    = "YEARLY";

    /** 月度目标 - 每月的目标 */
    String GOAL_TYPE_MONTHLY   = "MONTHLY";

    /** 周目标 - 每周的目标 */
    String GOAL_TYPE_WEEKLY    = "WEEKLY";

    // =================== 记账类型 ===================

    /** 收入 - 如工资、奖金、投资收益等 */
    String ACCOUNTING_TYPE_INCOME  = "INCOME";

    /** 支出 - 如餐饮、交通、购物等 */
    String ACCOUNTING_TYPE_EXPENSE = "EXPENSE";

    // =================== 账户类型 ===================
    // 定义支持的支付/储蓄账户类型

    /** 现金 */
    String ACCOUNT_TYPE_CASH   = "CASH";

    /** 微信支付 */
    String ACCOUNT_TYPE_WECHAT = "WECHAT";

    /** 支付宝 */
    String ACCOUNT_TYPE_ALIPAY = "ALIPAY";

    /** 银行卡 */
    String ACCOUNT_TYPE_BANK   = "BANK";
}
