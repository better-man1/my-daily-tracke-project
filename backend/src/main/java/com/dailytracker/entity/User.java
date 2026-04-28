package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码（BCrypt） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 微信OpenID */
    private String wxOpenid;

    /** 微信UnionID */
    private String wxUnionid;

    /** 个人签名 */
    private String signature;

    /** 状态(1=正常,0=禁用) */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
}
