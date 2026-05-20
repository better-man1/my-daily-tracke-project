package com.dailytracker.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.config.WxProperties;
import com.dailytracker.dto.request.LoginRequest;
import com.dailytracker.dto.request.RegisterRequest;
import com.dailytracker.dto.request.WxLoginRequest;
import com.dailytracker.dto.response.LoginResponse;
import com.dailytracker.entity.User;
import com.dailytracker.mapper.UserMapper;
import com.dailytracker.security.JwtTokenProvider;
import com.dailytracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类（Authentication Service Implementation）
 *
 * 【类设计说明】
 * 本类是 AuthService 接口的具体实现，负责处理用户注册、登录、令牌刷新、微信登录等认证逻辑。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J Logger 实例（等效于 private static final Logger log = LoggerFactory.getLogger(...)）。
 *               使用 log.info()、log.error() 等方法输出日志，方便调试和问题排查。
 *
 * @Service    - Spring 注解，将该类标记为"业务服务层组件"，Spring 容器启动时会自动创建其实例
 *               并注册到 IoC 容器中。其他类可以通过 @Autowired 或构造器注入来使用它。
 *               这是 Spring "控制反转"（IoC，Inversion of Control）思想的核心体现。
 *
 * @RequiredArgsConstructor - Lombok 注解，自动生成包含所有 final 字段的构造器。
 *               配合 Spring 的构造器注入机制，无需手动编写 @Autowired 注解。
 *               这是目前推荐的依赖注入方式（比字段注入更利于测试和不可变性）。
 *
 * 【依赖注入的字段说明】
 * - userMapper:        MyBatis-Plus 的 Mapper 接口，负责用户表的数据库操作
 * - passwordEncoder:   Spring Security 提供的密码编码器（BCrypt），用于密码加密和验证
 * - jwtTokenProvider:  JWT 令牌生成/验证工具类，封装了令牌的创建、解析、校验逻辑
 * - wxProperties:      微信小程序配置属性类（通过 @ConfigurationProperties 绑定配置文件）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户数据访问层（Mapper），通过构造器注入 */
    private final UserMapper userMapper;

    /** 密码编码器（BCrypt），用于密码的加密存储和验证比对 */
    private final PasswordEncoder passwordEncoder;

    /** JWT 令牌提供者，负责 AccessToken 和 RefreshToken 的生成与校验 */
    private final JwtTokenProvider jwtTokenProvider;

    /** 微信小程序配置（appId、appSecret 等） */
    private final WxProperties wxProperties;

    /**
     * AccessToken 过期时间（毫秒），从配置文件 jwt.access-token-expiration 读取。
     * 使用 @Value 注入而非构造器注入，因为它不是 final 字段。
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * 用户注册
     *
     * 【注解解释】
     * @Transactional - Spring 事务管理注解，确保方法内的数据库操作在同一个事务中执行。
     *                  如果方法执行过程中抛出异常，事务将自动回滚（undo 所有数据库更改）。
     *                  这里涉及用户记录的插入操作，需要保证原子性。
     *
     * @param request 注册请求 DTO
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 使用 LambdaQueryWrapper 构建查询条件（MyBatis-Plus 的 Lambda 风格，类型安全，避免硬编码字段名）
        // 查询条件：username 相同且未被逻辑删除（isDeleted=0）
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getIsDeleted, 0)
        );
        // 如果用户名已存在，抛出业务异常（防止重复注册）
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 构建用户实体对象
        User user = new User();
        user.setUsername(request.getUsername());
        // 使用 passwordEncoder 对密码进行加密（BCrypt 算法，每次加密结果不同，安全性高）
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // 如果未提供昵称，则使用用户名作为默认昵称
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        // 设置用户状态为启用（1=启用，0=禁用）
        user.setStatus(1);
        // 插入用户记录到数据库
        userMapper.insert(user);
        // 记录注册成功的日志（使用占位符 {} 避免字符串拼接开销）
        log.info("用户注册成功: username={}", request.getUsername());
    }

    /**
     * 用户登录
     *
     * 【安全设计说明】
     * - 用户不存在和密码错误返回相同的错误码（USER_PASSWORD_ERROR），防止攻击者通过不同的错误信息
     *   判断用户名是否存在（用户名枚举攻击）
     * - 密码验证使用 passwordEncoder.matches() 方法，内部会自动处理 BCrypt 的盐值比对
     *
     * @param request 登录请求 DTO
     * @return LoginResponse 登录响应（含 JWT 令牌）
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查询用户（排除已逻辑删除的用户）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getIsDeleted, 0)
        );
        // 用户不存在时，返回"用户名或密码错误"（而非"用户不存在"，防止信息泄露）
        if (user == null) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 校验用户状态（0=禁用，1=启用）
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 使用 BCrypt 验证密码：将用户输入的明文密码与数据库中的密文进行比对
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 登录成功后，更新最后登录时间（只更新部分字段，不影响其他数据）
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(updateUser);

        // 调用私有方法生成登录响应（含 JWT 令牌）
        return buildLoginResponse(user);
    }

    /**
     * 刷新 Token
     *
     * 【业务流程】
     * 验证 refreshToken 的有效性，然后为用户生成新的令牌对。
     * 这是 JWT 双令牌机制的关键环节。
     *
     * @param refreshToken 刷新令牌字符串
     * @return LoginResponse 包含新的令牌信息
     */
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证 refreshToken 是否有效（签名正确且未过期）
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.USER_TOKEN_INVALID);
        }
        // 从 Token 中提取用户ID
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        // 查询用户信息
        User user = userMapper.selectById(userId);
        // 校验用户是否存在且未被禁用
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 生成新的令牌并返回
        return buildLoginResponse(user);
    }

    /**
     * 微信小程序登录
     *
     * 【业务流程】
     * 1. 使用前端传来的 code 调用微信 API 换取 openid
     * 2. 根据 openid 查找本地用户
     * 3. 首次登录自动注册（静默注册）
     * 4. 更新登录时间并生成令牌
     *
     * @Transactional 注解确保用户查询、创建、更新在同一个事务中，
     * 防止并发场景下创建重复用户。
     */
    @Override
    @Transactional
    public LoginResponse wxLogin(WxLoginRequest request) {
        // 1. 调用微信接口，使用 code 换取 openid 和 session_key
        // 请求格式参考微信官方文档：https://developers.weixin.qq.com/miniprogram/dev/api-backend/open-api/login/auth.code2Session.html
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxProperties.getAppId(), wxProperties.getAppSecret(), request.getCode());

        // 使用 Hutool 的 HTTP 工具发送 GET 请求
        String responseStr = HttpUtil.get(url);
        // 解析微信返回的 JSON 响应
        JSONObject jsonObject = JSONUtil.parseObj(responseStr);
        String openid = jsonObject.getStr("openid");

        // 如果 openid 为空，说明微信接口调用失败
        if (openid == null) {
            log.error("微信登录失败: {}", responseStr);
            throw new BusinessException(ResultCode.USER_LOGIN_ERROR.getCode(), "微信登录失败: " + jsonObject.getStr("errmsg"));
        }

        // 2. 根据 openid 在本地数据库中查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getWxOpenid, openid)
                        .eq(User::getIsDeleted, 0)
        );

        // 3. 如果用户不存在，自动创建新用户（静默注册策略）
        if (user == null) {
            user = new User();
            // 生成唯一的用户名：wx_ + openid 前8位 + 纳秒时间戳
            user.setUsername("wx_" + openid.substring(0, 8) + LocalDateTime.now().getNano());
            // 使用 openid 作为默认密码（微信用户通常不使用密码登录）
            user.setPassword(passwordEncoder.encode(openid));
            user.setNickname("微信用户"); // 默认昵称，用户后续可以修改
            user.setWxOpenid(openid);     // 保存 openid，用于后续登录匹配
            user.setStatus(1);            // 设置为启用状态
            userMapper.insert(user);
            log.info("微信用户自动注册成功: openid={}", openid);
        }

        // 4. 更新最后登录时间
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(updateUser);

        // 5. 生成 JWT 令牌并返回
        return buildLoginResponse(user);
    }

    /**
     * 构建登录响应对象（私有辅助方法）
     *
     * 【设计说明】
     * 这是一个"私有辅助方法"（Private Helper Method），将重复的响应构建逻辑抽取出来，
     * 遵循 DRY 原则（Don't Repeat Yourself）。login、refreshToken、wxLogin 三个方法
     * 都需要构建相同的响应格式，通过提取公共方法避免代码重复。
     *
     * @param user 用户实体
     * @return LoginResponse 登录响应 DTO
     */
    private LoginResponse buildLoginResponse(User user) {
        // 生成 AccessToken（短期令牌，用于接口鉴权）
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        // 生成 RefreshToken（长期令牌，用于刷新 AccessToken）
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 组装响应 DTO
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        // 将毫秒转换为秒返回给前端（前端通常使用秒级时间戳）
        response.setAccessTokenExpireIn(accessTokenExpiration / 1000);
        return response;
    }
}
