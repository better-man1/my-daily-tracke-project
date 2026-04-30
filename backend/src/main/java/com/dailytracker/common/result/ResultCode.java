package com.dailytracker.common.result;

import lombok.Getter;

/**
 * 统一响应码枚举
 */
@Getter
public enum ResultCode {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据已存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 1001~1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已被占用"),
    USER_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    USER_TOKEN_EXPIRED(1005, "Token已过期，请重新登录"),
    USER_TOKEN_INVALID(1006, "Token无效"),
    USER_OLD_PASSWORD_ERROR(1007, "原密码错误"),
    USER_LOGIN_ERROR(1008, "登录失败"),

    // 计划模块 2001~2999
    PLAN_NOT_FOUND(2001, "计划不存在"),
    PLAN_DATE_INVALID(2002, "计划日期无效"),

    // 记账模块 3001~3999
    ACCOUNTING_NOT_FOUND(3001, "账目不存在"),
    ACCOUNTING_CATEGORY_NOT_FOUND(3002, "记账分类不存在"),
    BUDGET_ALREADY_EXISTS(3003, "该月预算已设置"),

    // 摘录模块 4001~4999
    EXCERPT_NOT_FOUND(4001, "摘录不存在"),

    // 目标模块 5001~5999
    GOAL_NOT_FOUND(5001, "目标不存在"),
    GOAL_PARENT_NOT_FOUND(5002, "父目标不存在"),
    GOAL_CIRCULAR_REFERENCE(5003, "不能将目标设置为其子目标的子目标"),

    // 总结模块 6001~6999
    SUMMARY_NOT_FOUND(6001, "总结不存在"),
    SUMMARY_ALREADY_EXISTS(6002, "今日总结已存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
