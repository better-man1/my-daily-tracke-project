package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 摘录-标签关联实体类 - 摘录（Excerpt）与标签（Tag）之间的多对多关联中间表
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_excerpt_tag_rel}</li>
 *   <li>注意：本实体<b>未继承</b> {@link com.dailytracker.common.base.BaseEntity}，
 *       而是直接实现 {@link Serializable} 接口。</li>
 *   <li>主键通过 {@code @TableId(type = IdType.AUTO)} 显式声明。</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 Excerpt 实体为多对一关系：excerptId 指向 t_excerpt 表的主键</li>
 *   <li>与 Tag 实体为多对一关系：tagId 指向 t_tag 表的主键</li>
 *   <li>Excerpt 与 Tag 通过本表形成多对多关系</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @TableName("t_excerpt_tag_rel")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>中间表用于实现摘录和标签之间的多对多关系。当用户为某条摘录添加标签时，
 * 在此表中创建一条关联记录；移除标签时删除对应的关联记录。</p>
 *
 * <p><b>【技术知识点 - 多对多关系与中间表】</b></p>
 * <p>在关系型数据库中，多对多（Many-to-Many）关系无法直接用外键表示，
 * 必须通过中间表（也叫关联表、桥接表、junction table）来建立关联。
 * <ul>
 *   <li>中间表通常只包含两个外键字段（分别指向两张关联表的主键）</li>
 *   <li>应建立唯一索引：UNIQUE(excerptId, tagId)，防止重复关联</li>
 *   <li>中间表可以包含额外的字段（如关联创建时间），本实现选择了最简方案</li>
 * </ul>
 * </p>
 *
 * <p><b>【技术知识点 - 中间表的最小化设计】</b></p>
 * <p>本中间表只包含 id、excerptId、tagId 三个字段，是最小化的中间表设计。
 * 这种设计适合不需要记录关联元数据（如创建时间、关联权重等）的简单场景。</p>
 *
 * @see Excerpt 摘录实体
 * @see Tag 标签实体
 * @see com.dailytracker.mapper.ExcerptTagRelMapper 对应的数据访问层
 */
@Data
@TableName("t_excerpt_tag_rel")
public class ExcerptTagRel implements Serializable {

    /**
     * 主键ID - 关联记录的唯一标识
     * <p>
     * {@code @TableId(type = IdType.AUTO)} - 使用数据库自增策略生成主键。
     * 数据库字段类型建议：BIGINT PRIMARY KEY AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 摘录ID - 关联 t_excerpt 表的主键 id
     * <p>
     * 指向具体的某一条摘录记录。
     * 数据库字段类型建议：BIGINT NOT NULL
     */
    private Long excerptId;

    /**
     * 标签ID - 关联 t_tag 表的主键 id
     * <p>
     * 指向具体的某一个标签。
     * 与 excerptId 组合应建立唯一索引：UNIQUE(excerptId, tagId)，
     * 确保同一条摘录不会重复关联同一个标签。
     * <p>
     * 数据库字段类型建议：BIGINT NOT NULL
     */
    private Long tagId;
}
