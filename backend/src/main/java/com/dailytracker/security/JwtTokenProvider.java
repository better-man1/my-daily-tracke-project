package com.dailytracker.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者（生成、解析、验证 Token 的核心工具类）
 *
 * 【类的用途】
 * 封装所有与 JWT（JSON Web Token）相关的操作，包括：
 * 1. 生成 Access Token（短期访问令牌，用于接口认证）
 * 2. 生成 Refresh Token（长期刷新令牌，用于无感刷新 Access Token）
 * 3. 从 Token 中解析用户信息（用户ID、用户名）
 * 4. 验证 Token 的有效性（是否过期、签名是否正确）
 *
 * 【设计思想】
 * 采用双 Token 机制（Access Token + Refresh Token）：
 * - Access Token 有效期短（如 2 小时），用于日常接口认证
 * - Refresh Token 有效期长（如 7 天），仅用于获取新的 Access Token
 * - 当 Access Token 过期时，前端使用 Refresh Token 自动获取新的 Access Token，
 *   用户无需重新登录，提升用户体验
 *
 * 【在架构中的位置】
 * 属于安全层（Security Layer），被 JwtAuthenticationFilter 和 AuthService 调用。
 * 是 JWT 认证方案的"发证机关"和"验证机关"。
 *
 * 【相关技术知识点 - JWT 详解】
 * 1. JWT 结构：Header.Payload.Signature
 *    - Header（头部）：包含令牌类型和签名算法
 *    - Payload（负载）：包含声明（Claims），如用户ID、用户名、过期时间等
 *    - Signature（签名）：使用密钥对前两部分进行签名，防止篡改
 *
 * 2. JWT vs Session：
 *    - Session：状态存储在服务端，需要共享 Session 才能支持集群
 *    - JWT：状态存储在 Token 中，天然支持分布式/集群部署
 *
 * 3. JWT 安全注意事项：
 *    - 密钥（secret）必须足够长（建议至少 256 位），否则签名不安全
 *    - 不应在 Token 中存储敏感信息（Token 可被 Base64 解码）
 *    - Token 无法主动失效（除非配合 Redis 黑名单机制）
 *
 * @Slf4j Lombok 注解，自动生成日志记录器 log
 * @Component Spring 组件注解，将此类注册为 Spring Bean
 *            使其他类可以通过依赖注入使用 JwtTokenProvider
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /**
     * JWT 签名密钥（从配置文件读取）
     *
     * @Value("${jwt.secret}") Spring 注解，从 application.yml 中读取 jwt.secret 配置项
     *                          注入到 secret 字段中
     *                          密钥用于对 JWT 进行签名和验证，确保 Token 不被篡改
     *                          【安全提示】密钥长度必须 >= 256 位（32 字节），否则 HMAC-SHA 算法会报错
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Access Token 的过期时间（毫秒）
     * 从配置文件读取，例如：jwt.access-token-expiration=7200000（2小时）
     */
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Refresh Token 的过期时间（毫秒）
     * 从配置文件读取，例如：jwt.refresh-token-expiration=604800000（7天）
     */
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * HMAC-SHA 签名密钥对象
     * 由 secret 字符串转换而来，用于 JJWT 库的签名和验证操作
     */
    private SecretKey secretKey;

    /**
     * 初始化方法 - 在 Bean 创建后自动执行
     *
     * 【方法作用】
     * 将配置文件中的 secret 字符串转换为 SecretKey 对象。
     * 必须在所有 @Value 注入完成之后执行，因此使用 @PostConstruct 注解。
     *
     * @PostConstruct Jakarta 注解，标记在依赖注入完成后需要执行的方法
     *                 执行时机：构造函数执行后、Bean 正式可用前
     *                 此注解确保 secret 已经注入后才进行密钥转换
     *
     * 【Keys.hmacShaKeyFor 说明】
     * 将字节数组转换为适合 HMAC-SHA 算法的 SecretKey。
     * 根据密钥长度自动选择算法：
     *   - 256 位（32 字节）-> HS256
     *   - 384 位（48 字节）-> HS384
     *   - 512 位（64 字节）-> HS512
     * StandardCharsets.UTF_8 确保密钥字符串按 UTF-8 编码转为字节数组
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token（访问令牌）
     *
     * 【方法作用】
     * 创建一个短期的 JWT Token，用于后续 API 请求的身份认证。
     * Access Token 的有效期通常较短（如 2 小时），过期后需要使用 Refresh Token 获取新的。
     *
     * @param userId   用户ID，将作为 JWT 的 subject（主题/主体）
     * @param username 用户名，将作为 JWT 的自定义声明
     * @return String 生成的 JWT Token 字符串，格式如：eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOi...
     */
    public String generateAccessToken(Long userId, String username) {
        return buildToken(userId, username, accessTokenExpiration, "access");
    }

    /**
     * 生成 Refresh Token（刷新令牌）
     *
     * 【方法作用】
     * 创建一个长期的 JWT Token，专门用于刷新 Access Token。
     * Refresh Token 的有效期通常较长（如 7 天），但它只能用于获取新的 Access Token，
     * 不能直接用于访问受保护的 API。
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return String 生成的 Refresh Token
     */
    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, refreshTokenExpiration, "refresh");
    }

    /**
     * 构建 JWT Token 的核心私有方法
     *
     * 【方法作用】
     * 所有 Token 生成的底层逻辑都在此方法中，generateAccessToken 和 generateRefreshToken
     * 只是传入不同的过期时间和类型参数。
     *
     * 【JWT Builder API 说明】
     * - subject()：设置 JWT 的主体（sub 声明），通常存放唯一标识（如用户ID）
     * - claim()：添加自定义声明（key-value 对），可存放任意业务数据
     * - issuedAt()：设置 Token 的签发时间（iat 声明）
     * - expiration()：设置 Token 的过期时间（exp 声明）
     * - signWith()：指定签名密钥，JJWT 会自动根据密钥长度选择 HMAC-SHA 算法
     * - compact()：将构建器内容紧凑序列化为 JWT 字符串
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param expiration 过期时间（毫秒）
     * @param type       Token 类型：("access" 或 "refresh")
     * @return String 完整的 JWT Token 字符串
     */
    private String buildToken(Long userId, String username, long expiration, String type) {
        Date now = new Date();                                           // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration);         // 过期时间 = 当前时间 + 有效期

        return Jwts.builder()
                .subject(String.valueOf(userId))                         // subject 设置为用户ID的字符串形式
                .claim("username", username)                             // 自定义声明：用户名
                .claim("type", type)                                     // 自定义声明：Token 类型（access/refresh）
                .issuedAt(now)                                           // 签发时间
                .expiration(expiryDate)                                  // 过期时间
                .signWith(secretKey)                                     // 使用密钥签名
                .compact();                                              // 序列化为 JWT 字符串
    }

    /**
     * 从 Token 中解析用户ID
     *
     * 【方法作用】
     * 解析 JWT Token，获取 subject 字段中存储的用户ID。
     * subject 在 buildToken 方法中被设置为 userId 的字符串形式。
     *
     * @param token JWT Token 字符串
     * @return Long 用户ID
     */
    public Long getUserIdFromToken(String token) {
        // 解析 Token 获取 Claims（声明集合），然后从 subject 中获取用户ID
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());   // subject 存储的是字符串形式的 userId，需转为 Long
    }

    /**
     * 从 Token 中解析用户名
     *
     * 【方法作用】
     * 解析 JWT Token，获取自定义声明 "username" 中存储的用户名。
     *
     * @param token JWT Token 字符串
     * @return String 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        // 从 Claims 中按 key 获取自定义声明值，指定返回类型为 String
        return claims.get("username", String.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * 【方法作用】
     * 验证 Token 的签名是否正确、是否过期。如果 Token 无效，返回 false。
     * 此方法被 JwtAuthenticationFilter 在每次请求时调用，验证请求中携带的 Token。
     *
     * 【验证逻辑】
     * 1. 尝试解析 Token（parseClaims 内部会验证签名和过期时间）
     * 2. 如果解析成功，说明 Token 有效，返回 true
     * 3. 如果抛出 ExpiredJwtException，说明 Token 已过期
     * 4. 如果抛出其他 JwtException，说明 Token 无效（签名错误、格式错误等）
     *
     * @param token JWT Token 字符串
     * @return boolean true=Token有效，false=Token无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);   // 尝试解析 Token，内部会验证签名和过期时间
            return true;          // 解析成功，Token 有效
        } catch (ExpiredJwtException e) {
            // ExpiredJwtException：Token 已过期的异常
            log.warn("Token已过期: {}", e.getMessage());
        } catch (JwtException e) {
            // JwtException：其他 JWT 相关异常（签名错误、格式错误、不支持等）
            log.warn("Token无效: {}", e.getMessage());
        }
        return false;   // 任何异常都说明 Token 无效
    }

    /**
     * 解析 JWT Token 获取 Claims（声明集合）
     *
     * 【方法作用】
     * 这是 Token 解析的核心方法，验证签名并提取 Token 中的所有声明。
     * 如果签名不匹配或 Token 已过期，会抛出相应的异常。
     *
     * 【JJWT 解析 API 说明】
     * - Jwts.parser()：创建 JWT 解析器
     * - verifyWith(secretKey)：设置验证签名的密钥（必须与生成时使用的密钥一致）
     * - build()：构建解析器实例
     * - parseSignedClaims(token)：解析并验证 Token，返回 Jws 对象
     * - getPayload()：获取 Token 的 Payload 部分（即 Claims）
     *
     * @param token JWT Token 字符串
     * @return Claims Token 中的声明集合，包含 sub（用户ID）、username、type、iat、exp 等
     * @throws ExpiredJwtException Token 已过期
     * @throws JwtException Token 无效（签名错误等）
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)                               // 设置签名验证密钥
                .build()
                .parseSignedClaims(token)                            // 解析 Token（同时验证签名和过期时间）
                .getPayload();                                       // 获取 Payload（Claims）
    }
}
