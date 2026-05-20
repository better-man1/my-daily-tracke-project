package com.dailytracker.service;

import com.dailytracker.dto.request.LoginRequest;
import com.dailytracker.dto.request.RegisterRequest;
import com.dailytracker.dto.request.WxLoginRequest;
import com.dailytracker.dto.response.LoginResponse;

/**
 * 认证服务接口（Authentication Service Interface）
 *
 * 【职责说明】
 * 本接口负责系统中所有与"身份认证"相关的业务逻辑，是整个安全体系的核心入口。
 * 涵盖的功能包括：用户名密码注册/登录、JWT令牌刷新、微信小程序登录等。
 *
 * 【设计模式与原则】
 * - 面向接口编程（Interface-Oriented Programming）：
 *   本接口只定义"做什么"（契约），具体的"怎么做"由实现类（如 AuthServiceImpl）负责。
 *   Controller 层只依赖此接口，不直接依赖实现类，从而实现解耦。
 *   这符合"依赖倒置原则"（DIP，Dependency Inversion Principle）——高层模块不应依赖低层模块，两者都应依赖抽象。
 *
 * - 单一职责原则（SRP，Single Responsibility Principle）：
 *   该接口只关注"认证"相关的操作，不涉及用户信息管理（由 UserService 负责）。
 *
 * - 接口隔离原则（ISP，Interface Segregation Principle）：
 *   接口方法精简，只包含认证相关的核心操作，不会强迫客户端依赖不需要的方法。
 *
 * 【扩展说明】
 * 如果未来需要支持更多的登录方式（如手机验证码登录、OAuth2 第三方登录），
 * 只需要在此接口中添加新方法，并在实现类中提供对应逻辑即可，无需修改已有代码。
 * 这体现了"开闭原则"（OCP，Open/Closed Principle）——对扩展开放，对修改关闭。
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * 【业务流程】
     * 1. 校验用户名是否已存在（不允许重复用户名）
     * 2. 对密码进行加密存储（使用 BCrypt 等算法）
     * 3. 创建用户记录并保存到数据库
     *
     * @param request 注册请求 DTO，包含用户名、密码、昵称等信息
     * @throws com.dailytracker.common.exception.BusinessException 如果用户名已存在则抛出业务异常
     */
    void register(RegisterRequest request);

    /**
     * 用户登录（用户名 + 密码方式）
     *
     * 【业务流程】
     * 1. 根据用户名查询用户记录
     * 2. 校验用户状态（是否被禁用）
     * 3. 验证密码是否匹配（使用 PasswordEncoder 进行密文比对）
     * 4. 更新用户的最后登录时间
     * 5. 生成 JWT AccessToken 和 RefreshToken 并返回
     *
     * @param request 登录请求 DTO，包含用户名和密码
     * @return LoginResponse 登录响应 DTO，包含用户信息及 JWT 令牌
     * @throws com.dailytracker.common.exception.BusinessException 用户不存在、密码错误或用户被禁用时抛出异常
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新 Token
     *
     * 【业务流程】
     * 1. 验证 refreshToken 是否有效（未过期且签名正确）
     * 2. 从 Token 中提取用户ID，查询用户信息
     * 3. 校验用户状态是否正常
     * 4. 生成新的 AccessToken 和 RefreshToken 并返回
     *
     * 【设计说明】
     * JWT 双令牌机制（AccessToken + RefreshToken）是一种常见的安全设计：
     * - AccessToken 有效期短（如 2 小时），用于日常接口鉴权
     * - RefreshToken 有效期长（如 7 天），用于在 AccessToken 过期后无感刷新
     * 这样既保证了安全性，又兼顾了用户体验。
     *
     * @param refreshToken 刷新令牌字符串
     * @return LoginResponse 包含新生成的令牌信息
     * @throws com.dailytracker.common.exception.BusinessException Token 无效或用户不存在时抛出异常
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 微信小程序登录
     *
     * 【业务流程】
     * 1. 使用前端传来的 code 调用微信 API 换取 openid（用户的唯一标识）
     * 2. 根据 openid 在本地数据库中查找用户
     * 3. 如果用户不存在，则自动创建新用户（自动注册机制）
     * 4. 更新最后登录时间
     * 5. 生成 JWT 令牌并返回
     *
     * 【设计说明】
     * 微信登录采用了"自动注册"策略：首次微信登录的用户无需额外注册流程，
     * 系统会根据 openid 自动创建账户，降低了使用门槛。
     *
     * @param request 微信登录请求 DTO，包含微信授权码（code）
     * @return LoginResponse 包含用户信息及 JWT 令牌
     * @throws com.dailytracker.common.exception.BusinessException 微信接口调用失败时抛出异常
     */
    LoginResponse wxLogin(WxLoginRequest request);
}
