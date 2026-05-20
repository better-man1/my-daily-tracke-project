package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体类 - 定义用户每月的消费预算计划
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_budget}</li>
 *   <li>注意：本实体<b>未继承</b> {@link com.dailytracker.common.base.BaseEntity}，
 *       而是直接实现 {@link Serializable} 接口。</li>
 *   <li>主键通过 {@code @TableId(type = IdType.AUTO)} 显式声明。</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多条预算属于一个用户（通过 userId 关联）</li>
 *   <li>与 AccountingCategory 实体为多对一关系：预算可关联到某个收支分类（通过 categoryId 关联）</li>
 *   <li>与 Accounting 实体间接关联：通过 userId + categoryId + 月份，汇总当月实际支出来计算预算执行情况</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @TableName("t_budget")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>预算功能帮助用户控制每月消费，支持两种预算模式：
 * <ul>
 *   <li><b>总预算</b>（categoryId 为 null）：设置每月总支出上限</li>
 *   <li><b>分类预算</b>（categoryId 有值）：为特定分类（如餐饮、娱乐）设置独立预算</li>
 * </ul>
 * 前端会对比预算金额与当月实际支出，展示预算使用进度和预警信息。</p>
 *
 * <p><b>【设计模式 - 预算表与记账表的解耦】</b></p>
 * <p>预算和实际消费存储在不同的表中，通过 userId、categoryId 和月份进行关联查询。
 * 这种设计使得：
 * <ul>
 *   <li>预算可以独立于记账记录进行管理（先设预算，后记账）</li>
 *   <li>删除记账记录不影响已设定的预算</li>
 *   <li>可以灵活扩展预算维度（年度预算、周预算等）</li>
 * </ul>
 *
 * @see Accounting 记账明细实体
 * @see AccountingCategory 记账分类实体
 * @see com.dailytracker.mapper.BudgetMapper 对应的数据访问层
 */
@Data
@TableName("t_budget")
public class Budget implements Serializable {

    /**
     * 主键ID - 预算记录的唯一标识
     * <p>
     * {@code @TableId(type = IdType.AUTO)} - 使用数据库自增策略生成主键。
     * 数据库字段类型建议：BIGINT PRIMARY KEY AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此预算属于哪个用户。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 分类ID（NULL=总预算）- 关联 t_accounting_category 表的主键 id
     * <p>
     * <ul>
     *   <li>为 NULL - 表示这是用户的月度总预算，涵盖所有支出分类</li>
     *   <li>有具体值 - 表示这是针对特定支出分类的预算（如"餐饮预算 2000 元"）</li>
     * </ul>
     * 一个用户在一个月内，总预算只有一条记录，每个分类也只应有一条预算记录。
     * 应建立唯一索引：UNIQUE(userId, categoryId, budgetYear, budgetMonth)
     * <p>
     * 数据库字段类型建议：BIGINT DEFAULT NULL
     */
    private Long categoryId;

    /**
     * 预算年份 - 预算所属的年份
     * <p>
     * 与 budgetMonth 配合使用，确定预算的生效月份。
     * 例如 budgetYear=2024, budgetMonth=1 表示 2024 年 1 月的预算。
     * <p>
     * 数据库字段类型建议：INT NOT NULL
     */
    private Integer budgetYear;

    /**
     * 预算月份 - 预算所属的月份（1~12）
     * <p>
     * 数据库字段类型建议：INT NOT NULL，CHECK 约束：budgetMonth BETWEEN 1 AND 12
     */
    private Integer budgetMonth;

    /**
     * 预算金额 - 本月设定的支出上限
     * <p>
     * 使用 {@link BigDecimal} 类型确保金额精确。
     * 预算金额应为正数。实际支出超过预算金额时触发预警提醒。
     * <p>
     * 数据库字段类型建议：DECIMAL(12,2) NOT NULL
     */
    private BigDecimal amount;

    /**
     * 创建时间 - 预算记录的创建时间
     * <p>
     * 未使用 BaseEntity 的自动填充，需在业务层手动设置或依赖数据库默认值。
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 预算记录的最后修改时间
     * <p>
     * 用户修改预算金额时会更新此字段。
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
     */
    private LocalDateTime updatedAt;
}
