package com.dailytracker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置属性类
 *
 * 【类的用途】
 * 将 application.yml 中以 "wx" 为前缀的微信小程序配置属性映射到 Java 对象中，
 * 用于微信小程序的用户登录认证（通过微信的 code 换取 openid/unionid）。
 *
 * 【设计思想】
 * 与 MinioProperties 相同，使用 @ConfigurationProperties 进行属性绑定。
 * 将第三方平台的敏感配置信息（AppID、AppSecret）与代码分离，便于环境切换和安全管控。
 *
 * 【在架构中的位置】
 * 属于配置层，被微信登录相关的 Service 注入使用。
 *
 * 【对应的 application.yml 配置示例】
 * wx:
 *   app-id: wx1234567890abcdef
 *   app-secret: your-app-secret-here
 *
 * 【相关技术知识点 - 微信小程序登录流程】
 * 1. 小程序前端调用 wx.login() 获取临时登录凭证 code
 * 2. 前端将 code 发送到后端
 * 3. 后端使用 appId + appSecret + code 调用微信的 auth.code2Session 接口
 * 4. 微信返回 openid（用户唯一标识）和 session_key
 * 5. 后端使用 openid 作为用户标识，生成 JWT Token 返回给前端
 *
 * 【安全提示】
 * AppSecret 是微信小程序的核心密钥，绝对不能泄露。
 * 建议通过环境变量或配置中心注入，不要硬编码在代码或配置文件中。
 *
 * @Data Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法
 * @Component Spring 组件注解，注册为 Spring Bean
 * @ConfigurationProperties(prefix = "wx") 绑定 yml 中 wx 前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx")
public class WxProperties {

    /**
     * 微信小程序 AppID
     *
     * AppID 是微信小程序的唯一标识，在微信公众平台注册小程序时获得。
     * 每个小程序都有唯一的 AppID，用于在 API 调用中标识应用身份。
     * 例如：wx1234567890abcdef
     */
    private String appId;

    /**
     * 微信小程序 AppSecret
     *
     * AppSecret 是小程序的密钥，与 AppID 配对使用，用于服务端调用微信 API 时的身份验证。
     * 【安全警告】此值极为敏感，泄露后他人可以冒充你的小程序调用微信 API
     * 生产环境中应通过环境变量注入：WX_APP_SECRET=xxx
     */
    private String appSecret;
}
