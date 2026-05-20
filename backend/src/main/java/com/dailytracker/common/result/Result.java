package com.dailytracker.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应体 - 所有接口返回值的标准包装类
 *
 * 【类的用途】
 * 定义统一的 API 响应格式，所有 Controller 方法都返回 Result<T> 类型，
 * 确保前端接收到的响应结构一致，便于统一处理。
 *
 * 响应格式示例（成功）：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { "id": 1, "username": "admin" },
 *   "timestamp": 1700000000000
 * }
 *
 * 响应格式示例（失败）：
 * {
 *   "code": 1001,
 *   "message": "用户不存在",
 *   "timestamp": 1700000000000
 * }
 *
 * 【设计思想】
 * 1. 泛型设计：Result<T> 中的 T 可以是任意类型（User、List<Plan>、PageResult 等）
 * 2. 工厂方法模式：通过静态工厂方法（success/fail/error）创建实例，隐藏构造细节
 * 3. 统一时间戳：每次响应都附带 timestamp，便于排查时序问题
 * 4. JSON 序列化优化：data 为 null 时不出现在 JSON 中（@JsonInclude）
 *
 * 【在架构中的位置】
 * 属于通用层，是 Controller 和前端之间的数据契约（Contract）。
 * 所有 Controller 方法 -> Result<T> -> Jackson 序列化 -> JSON 响应 -> 前端
 *
 * 【相关技术知识点】
 * @Data Lombok 注解，自动生成 getter、setter、toString、equals、hashCode
 *
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 * Jackson 注解，控制 JSON 序列化时的字段包含策略：
 * - NON_NULL：值为 null 的字段不会出现在 JSON 中
 * - 例如：失败响应中 data 为 null，JSON 中就不会有 "data" 字段
 * - 好处：减少 JSON 体积，前端不需要处理 null 值
 *
 * Serializable 接口：
 * - 标记接口，表示此类的实例可以序列化为字节流
 * - 虽然主要用于网络传输（如 RPC），在 RESTful API 中不是必需的
 * - 但作为最佳实践保留，以防未来需要缓存或其他序列化场景
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    /**
     * 响应状态码
     * - 200：成功
     * - 400：参数错误
     * - 401：未认证
     * - 403：无权限
     * - 500：服务器错误
     * - 1001~6999：业务错误码（详见 ResultCode）
     */
    private int code;

    /**
     * 响应消息 - 对状态码的文字描述，便于前端展示给用户
     */
    private String message;

    /**
     * 响应数据 - 泛型类型 T，可以是任何业务数据
     * 成功时包含业务数据，失败时为 null（由于 @JsonInclude 不会出现在 JSON 中）
     */
    private T data;

    /**
     * 时间戳 - 服务器生成响应时的毫秒时间戳
     * 用于排查问题和调试
     */
    private long timestamp;

    /**
     * 私有构造函数 - 禁止外部直接 new Result()
     *
     * 【为什么要私有化？】
     * 强制使用静态工厂方法（success/fail/error）创建实例，好处：
     * 1. 语义清晰：Result.success() 比 new Result() 更易理解
     * 2. 减少出错：工厂方法自动设置正确的 code 和 message
     * 3. 统一入口：所有 Result 创建逻辑集中管理
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();   // 自动设置当前时间戳
    }

    /**
     * 创建成功响应（无数据）
     *
     * 【使用场景】
     * 用于不需要返回数据的操作，如：删除、更新、注册等
     * 示例：return Result.success();
     *
     * @param <T> 数据类型
     * @return Result<T> 成功响应（code=200, message="操作成功", data=null）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());         // 设置状态码：200
        result.setMessage(ResultCode.SUCCESS.getMessage());   // 设置消息："操作成功"
        return result;
    }

    /**
     * 创建成功响应（带数据）
     *
     * 【使用场景】
     * 用于需要返回数据的操作，如：查询用户、获取列表等
     * 示例：return Result.success(user);
     *
     * @param data 响应数据，可以是任何类型（User、List、Map 等）
     * @param <T>  数据类型
     * @return Result<T> 成功响应（包含 data）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应（自定义消息和数据）
     *
     * 【使用场景】
     * 需要自定义成功消息时使用，如："注册成功"、"密码修改成功"
     *
     * @param message 自定义的成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return Result<T> 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 创建失败响应（使用预定义错误码）
     *
     * 【使用场景】
     * 最常用的失败响应创建方式，使用 ResultCode 枚举中预定义的错误码和消息
     * 示例：return Result.fail(ResultCode.USER_NOT_FOUND);
     *
     * @param resultCode 错误码枚举
     * @param <T>        数据类型
     * @return Result<T> 失败响应（不包含 data）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    /**
     * 创建失败响应（自定义消息 + 预定义错误码）
     *
     * 【使用场景】
     * 使用预定义错误码但需要自定义消息时使用
     * 如参数校验失败时，消息包含具体的校验错误信息
     *
     * @param resultCode 错误码枚举（只使用其 code）
     * @param message    自定义错误消息
     * @param <T>        数据类型
     * @return Result<T> 失败响应
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 创建失败响应（完全自定义错误码和消息）
     *
     * 【使用场景】
     * 使用自定义的错误码和消息，通常配合 BusinessException 使用
     *
     * @param code    自定义错误码
     * @param message 自定义错误消息
     * @param <T>     数据类型
     * @return Result<T> 失败响应
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 创建系统错误响应
     *
     * 【使用场景】
     * 用于服务器内部错误，使用 500 错误码但自定义消息
     *
     * @param message 错误描述
     * @param <T>     数据类型
     * @return Result<T> 系统错误响应（code=500）
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.INTERNAL_ERROR.getCode());   // 500
        result.setMessage(message);
        return result;
    }
}
