package com.dailytracker.common.exception;

import com.dailytracker.common.result.Result;
import com.dailytracker.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.StringJoiner;

/**
 * 全局异常处理器 - 统一捕获和处理所有 Controller 层抛出的异常
 *
 * 【类的用途】
 * 集中管理所有类型的异常处理逻辑，将异常转换为统一格式的 JSON 响应（Result 对象），
 * 避免在每个 Controller 中重复编写 try-catch 代码。
 *
 * 处理的异常类型包括：
 * 1. BusinessException - 业务异常（最常见）
 * 2. MethodArgumentNotValidException - @Valid 参数校验异常
 * 3. BindException - 参数绑定异常
 * 4. AuthenticationException - 认证异常（如密码错误）
 * 5. AccessDeniedException - 权限不足异常
 * 6. DataIntegrityViolationException - 数据库完整性约束冲突
 * 7. Exception - 兜底处理所有未知异常
 *
 * 【设计思想】
 * 利用 Spring AOP（面向切面编程）原理，@RestControllerAdvice 注解创建一个全局切面，
 * 自动拦截所有 Controller 方法抛出的异常，然后根据异常类型路由到对应的处理方法。
 * 这遵循了"关注点分离"原则，将异常处理逻辑从业务逻辑中分离出来。
 *
 * 【在架构中的位置】
 * 位于 Web 层的切面，在 Controller 之后、响应返回之前生效。
 * 请求处理流程：HTTP 请求 -> Filter -> Controller -> [抛出异常] -> GlobalExceptionHandler -> JSON 响应
 *
 * 【相关技术知识点】
 * @Slf4j Lombok 注解，自动生成日志对象
 *
 * @RestControllerAdvice Spring 注解，组合了 @ControllerAdvice 和 @ResponseBody：
 *   - @ControllerAdvice：定义全局的控制器增强（异常处理、数据绑定、预处理）
 *   - @ResponseBody：将返回值自动序列化为 JSON 响应
 *   效果：所有异常处理方法的返回值都会自动转为 JSON 返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（BusinessException）
     *
     * 【方法作用】
     * 捕获 Service 层主动抛出的 BusinessException，将其转换为统一的错误响应。
     * 这是最常触发的异常处理方法。
     *
     * @ExceptionHandler(BusinessException.class)
     * Spring 注解，标记此方法处理指定类型的异常。
     * 当 Controller 抛出 BusinessException 时，Spring 会自动调用此方法。
     *
     * @param e 捕获到的业务异常
     * @return Result<Void> 统一格式的错误响应（无 data 字段）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 使用 warn 级别记录日志，业务异常不是系统Bug，但仍需关注
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        // 使用异常中的错误码和消息构建响应
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid 触发的校验失败）
     *
     * 【方法作用】
     * 当使用 Spring Validation（@Valid / @Validated）进行参数校验失败时，
     * Spring 会抛出 MethodArgumentNotValidException。
     * 此方法将所有校验错误信息拼接为一个字符串返回给前端。
     *
     * 【使用场景】
     * 在 Controller 方法参数上添加 @Valid 注解，Spring 会自动校验：
     * @PostMapping("/register")
     * public Result<Void> register(@Valid @RequestBody RegisterDTO dto) { ... }
     * 如果 dto 中的字段不满足校验规则（如 @NotBlank、@Size 等），就会触发此异常。
     *
     * @ResponseStatus(HttpStatus.BAD_REQUEST) 设置 HTTP 响应状态码为 400（Bad Request）
     *
     * @param e 参数校验异常，包含所有校验失败的字段和错误信息
     * @return Result<Void> 包含所有校验错误信息的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // StringJoiner 用于拼接多个错误信息，以分号分隔
        StringJoiner joiner = new StringJoiner("; ");
        // 遍历所有字段校验错误
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            // 格式：字段名: 错误消息，如 "username: 用户名不能为空"
            joiner.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        log.warn("参数校验失败: {}", joiner);
        return Result.fail(ResultCode.BAD_REQUEST, joiner.toString());
    }

    /**
     * 处理参数绑定异常（表单提交时的类型转换失败等）
     *
     * 【方法作用】
     * 当请求参数绑定到 Java 对象失败时（如将字符串 "abc" 绑定到 Integer 字段），
     * Spring 会抛出 BindException。
     *
     * @ResponseStatus(HttpStatus.BAD_REQUEST) HTTP 400
     *
     * @param e 绑定异常
     * @return Result<Void> 包含绑定错误信息的响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        StringJoiner joiner = new StringJoiner("; ");
        for (FieldError fieldError : e.getFieldErrors()) {
            joiner.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }
        return Result.fail(ResultCode.BAD_REQUEST, joiner.toString());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = e.getName() + ": 参数类型错误";
        log.warn("参数类型转换失败: name={}, value={}, requiredType={}",
                e.getName(), e.getValue(), e.getRequiredType());
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 处理 Spring Security 认证异常
     *
     * 【方法作用】
     * 捕获 Spring Security 在认证过程中抛出的异常，如：
     * - BadCredentialsException：用户名或密码错误
     * - 其他 AuthenticationException：其他认证失败情况
     *
     * 【注意】
     * 此处理方法主要处理登录接口的认证异常。
     * JWT Token 认证失败由 JwtAuthenticationFilter 直接处理，不走此方法。
     *
     * @ResponseStatus(HttpStatus.UNAUTHORIZED) HTTP 401
     *
     * @param e 认证异常
     * @return Result<Void> 认证失败的错误响应
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        // 判断是否是密码错误异常（BadCredentialsException 是 AuthenticationException 的子类）
        if (e instanceof BadCredentialsException) {
            return Result.fail(ResultCode.USER_PASSWORD_ERROR);   // 返回"用户名或密码错误"
        }
        // 其他认证异常统一返回"未认证"
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * 处理权限不足异常
     *
     * 【方法作用】
     * 当已登录用户访问没有权限的资源时，Spring Security 抛出 AccessDeniedException。
     * 例如：普通用户访问管理员接口。
     *
     * @ResponseStatus(HttpStatus.FORBIDDEN) HTTP 403
     *
     * @param e 权限不足异常
     * @return Result<Void> 权限不足的错误响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.fail(ResultCode.FORBIDDEN);
    }

    /**
     * 处理数据库数据完整性约束冲突异常
     *
     * 【方法作用】
     * 当违反数据库的唯一约束（UNIQUE）、外键约束（FOREIGN KEY）等时，
     * Spring Data 抛出 DataIntegrityViolationException。
     * 这是一种兜底处理，防止未被业务层提前检查的重复插入导致 500 错误。
     *
     * 【典型场景】
     * 两个请求同时创建相同用户名的用户，第一个请求成功，第二个请求违反唯一约束。
     * 虽然业务层通常会先检查是否存在，但并发场景下可能遗漏，此处理作为最后防线。
     *
     * @ResponseStatus(HttpStatus.CONFLICT) HTTP 409（Conflict）
     *
     * @param e 数据完整性异常
     * @return Result<Void> 数据已存在的错误响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        // getMostSpecificCause() 获取最底层的异常原因（通常是数据库驱动的异常）
        log.warn("数据完整性冲突: {}", e.getMostSpecificCause().getMessage());
        return Result.fail(ResultCode.CONFLICT, "数据已存在，请勿重复提交");
    }

    /**
     * 兜底异常处理 - 捕获所有未被上述方法处理的异常
     *
     * 【方法作用】
     * 这是最后一道防线，处理所有意料之外的异常（如 NullPointerException、
     * ArrayIndexOutOfBoundsException 等系统异常）。
     * 防止向客户端暴露堆栈信息（安全风险），统一返回"服务器内部错误"。
     *
     * 【重要】
     * 使用 log.error 记录完整的异常堆栈，方便开发人员排查问题。
     * 前端只看到"服务器内部错误"，不暴露任何内部实现细节。
     *
     * @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) HTTP 500
     *
     * @param e 未知的系统异常
     * @return Result<Void> 服务器内部错误的响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        // error 级别记录完整堆栈信息（第二个参数 e 会输出完整堆栈）
        log.error("系统异常: {}", e.getMessage(), e);
        // 对外返回统一的错误信息，不暴露内部细节
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
