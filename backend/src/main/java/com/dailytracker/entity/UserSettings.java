package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 用户偏好设置实体类 - 存储用户个性化的系统配置和显示偏好
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_user_settings}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为一对一关系：一个用户对应一条设置记录</li>
 *   <li>通过 userId 字段与 t_user 表的主键 id 关联</li>
 *   <li>注意：此实体没有使用 @TableField(exist = false) 等特殊注解，所有字段均映射到数据库列</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_user_settings")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>用户首次注册后可初始化默认设置，也可在"设置"页面中修改各项偏好。
 * 系统在加载用户相关页面时，会读取这些配置来决定展示样式和行为。</p>
 *
 * <p><b>【设计模式 - 单独设置表】</b></p>
 * <p>将设置字段从 User 表中拆分到独立的 UserSettings 表中，原因：
 * <ul>
 *   <li>遵循单一职责原则（SRP）：User 表只存核心账户信息，Settings 表存偏好配置</li>
 *   <li>减少 User 表的列数，提高查询性能（登录时只需查 User，不需加载设置信息）</li>
 *   <li>便于扩展：新增设置项只需修改此实体，不影响 User 实体</li>
 *   <li>便于按需加载：只在需要时才查询设置信息</li>
 * </ul>
 *
 * @see User 用户实体，通过 userId 关联
 * @see BaseEntity 父类，包含公共字段
 * @see com.dailytracker.mapper.UserSettingsMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_settings")
public class UserSettings extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 这是本表与用户表之间的外键关联字段。
     * 在数据库层面应建立索引以提高关联查询性能。
     * 业务上应为 UNIQUE 约束，确保一个用户只有一条设置记录。
     * <p>
     * 数据库字段类型建议：BIGINT UNIQUE NOT NULL
     */
    private Long userId;

    /**
     * 主题 - 界面显示主题模式
     * <p>
     * 可选值：
     * <ul>
     *   <li>"light" - 浅色主题（默认），适合白天使用</li>
     *   <li>"dark" - 深色主题，适合夜间使用，减少眼睛疲劳</li>
     * </ul>
     * 前端根据此值决定加载哪套 CSS 变量/样式表。
     * <p>
     * 数据库字段类型建议：VARCHAR(10) DEFAULT 'light'
     */
    private String theme;

    /**
     * 默认首页视图 - 用户打开应用时默认显示的页面
     * <p>
     * 可选值根据前端路由定义，例如：
     * <ul>
     *   <li>"plan" - 每日计划页</li>
     *   <li>"summary" - 每日总结页</li>
     *   <li>"accounting" - 记账页</li>
     *   <li>"goal" - 目标页</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String defaultView;

    /**
     * 每日提醒时间 - 系统每日发送提醒通知的时间
     * <p>
     * 使用 Java 8 的 {@link LocalTime} 类型，只存储时分秒信息，不包含日期。
     * 对应数据库的 TIME 类型。
     * <p>
     * 应用场景：
     * <ul>
     *   <li>每日计划提醒：在设定时间提醒用户制定今日计划</li>
     *   <li>每日总结提醒：在设定时间提醒用户填写今日总结</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：TIME DEFAULT '21:00:00'
     */
    private LocalTime reminderTime;

    /**
     * 周起始日 - 定义一周的第一天是周几
     * <p>
     * 可选值范围 1~7，遵循 ISO 8601 标准：
     * <ul>
     *   <li>1 - 周一为起始日（中国/欧洲习惯，ISO标准）</li>
     *   <li>7 - 周日为起始日（美国习惯）</li>
     * </ul>
     * 此设置影响前端日历组件和后端统计报表中"一周"的起止日期计算。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 1
     */
    private Integer weekStartDay;

    /**
     * 语言 - 用户界面的显示语言
     * <p>
     * 使用语言代码（遵循 ISO 639-1 标准），例如：
     * <ul>
     *   <li>"zh-CN" - 简体中文</li>
     *   <li>"zh-TW" - 繁体中文</li>
     *   <li>"en-US" - 英语（美国）</li>
     * </ul>
     * 为未来国际化（i18n）预留字段。
     * <p>
     * 数据库字段类型建议：VARCHAR(10) DEFAULT 'zh-CN'
     */
    private String language;
}
