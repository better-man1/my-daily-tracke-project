package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务提醒实体类 - 管理每日计划任务的提醒通知设置
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_plan_reminder}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多个提醒属于一个用户（通过 userId 关联）</li>
 *   <li>与 DailyPlan 实体为多对一关系：多个提醒可以关联同一个任务（通过 planId 关联）</li>
 *   <li>一个任务可以有多个提醒（如"开始前15分钟"和"截止时"两个提醒）</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_plan_reminder")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>提醒功能帮助用户在指定时间收到任务提醒通知。支持多种提醒类型：
 * <ul>
 *   <li>任务开始提醒（START）：在任务计划开始时间前提醒用户</li>
 *   <li>任务截止提醒（DUE）：在任务截止时间前提醒用户</li>
 *   <li>自定义提醒（CUSTOM）：用户自定义的任意时间提醒</li>
 * </ul>
 * 后台定时任务会定期扫描未发送的提醒（isSent=false 且 reminderTime <= 当前时间），
 * 通过消息推送服务（如微信模板消息、站内通知等）发送提醒。</p>
 *
 * <p><b>【技术知识点 - 定时提醒的实现方案】</b></p>
 * <p>实现定时提醒通常有以下几种方案：
 * <ul>
 *   <li><b>数据库轮询</b>（本系统采用）：定时任务每隔一定时间（如每分钟）扫描数据库，
 *       查找 isSent=false 且 reminderTime <= NOW() 的记录，发送提醒后标记 isSent=true。
 *       优点：实现简单；缺点：有延迟（取决于轮询间隔）。</li>
 *   <li><b>消息队列延迟队列</b>：使用 RabbitMQ 的死信队列或 RocketMQ 的延迟消息功能。
 *       优点：实时性好；缺点：架构复杂度增加。</li>
 *   <li><b>Redis 过期键通知</b>：将提醒时间存为 Redis key 的过期时间，监听过期事件。
 *       优点：实时性好；缺点：可靠性依赖 Redis 配置。</li>
 * </ul>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see DailyPlan 每日计划实体，通过 planId 关联
 * @see com.dailytracker.mapper.PlanReminderMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_plan_reminder")
public class PlanReminder extends BaseEntity {

    /**
     * 任务ID - 关联 t_daily_plan 表的主键 id
     * <p>
     * 标识此提醒关联的具体任务。一个任务可以有多个提醒。
     * 应建立索引以提高按任务查询提醒的性能。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long planId;

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此提醒属于哪个用户。冗余存储，用于：
     * <ul>
     *   <li>数据权限控制：查询时确保只能看到自己的提醒</li>
     *   <li>推送通知时确定目标用户</li>
     *   <li>避免 JOIN 查询，提高查询性能</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 提醒时间 - 提醒应该触发的时间点
     * <p>
     * 使用 {@link LocalDateTime} 类型，精确到秒。
     * 后台定时任务会将此时间与当前时间比较，判断是否应该发送提醒。
     * <p>
     * 数据库字段类型建议：DATETIME NOT NULL，应建立索引（用于定时任务高效扫描）
     */
    private LocalDateTime reminderTime;

    /**
     * 提醒类型 - 提醒的触发场景
     * <p>
     * <ul>
     *   <li>"START" - 任务开始提醒：在任务计划开始时间前提醒用户准备</li>
     *   <li>"DUE" - 任务截止提醒：在任务截止时间前提醒用户尽快完成</li>
     *   <li>"CUSTOM" - 自定义提醒：用户自定义的任意时间点提醒</li>
     * </ul>
     * 不同类型的提醒在前端展示时会显示不同的文案。
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String reminderType;

    /**
     * 是否已发送 - 标记提醒是否已经成功推送给用户
     * <p>
     * <ul>
     *   <li>false - 未发送：等待定时任务扫描并发送</li>
     *   <li>true - 已发送：提醒已成功推送，不需要再次发送</li>
     * </ul>
     * 使用 {@link Boolean} 类型而非 Integer，语义更清晰。
     * MyBatis-Plus 会自动将 Boolean 映射为数据库的 TINYINT(1) 类型。
     * <p>
     * 数据库字段类型建议：TINYINT(1) DEFAULT 0
     */
    private Boolean isSent;
}
