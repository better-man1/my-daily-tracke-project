package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记账分类实体类 - 定义收支的分类体系（如餐饮、交通、工资等）
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_accounting_category}</li>
 *   <li>注意：本实体<b>未继承</b> {@link com.dailytracker.common.base.BaseEntity}，
 *       而是直接实现 {@link Serializable} 接口，因此没有 isDeleted 逻辑删除字段。</li>
 *   <li>主键通过 {@code @TableId(type = IdType.AUTO)} 显式声明，因为未继承 BaseEntity 的 id 字段。</li>
 * </ul>
 *
 * <p><b>【为什么此实体不继承 BaseEntity？】</b></p>
 * <p>记账分类是"字典数据"而非"业务数据"，通常不需要逻辑删除功能。
 * 系统预设分类不会被删除，用户自定义分类即使不再使用也不需要"回收站"功能。
 * 不继承 BaseEntity 可以使表结构更简洁。</p>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：用户自定义分类通过 userId 关联</li>
 *   <li>自关联关系：parentId 指向本表 id，支持分类的层级结构（如"餐饮"下有"早餐/午餐/晚餐"）</li>
 *   <li>与 Accounting 实体为一对多关系：一个分类可以被多条记账记录引用</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @TableName("t_accounting_category")} - 指定映射的数据库表名</li>
 *   <li>未使用 @EqualsAndHashCode(callSuper = true)，因为只实现 Serializable 接口，没有需要包含的父类字段</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>分类体系支持两层设计：
 * <ul>
 *   <li>系统预设分类（isSystem=1, userId=null）：所有用户共享，初始化时自动创建</li>
 *   <li>用户自定义分类（isSystem=0, userId=具体用户ID）：用户可自行添加个性化的收支分类</li>
 * </ul>
 * 前端展示分类列表时，查询条件为：userId = 当前用户 OR userId IS NULL（系统预设）</p>
 *
 * @see Accounting 记账明细实体，通过 categoryId 关联
 * @see com.dailytracker.mapper.AccountingCategoryMapper 对应的数据访问层
 */
@Data
@TableName("t_accounting_category")
public class AccountingCategory implements Serializable {

    /**
     * 主键ID - 分类的唯一标识
     * <p>
     * {@code @TableId(type = IdType.AUTO)} - MyBatis-Plus 注解，标记此字段为表的主键
     * <ul>
     *   <li>IdType.AUTO：使用数据库自增策略（MySQL 的 AUTO_INCREMENT）</li>
     *   <li>插入数据时不需要手动设置 id，数据库会自动生成</li>
     *   <li>插入后可以通过实体对象的 getId() 获取自动生成的 id</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：BIGINT PRIMARY KEY AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID - 标识此分类属于哪个用户
     * <p>
     * <ul>
     *   <li>为 NULL 时表示系统预设分类，所有用户可见</li>
     *   <li>有具体值时表示该用户自定义的分类，仅该用户可见</li>
     * </ul>
     * 这种设计模式称为"系统预设 + 用户扩展"，在 SaaS 应用中非常常见。
     * <p>
     * 数据库字段类型建议：BIGINT DEFAULT NULL
     */
    private Long userId;

    /**
     * 父分类ID - 自关联外键，指向本表的主键 id
     * <p>
     * 支持两级分类结构：
     * <ul>
     *   <li>为 NULL - 一级分类（如"餐饮"）</li>
     *   <li>有值 - 二级分类（如"早餐"、"午餐"、"晚餐"，parentId 指向"餐饮"的 id）</li>
     * </ul>
     * 数据库字段类型建议：BIGINT DEFAULT NULL
     */
    private Long parentId;

    /**
     * 分类名称 - 分类的显示名称
     * <p>
     * 例如："餐饮"、"交通"、"工资"、"理财收益"等。
     * 数据库字段类型建议：VARCHAR(50) NOT NULL
     */
    private String name;

    /**
     * 图标 - 分类的图标标识
     * <p>
     * 可以是图标类名（如 FontAwesome 的 "fa-utensils"），
     * 或者是图标图片的 URL，或者 Emoji 字符。
     * 前端根据此字段渲染对应的图标。
     * <p>
     * 数据库字段类型建议：VARCHAR(50)
     */
    private String icon;

    /**
     * 类型 - 分类适用的收支类型
     * <p>
     * <ul>
     *   <li>"INCOME" - 收入分类：工资、奖金、投资收益、兼职收入等</li>
     *   <li>"EXPENSE" - 支出分类：餐饮、交通、购物、娱乐、医疗等</li>
     * </ul>
     * 一个分类只能属于收入或支出其中一种类型。
     * <p>
     * 数据库字段类型建议：VARCHAR(10) NOT NULL
     */
    private String type;

    /**
     * 排序 - 同级分类之间的显示顺序
     * <p>
     * 值越小排在越前面。用户可以自定义分类的排列顺序。
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer sortOrder;

    /**
     * 是否系统预设 - 标识此分类是否为系统初始化时自动创建的预设分类
     * <p>
     * <ul>
     *   <li>1 - 系统预设分类，所有用户共享，不允许删除和修改名称</li>
     *   <li>0 - 用户自定义分类，可以删除和修改</li>
     * </ul>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer isSystem;

    /**
     * 创建时间 - 分类的创建时间
     * <p>
     * 注意：此字段未使用 @TableField(fill = FieldFill.INSERT) 注解，
     * 因为未继承 BaseEntity，不会触发自动填充。
     * 需要在业务层手动设置或依赖数据库 DEFAULT CURRENT_TIMESTAMP。
     * <p>
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP
     */
    private LocalDateTime createdAt;
}
