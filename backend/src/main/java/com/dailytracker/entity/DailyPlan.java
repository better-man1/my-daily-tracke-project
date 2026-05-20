package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 每日计划实体类 - 记录用户每天的任务/计划安排，是系统最核心的业务实体之一
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_daily_plan}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多个计划属于一个用户（通过 userId 关联）</li>
 *   <li>与 GoalPlan 实体为多对一关系：多个计划可关联同一个目标（通过 goalId 关联）</li>
 *   <li>自关联关系：parentId 指向本表的主键 id，实现任务与子任务的树形结构</li>
 *   <li>与 PlanTag 通过中间表（t_plan_tag_relation）建立多对多关系</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_daily_plan")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>每日计划是用户日常使用的核心功能。用户可以：
 * <ul>
 *   <li>创建每日任务，设置优先级、分类、预估时间</li>
 *   <li>使用子任务拆分复杂任务（通过 parentId 自关联）</li>
 *   <li>设置重复任务（如每日、每周重复），通过模板功能快速创建</li>
 *   <li>使用时间块（Time Block）功能进行时间管理</li>
 *   <li>将计划关联到长期目标（通过 goalId）</li>
 * </ul>
 *
 * <p><b>【技术知识点 - 时间类型映射】</b></p>
 * <ul>
 *   <li>{@link LocalDate} - 只有日期（年月日），对应数据库 DATE 类型，用于 planDate、repeatEndDate</li>
 *   <li>{@link LocalTime} - 只有时间（时分秒），对应数据库 TIME 类型，用于 startTime、endTime</li>
 *   <li>{@link LocalDateTime} - 日期+时间，对应数据库 DATETIME 类型，用于 completedAt</li>
 *   <li>MyBatis-Plus 从 3.x 版本开始原生支持 Java 8 日期时间 API，无需额外配置类型处理器</li>
 * </ul>
 *
 * <p><b>【技术知识点 - 树形结构设计】</b></p>
 * <p>子任务功能通过 parentId 字段实现邻接表（Adjacency List）模型：
 * <ul>
 *   <li>parentId 为 null 表示顶级任务</li>
 *   <li>parentId 有值表示是某个任务的子任务</li>
 *   <li>subtaskCount 和 completedSubtaskCount 用于快速显示进度，避免每次都 COUNT 查询</li>
 * </ul>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see GoalPlan 通过 goalId 关联的目标计划实体
 * @see PlanReminder 计划关联的提醒记录
 * @see com.dailytracker.mapper.DailyPlanMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_daily_plan")
public class DailyPlan extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此计划属于哪个用户。所有数据操作都需要带此条件，确保数据隔离。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 关联目标ID - 关联 t_goal_plan 表的主键 id
     * <p>
     * 可为 null，表示此计划不关联任何长期目标。
     * 当用户在目标页面创建关联计划时，此字段被赋值。
     * 用于追踪每日任务对长期目标的贡献。
     * <p>
     * 数据库字段类型建议：BIGINT，可建立外键约束或索引
     */
    private Long goalId;

    /**
     * 任务标题 - 任务的简要描述
     * <p>
     * 必填字段，用户创建任务时输入。
     * 数据库字段类型建议：VARCHAR(200) NOT NULL
     */
    private String title;

    /**
     * 任务描述 - 任务的详细说明
     * <p>
     * 可选字段，用于记录任务的详细步骤、注意事项等。
     * 数据库字段类型建议：TEXT
     */
    private String description;

    /**
     * 计划日期 - 任务所属的日期
     * <p>
     * 使用 {@link LocalDate} 类型，只存储年月日信息。
     * 这是任务列表按日期分组和查询的核心字段。
     * 数据库字段类型建议：DATE NOT NULL，应建立索引（与 userId 组合索引）
     */
    private LocalDate planDate;

    /**
     * 优先级 - 任务的紧急重要程度
     * <p>
     * 采用四象限优先级体系（类似 P 级别系统）：
     * <ul>
     *   <li>"P0" - 紧急且重要，最高优先级，需要立即处理</li>
     *   <li>"P1" - 重要但不紧急，需要规划时间处理</li>
     *   <li>"P2" - 紧急但不重要，可以委托他人或快速处理</li>
     *   <li>"P3" - 不紧急不重要，可以在空闲时处理</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(5) DEFAULT 'P2'
     */
    private String priority;

    /**
     * 分类 - 任务所属的生活领域
     * <p>
     * 预定义分类：
     * <ul>
     *   <li>"WORK" - 工作相关</li>
     *   <li>"STUDY" - 学习成长</li>
     *   <li>"LIFE" - 日常生活</li>
     *   <li>"HEALTH" - 健康运动</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String category;

    /**
     * 预估时间（分钟）- 用户预估完成任务所需的时间
     * <p>
     * 用于时间管理分析，帮助用户提高时间预估能力。
     * 与 actualMins 配合使用，可以在总结时对比预估与实际的差异。
     * 数据库字段类型建议：INT
     */
    private Integer estimatedMins;

    /**
     * 实际时间（分钟）- 任务完成后记录的实际耗时
     * <p>
     * 用户手动填写或通过计时功能自动记录。
     * 与 estimatedMins 对比可用于生成时间管理分析报告。
     * 数据库字段类型建议：INT
     */
    private Integer actualMins;

    /**
     * 状态 - 任务当前的生命周期状态
     * <p>
     * 状态机流转：TODO -> IN_PROGRESS -> DONE
     *                                    -> CANCELLED
     * <ul>
     *   <li>"TODO" - 待办，刚创建的初始状态</li>
     *   <li>"IN_PROGRESS" - 进行中，用户开始执行任务</li>
     *   <li>"DONE" - 已完成，任务执行完毕</li>
     *   <li>"CANCELLED" - 已取消，因故放弃执行</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(20) DEFAULT 'TODO'
     */
    private String status;

    /**
     * 排序权重 - 控制任务在同一天内的显示顺序
     * <p>
     * 值越小排在越前面。用户可以通过拖拽调整任务顺序，前端将新顺序更新到后端。
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer sortOrder;

    /**
     * 完成时间 - 任务被标记为完成的时间戳
     * <p>
     * 当 status 变为 "DONE" 时记录。可用于统计完成时间分布和效率分析。
     * 为 null 表示任务尚未完成。
     * <p>
     * 使用 {@link LocalDateTime} 类型，精确记录日期和时间。
     * 数据库字段类型建议：DATETIME
     */
    private LocalDateTime completedAt;

    /**
     * 是否为模板 - 标记此计划是否为可复用的模板
     * <p>
     * 模板功能允许用户保存常用的任务结构，在创建新任务时快速应用：
     * <ul>
     *   <li>0 - 不是模板，是普通计划任务</li>
     *   <li>1 - 是模板，不会出现在日常任务列表中</li>
     * </ul>
     * 模板可以包含预设的标题、描述、优先级、分类等信息。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer isTemplate;

    /**
     * 模板名称 - 模板的标识名称
     * <p>
     * 仅当 isTemplate = 1 时有效。
     * 用于在模板选择列表中展示。
     * 数据库字段类型建议：VARCHAR(100)
     */
    private String templateName;

    /**
     * 重复类型 - 任务的重复周期类型
     * <p>
     * 支持的重复模式：
     * <ul>
     *   <li>"NONE" - 不重复（默认）</li>
     *   <li>"DAILY" - 每日重复</li>
     *   <li>"WEEKLY" - 每周重复</li>
     *   <li>"MONTHLY" - 每月重复</li>
     *   <li>"CUSTOM" - 自定义重复规则</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(20) DEFAULT 'NONE'
     */
    private String repeatType;

    /**
     * 重复模式（JSON格式）- 存储自定义重复规则的详细配置
     * <p>
     * 当 repeatType 为 "CUSTOM" 时，此字段存储 JSON 格式的详细重复规则，例如：
     * <pre>
     * {
     *   "interval": 2,           // 每2周重复一次
     *   "daysOfWeek": [1, 3, 5], // 周一、周三、周五
     *   "dayOfMonth": 15         // 每月15号
     * }
     * </pre>
     * 对于非自定义重复类型，此字段可为 null。
     * <p>
     * 数据库字段类型建议：JSON 或 VARCHAR(500)
     */
    private String repeatPattern;

    /**
     * 重复结束日期 - 重复任务的终止日期
     * <p>
     * 为 null 表示无限期重复。
     * 到达此日期后，系统不再自动生成新的重复任务。
     * <p>
     * 数据库字段类型建议：DATE
     */
    private LocalDate repeatEndDate;

    /**
     * 父任务ID - 自关联外键，指向本表（t_daily_plan）的主键 id
     * <p>
     * 用于实现任务的层级结构（子任务功能）：
     * <ul>
     *   <li>为 null - 顶级任务</li>
     *   <li>有值 - 为指定 id 的任务的子任务</li>
     * </ul>
     * 这种设计称为"邻接表模型"（Adjacency List），是关系型数据库中实现树形结构的常用方案。
     * 优点是结构简单、易于理解；缺点是查询所有层级需要递归。
     * <p>
     * 数据库字段类型建议：BIGINT，应建立索引
     */
    private Long parentId;

    /**
     * 子任务数量 - 该任务下的直接子任务总数
     * <p>
     * 冗余字段，用于快速显示任务进度（如 "3/5 已完成"），避免每次都执行 COUNT 查询。
     * 创建/删除子任务时需要同步更新此字段。
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer subtaskCount;

    /**
     * 已完成子任务数量 - 该任务下已完成的子任务数
     * <p>
     * 与 subtaskCount 配合使用，计算完成率：completedSubtaskCount / subtaskCount。
     * 子任务状态变为 DONE 时 +1，变为非 DONE 时 -1。
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer completedSubtaskCount;

    /**
     * 开始时间 - 时间块的开始时间
     * <p>
     * 使用 {@link LocalTime} 类型，只存储时分秒。
     * 配合 endTime 和 isTimeblock 实现时间块（Time Block）功能，
     * 即将一天的时间划分为若干时间段，每个时间段分配特定任务。
     * <p>
     * 时间块是一种高效的时间管理方法，源自《番茄工作法》和卡尔·纽波特的"深度工作"理念。
     * <p>
     * 数据库字段类型建议：TIME
     */
    private LocalTime startTime;

    /**
     * 结束时间 - 时间块的结束时间
     * <p>
     * 应晚于 startTime。前端应做校验。
     * 数据库字段类型建议：TIME
     */
    private LocalTime endTime;

    /**
     * 是否为时间块 - 标记此计划是否为时间块类型
     * <p>
     * <ul>
     *   <li>0 - 普通任务</li>
     *   <li>1 - 时间块任务，具有 startTime 和 endTime</li>
     * </ul>
     * 时间块在日历/时间轴视图中以区块形式展示，直观显示时间分配情况。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer isTimeblock;
}
