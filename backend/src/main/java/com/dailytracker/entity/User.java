package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 系统核心实体，存储用户的基本账户信息
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_user}</li>
 *   <li>表命名规范：t_ 前缀 + 业务名（t 代表 table）</li>
 *   <li>继承自 {@link BaseEntity}，因此自动包含 id、createdAt、updatedAt、isDeleted 四个公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>本实体通过 MyBatis-Plus 的 ORM 机制，自动将 Java 字段映射到数据库列</li>
 *   <li>字段名采用 Java 驼峰命名（如 wxOpenid），MyBatis-Plus 自动转换为数据库下划线命名（如 wx_openid）</li>
 *   <li>映射规则在 application.yml 中配置：mybatis-plus.configuration.map-underscore-to-camel-case=true</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法，减少样板代码</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - Lombok 注解，生成 equals/hashCode 时包含父类 BaseEntity 的字段（id、createdAt 等），
 *       确保两个 User 对象即使业务字段相同但 id 不同时不会被判定为相等</li>
 *   <li>{@code @TableName("t_user")} - MyBatis-Plus 注解，指定此实体类对应的数据库表名。
 *       如果不使用此注解，MyBatis-Plus 默认会将类名转为下划线形式作为表名（即默认表名为 user）</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>用户是整个系统的核心实体，其他所有业务实体（计划、总结、记账等）都通过 userId 与用户关联。
 * 支持三种登录方式：用户名密码登录、邮箱登录、微信小程序登录（通过 wxOpenid/wxUnionid）。</p>
 *
 * <p><b>【技术知识点】</b></p>
 * <ul>
 *   <li>密码使用 BCrypt 算法加密存储，Spring Security 提供的 BCryptPasswordEncoder 可直接使用</li>
 *   <li>微信 OpenID 是用户在某个小程序/公众号下的唯一标识，UnionID 是用户在同一微信开放平台下所有应用的唯一标识</li>
 *   <li>status 字段使用整型而非布尔型，便于扩展（如未来可能增加"待审核=2"、"封禁=3"等状态）</li>
 * </ul>
 *
 * @see BaseEntity 父类，包含 id、createdAt、updatedAt、isDeleted 等公共字段
 * @see com.dailytracker.mapper.UserMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    /**
     * 用户名 - 用于登录的唯一标识
     * <p>
     * 业务规则：注册时唯一，不可重复。
     * 数据库字段类型建议：VARCHAR(50) UNIQUE NOT NULL
     */
    private String username;

    /**
     * 密码 - 使用 BCrypt 算法加密后的密码哈希值
     * <p>
     * 存储的是加密后的密文，而非明文密码。
     * BCrypt 是一种自适应哈希算法，自带盐值（salt），安全性高于 MD5/SHA 系列。
     * 验证密码时使用 BCryptPasswordEncoder.matches(rawPassword, encodedPassword) 方法。
     * <p>
     * 数据库字段类型建议：VARCHAR(100)，BCrypt 哈希值固定为 60 个字符
     */
    private String password;

    /**
     * 昵称 - 用户在系统中显示的名称
     * <p>
     * 与 username 不同，昵称用于展示，可以重复，可以修改。
     * 数据库字段类型建议：VARCHAR(50)
     */
    private String nickname;

    /**
     * 头像URL - 用户头像图片的访问地址
     * <p>
     * 可以是系统默认头像，也可以是用户上传后返回的 OSS/CDN 链接。
     * 数据库字段类型建议：VARCHAR(255)
     */
    private String avatar;

    /**
     * 邮箱 - 用于邮箱登录和找回密码
     * <p>
     * 注册时可选，绑定后需要验证。可以作为登录凭证之一。
     * 数据库字段类型建议：VARCHAR(100)
     */
    private String email;

    /**
     * 手机号 - 用于手机号登录和短信验证
     * <p>
     * 注册时可选，绑定后可作为登录凭证。
     * 存储时应考虑加密或脱敏处理。
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String phone;

    /**
     * 微信OpenID - 微信小程序用户的唯一标识
     * <p>
     * 每个用户在同一个小程序/公众号下有唯一的 OpenID。
     * 用于微信授权登录时识别用户身份。
     * 微信登录流程：前端调用 wx.login() 获取 code -> 后端用 code 换取 session_key + openid
     * <p>
     * 数据库字段类型建议：VARCHAR(100)
     */
    private String wxOpenid;

    /**
     * 微信UnionID - 微信开放平台用户的统一标识
     * <p>
     * 同一用户在同一微信开放平台账号下的所有应用（小程序、公众号、App等）
     * 拥有相同的 UnionID，用于多应用之间的用户身份打通。
     * 只有在用户关注了同主体的公众号或授权过同主体的应用后才会返回。
     * <p>
     * 数据库字段类型建议：VARCHAR(100)
     */
    private String wxUnionid;

    /**
     * 个人签名 - 用户在个人主页显示的个性签名
     * <p>
     * 纯展示用途，无业务逻辑约束。
     * 数据库字段类型建议：VARCHAR(200)
     */
    private String signature;

    /**
     * 状态 - 用户账户状态
     * <p>
     * 使用整型而非布尔型，便于后续扩展更多状态：
     * <ul>
     *   <li>1 - 正常：用户可以正常使用系统所有功能</li>
     *   <li>0 - 禁用：管理员手动禁用，用户无法登录</li>
     * </ul>
     * 数据库字段类型建议：INT DEFAULT 1
     */
    private Integer status;

    /**
     * 最后登录时间 - 用户最近一次成功登录的时间
     * <p>
     * 每次用户登录成功后更新此字段。
     * 可用于安全审计（检测异常登录行为）和统计分析（用户活跃度）。
     * <p>
     * 使用 Java 8+ 的 LocalDateTime 类型，对应数据库的 DATETIME 类型。
     * 数据库字段类型建议：DATETIME
     */
    private LocalDateTime lastLoginAt;
}
