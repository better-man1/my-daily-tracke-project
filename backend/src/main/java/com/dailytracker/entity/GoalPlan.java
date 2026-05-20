package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 目标计划实体类 - 管理用户的中长期目标，支持 OKR（Objectives and Key Results）方法论
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_goal_plan}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多个目标属于一个用户（通过 userId 关联）</li>
 *   <li>自关联关系：parentId 指向本表主键 id，支持目标的层级分解（如五年目标 -> 年度目标 -> 月度目标）</li>
 *   <li>与 GoalKr 实体为一对多关系：一个目标可以有多个关键结果（通过 goalId 关联）</li>
 *   <li>与 DailyPlan 实体为一对多关系：一个目标可以关联多个每日计划（通过 goalId 关联）</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_goal_plan")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>目标管理功能帮助用户将远大的愿景分解为可执行的计划。支持多层级目标体系：
 * <ul>
 *   <li>五年愿景 -> 年度目标 -> 月度目标 -> 周目标 -> 每日计划</li>
 * </ul>
 * 结合 OKR 方法论，每个目标（Objective）下可以设置多个关键结果（Key Result），
 * 通过量化指标来衡量目标的完成进度。</p>
 *
 * <p><b>【技术知识点 - 树形结构的邻接表模型】</b></p>
 * <p>本实体的 parentId 字段实现了邻接表（Adjacency List）模型，用于表示目标的层级关系：
 * <ul>
 *   <li>优点：结构简单直观，易于理解，增删改操作简单</li>
 *   <li>缺点：查询所有子孙节点需要递归（MySQL 8.0+ 支持 CTE 递归查询）</li>
 *   <li>替代方案：嵌套集（Nested Set）、路径枚举（Path Enumeration）、闭包表（Closure Table）</li>
 * </ul>
 *
 * <p><b>【技术知识点 - OKR 方法论】</b></p>
 * <p>OKR（Objectives and Key Results）是 Intel 和 Google 等公司广泛使用的目标管理方法：
 * <ul>
 *   <li>O（Objective）：定性描述"我要实现什么"</li>
 *   <li>KR（Key Result）：量化指标"如何衡量是否实现了"</li>
 *   <li>好的 KR 应该是具体、可衡量、有时间限制的</li>
 * </ul>
 * 本系统中，GoalPlan 对应 O（目标），{@link GoalKr} 对应 KR（关键结果）。</p>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see GoalKr 目标关键结果实体
 * @see DailyPlan 每日计划实体（可通过 goalId 关联到目标）
 * @see com.dailytracker.mapper.GoalPlanMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_goal_plan")
public class GoalPlan extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此目标属于哪个用户。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 父目标ID - 自关联外键，指向本表（t_goal_plan）的主键 id
     * <p>
     * 用于实现目标的层级分解：
     * <ul>
     *   <li>为 NULL - 顶级目标（如五年愿景、年度目标）</li>
     *   <li>有值 - 子目标（如月度目标，parentId 指向年度目标）</li>
     * </ul>
     * 通过这种树形结构，用户可以将大目标逐级分解为小目标。
     * <p>
     * 数据库字段类型建议：BIGINT DEFAULT NULL，应建立索引
     */
    private Long parentId;

    /**
     * 目标标题 - 目标的简要描述
     * <p>
     * 应该是鼓舞人心的定性描述（OKR 中的 Objective）。
     * 例如："成为全栈开发工程师"、"保持健康的生活方式"。
     * <p>
     * 数据库字段类型建议：VARCHAR(200) NOT NULL
     */
    private String title;

    /**
     * 目标描述 - 目标的详细说明
     * <p>
     * 可以包含目标的具体内涵、达成标准、注意事项等。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String description;

    /**
     * 目标类型 - 目标的时间维度层级
     * <p>
     * <ul>
     *   <li>"FIVE_YEAR" - 五年愿景：长期人生规划</li>
     *   <li>"YEARLY" - 年度目标：每年的核心目标</li>
     *   <li>"MONTHLY" - 月度目标：每月的具体目标</li>
     *   <li>"WEEKLY" - 周目标：每周的重点目标</li>
     * </ul>
     * 不同类型的目标通过 parentId 形成层级关系：
     * 五年愿景 -> 年度目标 -> 月度目标 -> 周目标
     * <p>
     * 数据库字段类型建议：VARCHAR(20) NOT NULL
     */
    private String goalType;

    /**
     * 分类 - 目标所属的生活领域
     * <p>
     * <ul>
     *   <li>"CAREER" - 职业：职业发展、技能提升、项目成就等</li>
     *   <li>"STUDY" - 学习：学历提升、证书考取、知识积累等</li>
     *   <li>"HEALTH" - 健康：运动、饮食、睡眠、心理健康等</li>
     *   <li>"FINANCE" - 财务：收入增长、储蓄目标、投资计划等</li>
     *   <li>"LIFE" - 生活：旅行、社交、兴趣爱好、家庭等</li>
     *   <li>"OTHER" - 其他：不属于以上分类的目标</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String category;

    /**
     * 开始日期 - 目标的启动日期
     * <p>
     * 使用 {@link LocalDate} 类型。
     * 对于年度目标通常为 1 月 1 日，月度目标为每月 1 日。
     * <p>
     * 数据库字段类型建议：DATE
     */
    private LocalDate startDate;

    /**
     * 截止日期 - 目标的完成期限
     * <p>
     * 使用 {@link LocalDate} 类型。
     * 对于年度目标通常为 12 月 31 日，月度目标为月末最后一天。
     * 截止日期用于提醒用户目标的紧迫性和计算剩余时间。
     * <p>
     * 数据库字段类型建议：DATE
     */
    private LocalDate endDate;

    /**
     * 进度 - 目标的完成百分比
     * <p>
     * 范围 0~100 的整数：
     * <ul>
     *   <li>0 - 刚开始，未取得进展</li>
     *   <li>50 - 完成一半</li>
     *   <li>100 - 目标已完全达成</li>
     * </ul>
     * 进度可以根据下属关键结果（GoalKr）的完成度自动计算，
     * 也可以由用户手动调整。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0，CHECK 约束：progress BETWEEN 0 AND 100
     */
    private Integer progress;

    /**
     * 状态 - 目标当前的生命周期状态
     * <p>
     * 状态流转：NOT_STARTED -> IN_PROGRESS -> COMPLETED
     *                                      -> ABANDONED
     * <ul>
     *   <li>"NOT_STARTED" - 未开始：目标已创建但尚未启动</li>
     *   <li>"IN_PROGRESS" - 进行中：目标正在执行中</li>
     *   <li>"COMPLETED" - 已完成：目标已达成</li>
     *   <li>"ABANDONED" - 已放弃：因故放弃此目标</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：VARCHAR(20) DEFAULT 'NOT_STARTED'
     */
    private String status;

    /**
     * 优先级 - 目标的重要程度排序
     * <p>
     * 使用整数表示，值越小优先级越高。
     * 帮助用户在多个目标之间分配精力和时间。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer priority;

    /**
     * 排序 - 同级目标之间的显示顺序
     * <p>
     * 值越小排在越前面。用户可以通过拖拽调整目标的排列顺序。
     * 与 priority 不同，sortOrder 纯粹控制显示排列，不代表重要程度。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer sortOrder;
}
