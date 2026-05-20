package com.dailytracker.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类 - 所有数据库实体类的父类，包含公共字段
 *
 * 【类的用途】
 * 定义所有数据库表共有的字段：主键ID、创建时间、更新时间、逻辑删除标志。
 * 其他实体类（如 User、Plan、Goal 等）继承此类即可拥有这些公共字段，
 * 无需在每个实体类中重复定义。
 *
 * 【设计思想】
 * 1. 模板方法模式（Template Method）：抽取公共逻辑到基类
 * 2. 约定优于配置（Convention over Configuration）：统一字段命名和类型
 * 3. 配合 MybatisPlusConfig 中的 MetaObjectHandler 实现时间字段的自动填充
 *
 * 【在架构中的位置】
 * 属于通用基础层，被所有数据库实体类继承。
 * 是领域模型（Domain Model）的基础设施。
 *
 * 【相关技术知识点 - MyBatis-Plus 注解】
 * 1. @TableName：指定实体对应的数据库表名（如不指定，默认将类名转下划线作为表名）
 * 2. @TableId：标记主键字段
 * 3. @TableField：标记普通数据库字段
 * 4. @TableLogic：标记逻辑删除字段
 *
 * @Data Lombok 注解，自动生成 getter、setter、toString、equals、hashCode 方法
 */
@Data
public abstract class BaseEntity implements Serializable {

    /**
     * 主键ID - 数据库表的自增主键
     *
     * @TableId(type = IdType.AUTO) MyBatis-Plus 注解，标记此字段为主键
     *   - type = IdType.AUTO：主键生成策略为"数据库自增"
     *   - 其他策略：IdType.ASSIGN_ID（雪花算法）、IdType.ASSIGN_UUID（UUID）、IdType.INPUT（手动输入）
     *   - AUTO 策略下，插入数据时不需要设置 ID，数据库自动生成
     *
     * 使用 Long 类型而非 int/long，原因：
     * - Long 的最大值为 9223372036854775807（约 922 亿亿），足以应对大数据量
     * - 数据库中的 BIGINT 类型对应 Java 的 Long
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间 - 记录创建时的时间戳
     *
     * @TableField(fill = FieldFill.INSERT) MyBatis-Plus 注解，配置字段自动填充策略
     *   - FieldFill.INSERT：仅在执行 INSERT 操作时自动填充
     *   - 填充逻辑在 MybatisPlusConfig.insertFill() 方法中实现
     *   - 当执行 INSERT 时，如果此字段为 null，MyBatis-Plus 会自动设置为当前时间
     *
     * 使用 LocalDateTime 类型（Java 8+ 日期时间 API）：
     * - 精确到纳秒，无时区信息
     * - 对应数据库中的 DATETIME 类型
     * - 比 Date 类型更安全、更易用
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 记录最后修改的时间戳
     *
     * @TableField(fill = FieldFill.INSERT_UPDATE) 自动填充策略
     *   - FieldFill.INSERT_UPDATE：在 INSERT 和 UPDATE 操作时都自动填充
     *   - 新建记录时：填充为创建时间（由 MybatisPlusConfig.insertFill() 处理）
     *   - 更新记录时：自动更新为当前时间（由 MybatisPlusConfig.updateFill() 处理）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标志 - 标记记录是否已被"删除"
     *
     * @TableLogic MyBatis-Plus 注解，标记此字段为逻辑删除字段
     *
     * 【逻辑删除 vs 物理删除】
     * - 物理删除：执行 DELETE FROM table WHERE id = ?，数据从数据库中永久移除
     * - 逻辑删除：执行 UPDATE table SET is_deleted = 1 WHERE id = ?，数据保留但标记为"已删除"
     *
     * 【@TableLogic 的工作原理】
     * 添加此注解后，MyBatis-Plus 会自动改写 SQL：
     * - SELECT 语句自动追加 WHERE is_deleted = 0（只查询未删除的记录）
     * - DELETE 语句自动转为 UPDATE ... SET is_deleted = 1（标记删除而非真正删除）
     * - 对开发者透明，使用 mapper.selectList() 等方法时自动生效
     *
     * 值的约定：
     * - 0（默认）：未删除
     * - 1：已删除
     * 可在 application.yml 中自定义：mybatis-plus.global-config.db-config.logic-delete-value=1
     */
    @TableLogic
    private Integer isDeleted;
}
