package com.dailytracker.common.exception;

import com.dailytracker.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 *
 * 【类的用途】
 * 用于封装业务逻辑中的异常情况，例如：
 * - 用户不存在
 * - 用户名已被占用
 * - 密码错误
 * - 计划不存在
 * - 数据已存在（唯一约束冲突）
 *
 * 与系统异常（如 NullPointerException）不同，业务异常是"可预期的错误"，
 * 是正常业务流程的一部分，需要向前端返回友好的错误提示。
 *
 * 【设计思想】
 * 1. 继承 RuntimeException（非受检异常），不需要在方法签名中声明 throws
 * 2. 携带错误码（code）和错误消息（message），便于前端精确处理不同错误
 * 3. 配合 GlobalExceptionHandler 统一捕获，自动转换为标准响应格式
 *
 * 【在架构中的位置】
 * 属于通用异常层，被 Service 层抛出，由 GlobalExceptionHandler 统一捕获处理。
 * 异常流转路径：Service 抛出 BusinessException -> GlobalExceptionHandler 捕获 -> 转为 Result JSON 返回前端
 *
 * 【使用示例】
 * // 使用预定义的错误码
 * throw new BusinessException(ResultCode.USER_NOT_FOUND);
 *
 * // 使用预定义的错误码但自定义消息
 * throw new BusinessException(ResultCode.BAD_REQUEST, "用户名长度必须在3-20之间");
 *
 * // 使用自定义错误码和消息
 * throw new BusinessException(9999, "自定义错误");
 *
 * 【为什么继承 RuntimeException 而非 Exception？】
 * RuntimeException 是非受检异常（Unchecked Exception），编译器不强制要求 try-catch 或 throws。
 * 业务异常通常不需要在上层逐层捕获处理，而是直接抛到 GlobalExceptionHandler 统一处理。
 * 如果继承 Exception（受检异常），每个调用方法都需要声明 throws，代码会变得臃肿。
 *
 * @Getter Lombok 注解，自动生成 getCode() 方法
 *          外部类（如 GlobalExceptionHandler）需要通过 getCode() 获取错误码
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码 - 与 HTTP 状态码或自定义业务错误码对应
     * 例如：400（参数错误）、1001（用户不存在）、2001（计划不存在）
     */
    private final int code;

    /**
     * 构造方法1：使用预定义的错误码枚举
     *
     * 【使用场景】
     * 最常用的构造方式，直接使用 ResultCode 枚举中预定义的错误码和消息。
     *
     * @param resultCode 错误码枚举，包含 code 和 message
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());           // 设置 RuntimeException 的异常消息
        this.code = resultCode.getCode();         // 从枚举中获取错误码
    }

    /**
     * 构造方法2：使用预定义错误码但自定义消息
     *
     * 【使用场景】
     * 当预定义的消息不够精确时，可以自定义更具体的错误消息。
     * 例如：ResultCode.BAD_REQUEST 的消息是"请求参数错误"，
     * 但实际可能是"用户名长度必须在3-20之间"。
     *
     * @param resultCode 错误码枚举（只使用其 code）
     * @param message    自定义的错误消息（覆盖枚举中的默认消息）
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);                           // 使用自定义消息
        this.code = resultCode.getCode();         // 使用枚举中的错误码
    }

    /**
     * 构造方法3：完全自定义错误码和消息
     *
     * 【使用场景】
     * 当需要使用不在 ResultCode 枚举中的错误码时使用。
     * 适用于临时性的、特殊的错误场景。
     *
     * @param code    自定义错误码
     * @param message 自定义错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
