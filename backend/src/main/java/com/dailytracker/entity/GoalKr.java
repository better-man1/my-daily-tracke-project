package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 目标关键结果实体类（Key Result）- OKR 方法论中的 KR，用于量化衡量目标的完成情况
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_goal_kr}</li>
 *   <li>注意：本实体<b>未继承</b> {@link com.dailytracker.common.base.BaseEntity}，
 *       而是直接实现 {@link Serializable} 接口，因此<b>没有</b>逻辑删除功能。</li>
 *   <li>主键通过 {@code @TableId(type = IdType.AUTO)} 显式声明。</li>
 * </ul>
 *
 * <p><b>【为什么 GoalKr 不使用逻辑删除？】</b></p>
 * <p>关键结果与目标紧密关联，通常不需要单独"软删除"。
 * 如果用户删除目标，关联的关键结果也应该被物理删除或级联处理。
 * 这种设计简化了数据管理，避免了逻辑删除带来的复杂查询问题。</p>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 GoalPlan 实体为多对一关系：多个关键结果属于一个目标（通过 goalId 关联）</li>
 *   <li>与 User 实体为多对一关系：通过 userId 关联，用于数据权限控制</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @TableName("t_goal_kr")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>关键结果是目标的具体量化指标，用于衡量目标是否达成。
 * 一个好的关键结果（Key Result）应该具备以下特征：
 * <ul>
 *   <li><b>具体</b>（Specific）：清晰明确，不含糊</li>
 *   <li><b>可衡量</b>（Measurable）：有明确的数字指标</li>
 *   <li><b>有时间限制</b>（Time-bound）：在目标的时间范围内完成</li>
 * </ul>
 * 例如，目标是"提升英语水平"，关键结果可以是：
 * <ul>
 *   <li>完成 50 篇英文文章阅读（targetValue=50, unit="篇"）</li>
 *   <li>雅思成绩达到 7.0 分（targetValue=7.0, unit="分"）</li>
 *   <li>背诵 3000 个单词（targetValue=3000, unit="个"）</li>
 * </ul>
 *
 * <p><b>【技术知识点 - 进度计算】</b></p>
 * <p>关键结果的进度可以通过以下公式自动计算：
 * <pre>
 *   progress = (currentValue / targetValue) * 100
 * </pre>
 * 也可以由用户手动调整 progress 字段。目标（GoalPlan）的总进度
 * 可以根据其下属所有关键结果的进度加权平均计算得出。</p>
 *
 * @see GoalPlan 目标计划实体，一个目标可以有多个关键结果
 * @see com.dailytracker.mapper.GoalKrMapper 对应的数据访问层
 */
@Data
@TableName("t_goal_kr")
public class GoalKr implements Serializable {

    /**
     * 主键ID - 关键结果的唯一标识
     * <p>
     * {@code @TableId(type = IdType.AUTO)} - 使用数据库自增策略生成主键。
     * 数据库字段类型建议：BIGINT PRIMARY KEY AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 目标ID - 关联 t_goal_plan 表的主键 id
     * <p>
     * 标识此关键结果属于哪个目标。一个目标下可以有多个关键结果。
     * 应建立索引以提高按目标查询关键结果的性能。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long goalId;

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 冗余字段，用于数据权限控制。虽然可以通过 GoalPlan -> userId 间接获取，
     * 但直接存储可以简化查询条件，避免不必要的 JOIN 操作。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL
     */
    private Long userId;

    /**
     * KR标题 - 关键结果的描述
     * <p>
     * 应该是一个可量化的具体指标。例如：
     * <ul>
     *   <li>"完成50篇英文阅读"</li>
     *   <li>"体重减到70公斤"</li>
     *   <li>"储蓄达到10万元"</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：VARCHAR(200) NOT NULL
     */
    private String title;

    /**
     * 目标值 - 关键结果需要达到的目标数值
     * <p>
     * 使用 {@link BigDecimal} 类型，支持整数和小数目标：
     * <ul>
     *   <li>整数目标：如阅读50篇、跑步100公里</li>
     *   <li>小数目标：如雅思7.5分、存款利率5.5%</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：DECIMAL(12,2)
     */
    private BigDecimal targetValue;

    /**
     * 当前值 - 当前已完成的数值
     * <p>
     * 与 targetValue 配合使用计算完成进度。
     * 用户每次更新关键结果时更新此字段。
     * <p>
     * 数据库字段类型建议：DECIMAL(12,2) DEFAULT 0
     */
    private BigDecimal currentValue;

    /**
     * 单位 - 数值的计量单位
     * <p>
     * 例如："篇"、"公里"、"公斤"、"分"、"元"、"次"等。
     * 用于前端展示时附加在数值后面，让数据更有可读性。
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String unit;

    /**
     * 进度 - 关键结果的完成百分比（0~100）
     * <p>
     * 可以由系统自动计算（currentValue / targetValue * 100），
     * 也可以由用户手动设置。
     * 目标的总进度可以根据所有关键结果的进度计算得出。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0，CHECK 约束：progress BETWEEN 0 AND 100
     */
    private Integer progress;

    /**
     * 排序 - 同一目标下关键结果的显示顺序
     * <p>
     * 值越小排在越前面。用户可以通过拖拽调整排列顺序。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer sortOrder;

    /**
     * 创建时间 - 关键结果的创建时间
     * <p>
     * 未使用 BaseEntity 的自动填充，需在业务层手动设置或依赖数据库默认值。
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 关键结果的最后修改时间
     * <p>
     * 用户更新 currentValue 或 progress 时会同步更新此字段。
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     */
    private LocalDateTime updatedAt;
}
