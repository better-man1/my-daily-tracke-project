package com.dailytracker.controller;

// ==================== 导入依赖说明 ====================
// Result: 统一响应封装类，将所有API返回值包装为统一的JSON格式（包含code、message、data字段）
import com.dailytracker.common.result.Result;
// LoginRequest: 登录请求的DTO（数据传输对象），包含用户名和密码字段
import com.dailytracker.dto.request.LoginRequest;
// RegisterRequest: 注册请求的DTO，包含用户名、密码、邮箱等注册所需字段
import com.dailytracker.dto.request.RegisterRequest;
// WxLoginRequest: 微信登录请求的DTO，包含微信授权码等字段
import com.dailytracker.dto.request.WxLoginRequest;
// LoginResponse: 登录成功后的响应DTO，包含JWT令牌（accessToken、refreshToken）和用户基本信息
import com.dailytracker.dto.response.LoginResponse;
// AuthService: 认证业务逻辑层接口，处理注册、登录、Token刷新等核心业务
import com.dailytracker.service.AuthService;
// @Tag: Swagger/OpenAPI注解，用于对API接口进行分组，在API文档中显示为分组名称
import io.swagger.v3.oas.annotations.tags.Tag;
// @Operation: Swagger/OpenAPI注解，用于描述单个API接口的功能摘要，显示在API文档中
import io.swagger.v3.oas.annotations.Operation;
// @Valid: Jakarta Bean Validation注解，用于触发请求参数的自动校验
//   当DTO字段上标注了@NotBlank、@Size、@Email等注解时，@Valid会自动执行校验
//   校验失败时抛出MethodArgumentNotValidException，由全局异常处理器统一返回错误信息
import jakarta.validation.Valid;
// @RequiredArgsConstructor: Lombok注解，自动生成包含所有final字段的构造函数
//   等价于手写 public AuthController(AuthService authService) { this.authService = authService; }
//   Spring会通过构造函数注入（Constructor Injection）自动注入依赖的Bean
import lombok.RequiredArgsConstructor;
// Spring MVC Web层注解集合：
//   @RestController = @Controller + @ResponseBody，标记该类为REST控制器
//   @RequestMapping: 映射HTTP请求的URL路径前缀
//   @PostMapping: 映射HTTP POST请求
//   @RequestBody: 将HTTP请求体中的JSON自动反序列化为Java对象
//   @RequestHeader: 获取HTTP请求头的值
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器（AuthController）
 *
 * 【职责说明】
 * 本控制器负责处理用户认证相关的所有API请求，包括：
 *   1. 用户注册（账号密码注册）
 *   2. 用户登录（账号密码登录，返回JWT令牌）
 *   3. Token刷新（使用RefreshToken获取新的AccessToken）
 *   4. 微信一键登录（通过微信授权码登录）
 *
 * 【RESTful设计思想】
 * - URL路径采用名词而非动词：/api/v1/auth 是资源路径，操作通过HTTP方法（POST）体现
 * - 版本控制：URL中包含/v1/，便于后续API版本升级（如升级到/v2/时不影响旧版客户端）
 * - 无状态认证：使用JWT令牌，服务端不保存会话状态，每次请求携带Token即可验证身份
 * - Token双令牌机制：AccessToken（短期有效，如2小时）+ RefreshToken（长期有效，如7天）
 *   AccessToken过期后用RefreshToken获取新的AccessToken，避免用户频繁重新登录
 *
 * 【API路径前缀】/api/v1/auth
 */
// @Tag: Swagger分组标签，在Swagger UI中将该控制器的所有接口归入"认证管理"分组
// 方便前端开发者在API文档中快速定位认证相关的接口
@Tag(name = "认证管理", description = "注册、登录、刷新Token")
// @RestController: 标记该类为RESTful控制器
// = @Controller（标记为Spring MVC控制器） + @ResponseBody（方法的返回值直接作为HTTP响应体）
// 所有方法的返回值会自动通过Jackson序列化为JSON格式
@RestController
// @RequestMapping: 定义该控制器所有接口的URL路径前缀
// 所有方法的路径都会拼接此前缀，例如 register() 的完整路径为 POST /api/v1/auth/register
@RequestMapping("/api/v1/auth")
// @RequiredArgsConstructor: Lombok自动生成构造函数，Spring通过构造函数自动注入依赖
// 构造函数注入是Spring推荐的注入方式（优于@Autowired字段注入），原因：
//   1. 依赖不可变（final字段），防止运行时被修改
//   2. 依赖不为null（构造时必须提供），避免NPE
//   3. 便于单元测试（可直接通过构造函数传入Mock对象）
@RequiredArgsConstructor
public class AuthController {

    // 依赖注入：通过final + @RequiredArgsConstructor实现构造函数注入
    // AuthService负责具体的认证业务逻辑（密码加密、Token生成、用户验证等）
    private final AuthService authService;

    /**
     * 用户注册接口
     *
     * 【功能】接收用户注册信息，创建新用户账号
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/auth/register
     *
     * 【参数说明】
     * @param request 注册请求DTO，包含用户名、密码、确认密码、邮箱等字段
     *                通过@Valid触发Bean Validation自动校验：
     *                - 用户名不能为空，长度在3-20之间
     *                - 密码不能为空，长度在6-20之间
     *                - 邮箱格式必须合法（如使用@Email注解）
     *                通过@RequestBody将JSON请求体自动反序列化为Java对象
     *
     * 【返回值】Result<Void> - 统一响应对象，注册成功时不返回数据（data为null）
     *
     * 【RESTful设计说明】
     * - 使用POST方法：因为注册是"创建新资源"的操作，POST语义为"在指定资源集合下创建子资源"
     * - 路径使用名词/register：表示注册这个动作（也可以设计为POST /api/v1/users表示创建用户）
     * - 返回void而非用户信息：出于安全考虑，注册成功后不直接返回用户详情，需重新登录
     */
    @Operation(summary = "用户注册") // Swagger注解，描述接口功能，显示在API文档中
    @PostMapping("/register") // 映射HTTP POST请求到 /api/v1/auth/register
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 调用Service层执行注册逻辑：密码加密、用户名唯一性检查、数据持久化等
        authService.register(request);
        // 返回统一格式的成功响应，包含提示信息和null数据
        // Result.success("注册成功", null) 的JSON示例：{"code":200,"message":"注册成功","data":null}
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录接口
     *
     * 【功能】验证用户身份，返回JWT令牌（AccessToken + RefreshToken）
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/auth/login
     *
     * 【参数说明】
     * @param request 登录请求DTO，包含用户名和密码
     *                @Valid触发校验：确保用户名和密码不为空
     *                @RequestBody将JSON体映射为Java对象
     *
     * 【返回值】Result<LoginResponse> - 包含JWT令牌和用户基本信息
     *           LoginResponse通常包含：accessToken、refreshToken、tokenType("Bearer")、expiresIn等
     *
     * 【安全设计说明】
     * - 密码传输：前端应通过HTTPS发送密码（加密传输），避免明文泄露
     * - 密码存储：后端使用BCrypt等算法加密存储，不可逆，即使数据库泄露也无法还原明文
     * - 登录失败：返回模糊的错误提示（如"用户名或密码错误"），不提示具体是哪个错误
     *   防止攻击者通过错误信息枚举用户名
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login") // 映射HTTP POST请求到 /api/v1/auth/login
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 调用Service层执行登录验证：密码比对、生成JWT令牌对
        LoginResponse response = authService.login(request);
        // 返回包含Token信息的成功响应
        // JSON示例：{"code":200,"message":"success","data":{"accessToken":"eyJ...","refreshToken":"eyJ..."}}
        return Result.success(response);
    }

    /**
     * 刷新Token接口
     *
     * 【功能】使用RefreshToken获取新的AccessToken，实现无感刷新
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/auth/refresh
     *
     * 【参数说明】
     * @param refreshToken 刷新令牌，从HTTP请求头"Refresh-Token"字段中获取
     *                     使用@RequestHeader注解从请求头中提取指定字段的值
     *                     注意：不使用@RequestBody，因为这里只需要一个字符串值
     *
     * 【返回值】Result<LoginResponse> - 包含新的AccessToken（可能同时返回新的RefreshToken）
     *
     * 【Token刷新机制说明】
     * - 为什么需要刷新？AccessToken有效期短（如2小时），过期后用户需要重新登录
     * - RefreshToken有效期长（如7天），用于在AccessToken过期后换取新的AccessToken
     * - 流程：前端检测到AccessToken过期 -> 携带RefreshToken调用此接口 -> 获取新的AccessToken
     * - 安全性：RefreshToken只用于获取新的AccessToken，不能用于访问业务API
     *
     * 【为什么Token放在请求头而非请求体？】
     * - Token不是敏感的业务数据，放在Header中更符合HTTP规范
     * - POST请求体通常用于传递业务数据，Token作为认证信息更适合放在Header中
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh") // 映射HTTP POST请求到 /api/v1/auth/refresh
    public Result<LoginResponse> refreshToken(
            // @RequestHeader: 从HTTP请求头中获取指定名称的头部值
            // 客户端需要在请求头中添加：Refresh-Token: <token值>
            @RequestHeader("Refresh-Token") String refreshToken) {
        // 调用Service层验证RefreshToken的有效性，生成新的AccessToken
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    /**
     * 微信一键登录接口
     *
     * 【功能】通过微信授权码实现微信用户的快捷登录/注册
     *
     * 【HTTP方法】POST
     * 【URL路径】/api/v1/auth/wx-login
     *
     * 【参数说明】
     * @param request 微信登录请求DTO，通常包含微信授权码（code）
     *                @Valid触发校验：确保授权码不为空
     *
     * 【返回值】Result<LoginResponse> - 与普通登录相同，返回JWT令牌和用户信息
     *
     * 【微信登录流程（OAuth 2.0）】
     * 1. 前端调用微信登录SDK，获取授权码（code）
     * 2. 前端将code发送到本接口
     * 3. 后端使用code向微信服务器换取用户的openid和access_token
     * 4. 后端根据openid查询本地用户：
     *    - 如果已注册：直接登录，返回JWT令牌
     *    - 如果未注册：自动创建新用户（静默注册），然后返回JWT令牌
     * 5. 前端保存JWT令牌，后续请求携带令牌访问API
     *
     * 【设计最佳实践】
     * - 微信登录与账号密码登录使用相同的返回格式（LoginResponse），保持API一致性
     * - 微信用户首次登录自动注册，降低使用门槛
     * - 将微信特有的逻辑封装在Service层，Controller只负责转发
     */
    @Operation(summary = "微信一键登录")
    @PostMapping("/wx-login") // 映射HTTP POST请求到 /api/v1/auth/wx-login
    public Result<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        // 调用Service层处理微信登录：验证code、获取openid、查找或创建用户、生成JWT
        LoginResponse response = authService.wxLogin(request);
        return Result.success(response);
    }
}
