package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 每日总结实体类 - 记录用户每天的反思总结、心情日记和感恩记录
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_daily_summary}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多条总结属于一个用户（通过 userId 关联）</li>
 *   <li>与 DailyPlan 实体通过 userId 和日期间接关联：总结日期对应计划日期</li>
 *   <li>每个用户每天只有一条总结记录（userId + summaryDate 应建立唯一索引）</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_daily_summary")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>每日总结功能帮助用户进行每日反思（Daily Reflection），是个人成长和自我管理的核心环节。
 * 基于积极心理学（Positive Psychology）的理念，总结包含以下维度：
 * <ul>
 *   <li>心情与评分 - 量化当日整体感受</li>
 *   <li>成就与不足 - 客观回顾当日表现</li>
 *   <li>感恩记录 - 培养感恩习惯，提升幸福感</li>
 *   <li>明日计划 - 提前规划，减少焦虑</li>
 *   <li>健康记录 - 追踪身体状况</li>
 *   <li>自由日记 - 自由表达的空间</li>
 * </ul>
 *
 * <p><b>【技术知识点 - 逻辑删除与唯一索引】</b></p>
 * <p>由于本表使用了逻辑删除（isDeleted 字段），且 userId + summaryDate 是唯一约束，
 * 当用户删除某天的总结后又想重新创建时，会触发唯一索引冲突。
 * 解决方案：在 Mapper 层提供 selectByDateIgnoreDeleted 方法绕过逻辑删除查询，
 * 找到已删除的记录后使用 restoreAndUpdate 方法恢复并更新，而不是插入新记录。</p>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see com.dailytracker.mapper.DailySummaryMapper 对应的数据访问层（包含自定义SQL方法）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_daily_summary")
public class DailySummary extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此总结属于哪个用户。
     * 数据库字段类型建议：BIGINT NOT NULL
     */
    private Long userId;

    /**
     * 总结日期 - 此总结对应的日期
     * <p>
     * 使用 {@link LocalDate} 类型，只存储年月日。
     * 与 userId 组合应建立唯一索引：UNIQUE(userId, summaryDate, isDeleted) 或在业务层处理唯一性。
     * 数据库字段类型建议：DATE NOT NULL
     */
    private LocalDate summaryDate;

    /**
     * 心情 - 用户当日的心情评分
     * <p>
     * 使用 1~5 的整数表示五个心情等级：
     * <ul>
     *   <li>1 - 非常糟糕（Very Bad）</li>
     *   <li>2 - 比较差（Bad）</li>
     *   <li>3 - 一般（Normal）</li>
     *   <li>4 - 比较好（Good）</li>
     *   <li>5 - 非常好（Very Good）</li>
     * </ul>
     * 前端通常以表情图标（Emoji）形式展示，后端存储对应的数字值。
     * <p>
     * 数据库字段类型建议：INT，CHECK 约束：mood BETWEEN 1 AND 5
     */
    private Integer mood;

    /**
     * 今日评分 - 用户对当天整体表现的评分
     * <p>
     * 使用 1~10 的整数，提供比心情更细粒度的自我评价：
     * <ul>
     *   <li>1~3 - 低效日，大部分计划未完成</li>
     *   <li>4~6 - 普通日，完成了部分计划</li>
     *   <li>7~9 - 高效日，大部分计划顺利完成</li>
     *   <li>10 - 完美日，所有计划完成且有额外收获</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：INT，CHECK 约束：score BETWEEN 1 AND 10
     */
    private Integer score;

    /**
     * 今日成就 - 记录当天完成的重要事项和成就
     * <p>
     * 鼓励用户关注积极的方面，培养成就感。
     * 可以来自 DailyPlan 中已完成的任务自动汇总，也可以由用户手动填写。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String achievement;

    /**
     * 今日不足 - 反思当天做得不好的地方和改进方向
     * <p>
     * 帮助用户进行建设性反思（Constructive Reflection），而非自我批评。
     * 建议用户不仅记录问题，还思考改进方案。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String improvement;

    /**
     * 明日计划 - 用户对第二天的大致规划
     * <p>
     * 提前规划第二天可以提高睡眠质量（减少对未完成事项的焦虑），
     * 并让第二天一早就有清晰的行动方向。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String tomorrowPlan;

    /**
     * 感恩记录（JSON数组）- 当天的感恩事项列表
     * <p>
     * 以 JSON 数组格式存储多条感恩记录，例如：
     * <pre>
     * ["今天天气很好", "同事帮我解决了一个难题", "家人做了一顿好吃的晚餐"]
     * </pre>
     * 感恩练习（Gratitude Journal）是积极心理学中被证实最有效的幸福感提升方法之一。
     * <p>
     * 数据库字段类型建议：JSON 或 TEXT
     */
    private String gratitude;

    /**
     * 健康记录 - 当天的身体状况和健康相关信息
     * <p>
     * 可记录运动情况、睡眠质量、饮食状况等。
     * 为未来健康趋势分析提供数据基础。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String healthNote;

    /**
     * 自由日记 - 不限格式的自由写作空间
     * <p>
     * 用户可以在这里记录任何想法、灵感、情绪等。
     * 类似传统的日记本，不受结构化字段限制。
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String freeWriting;

    /**
     * 标签（JSON数组）- 总结的标签列表
     * <p>
     * 以 JSON 数组格式存储，例如：["高效", "运动", "阅读"]
     * 用于总结的分类和检索。与 PlanTag 不同，这里直接存储标签文本，
     * 不通过关联表关联，简化了数据结构。
     * <p>
     * 数据库字段类型建议：JSON 或 VARCHAR(500)
     */
    private String tags;
}
