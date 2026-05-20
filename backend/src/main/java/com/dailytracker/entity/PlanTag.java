package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计划标签实体类 - 用于每日计划（DailyPlan）模块的标签分类管理
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_plan_tag}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多个标签属于一个用户（通过 userId 关联）</li>
 *   <li>与 DailyPlan 实体为多对多关系：通过中间表 t_plan_tag_relation 关联</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_plan_tag")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>计划标签用于对每日计划任务进行灵活的分类标记。与 DailyPlan 的 category 字段（固定分类）不同，
 * 标签提供了更自由的分类方式：
 * <ul>
 *   <li>category 是预定义的分类（工作/学习/生活/健康），每条记录只能选一个</li>
 *   <li>标签是用户自定义的，可以给一条计划打多个标签</li>
 * </ul>
 * 例如：一个"写项目报告"的计划可以同时打上"工作"和"重要"两个标签。</p>
 *
 * <p><b>【与 Tag（摘录标签）的区别】</b></p>
 * <p>本系统中有两套独立的标签体系：
 * <ul>
 *   <li>{@link PlanTag}（本实体） - 用于每日计划模块，存储在 t_plan_tag 表</li>
 *   <li>{@link Tag} - 用于摘录模块，存储在 t_tag 表</li>
 * </ul>
 * 两套标签的设计原因：计划标签和摘录标签的使用场景不同、字段需求不同，
 * 分开管理可以避免混淆，各自优化。</p>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see Tag 摘录标签实体（独立标签体系）
 * @see DailyPlan 每日计划实体
 * @see com.dailytracker.mapper.PlanTagMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_plan_tag")
public class PlanTag extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此标签属于哪个用户。每个用户有自己独立的标签集合。
     * 应建立唯一索引：UNIQUE(userId, name)，防止同一用户创建重名标签。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 标签名称 - 标签的显示文本
     * <p>
     * 例如："重要"、"紧急"、"专注"、"碎片时间"、"会议"等。
     * 用户在创建标签时自定义输入。
     * <p>
     * 数据库字段类型建议：VARCHAR(50) NOT NULL
     */
    private String name;

    /**
     * 标签颜色 - 标签的显示颜色
     * <p>
     * 支持 CSS 颜色值格式，例如：
     * <ul>
     *   <li>十六进制："#FF5733"</li>
     *   <li>RGB：rgb(255, 87, 51)</li>
     *   <li>颜色名："red"、"blue"</li>
     * </ul>
     * 前端在渲染标签时使用此颜色作为背景色或边框色。
     * <p>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String color;
}
