package com.dailytracker.common.constant;

/**
 * 全局常量定义
 */
public interface Constants {

    // =================== Redis Key 前缀 ===================

    /** 用户 Token 缓存前缀 */
    String REDIS_KEY_USER_TOKEN = "user:token:";

    /** 用户信息缓存前缀 */
    String REDIS_KEY_USER_INFO = "user:info:";

    /** 看板今日数据缓存前缀 */
    String REDIS_KEY_DASHBOARD_TODAY = "dashboard:today:";

    // =================== 默认值 ===================

    /** 默认每页条数 */
    int DEFAULT_PAGE_SIZE = 20;

    /** 最大每页条数 */
    int MAX_PAGE_SIZE = 100;

    // =================== 每日计划状态 ===================

    String PLAN_STATUS_TODO        = "TODO";
    String PLAN_STATUS_IN_PROGRESS = "IN_PROGRESS";
    String PLAN_STATUS_DONE        = "DONE";
    String PLAN_STATUS_CANCELLED   = "CANCELLED";

    // =================== 目标状态 ===================

    String GOAL_STATUS_NOT_STARTED = "NOT_STARTED";
    String GOAL_STATUS_IN_PROGRESS = "IN_PROGRESS";
    String GOAL_STATUS_COMPLETED   = "COMPLETED";
    String GOAL_STATUS_ABANDONED   = "ABANDONED";

    // =================== 目标类型 ===================

    String GOAL_TYPE_FIVE_YEAR = "FIVE_YEAR";
    String GOAL_TYPE_YEARLY    = "YEARLY";
    String GOAL_TYPE_MONTHLY   = "MONTHLY";
    String GOAL_TYPE_WEEKLY    = "WEEKLY";

    // =================== 记账类型 ===================

    String ACCOUNTING_TYPE_INCOME  = "INCOME";
    String ACCOUNTING_TYPE_EXPENSE = "EXPENSE";

    // =================== 账户类型 ===================

    String ACCOUNT_TYPE_CASH   = "CASH";
    String ACCOUNT_TYPE_WECHAT = "WECHAT";
    String ACCOUNT_TYPE_ALIPAY = "ALIPAY";
    String ACCOUNT_TYPE_BANK   = "BANK";
}
