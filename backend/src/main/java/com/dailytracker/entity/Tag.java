package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体类 - 用于摘录内容的分类标记
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_tag}</li>
 *   <li>注意：本实体<b>未继承</b> {@link com.dailytracker.common.base.BaseEntity}，
 *       而是直接实现 {@link Serializable} 接口。</li>
 *   <li>主键通过 {@code @TableId(type = IdType.AUTO)} 显式声明。</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多个标签属于一个用户（通过 userId 关联）</li>
 *   <li>与 Excerpt 实体为多对多关系：通过中间表 t_excerpt_tag_rel（{@link ExcerptTagRel}）关联</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @TableName("t_tag")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>标签是对摘录内容进行分类和索引的一种灵活机制。与固定分类不同，标签具有以下特点：
 * <ul>
 *   <li>用户可以自由创建标签，无需预设分类体系</li>
 *   <li>一条摘录可以有多个标签（多对多关系）</li>
 *   <li>标签可以通过使用频率反映用户的关注方向</li>
 *   <li>通过标签可以快速检索相关内容</li>
 * </ul>
 * </p>
 *
 * <p><b>【与 PlanTag 的区别】</b></p>
 * <p>本系统中有两套标签体系：
 * <ul>
 *   <li>{@link Tag}（本实体） - 用于摘录（Excerpt）模块的标签</li>
 *   <li>{@link PlanTag} - 用于每日计划（DailyPlan）模块的标签</li>
 * </ul>
 * 两套标签独立管理，互不影响。未来可以考虑合并为统一标签系统。</p>
 *
 * <p><b>【技术知识点 - 标签系统的常见设计模式】</b></p>
 * <p>标签系统通常有两种设计模式：
 * <ul>
 *   <li><b>扁平标签</b>（本系统采用）：所有标签平级，无层级关系。简单易用。</li>
 *   <li><b>层级标签</b>：标签有父子关系，形成分类树。功能更强大但复杂度更高。</li>
 * </ul>
 *
 * @see Excerpt 摘录实体
 * @see ExcerptTagRel 摘录-标签关联中间表实体
 * @see PlanTag 计划标签实体（独立标签体系）
 * @see com.dailytracker.mapper.TagMapper 对应的数据访问层
 */
@Data
@TableName("t_tag")
public class Tag implements Serializable {

    /**
     * 主键ID - 标签的唯一标识
     * <p>
     * {@code @TableId(type = IdType.AUTO)} - 使用数据库自增策略生成主键。
     * 数据库字段类型建议：BIGINT PRIMARY KEY AUTO_INCREMENT
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此标签属于哪个用户。每个用户有自己独立的标签集合。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 标签名 - 标签的显示文本
     * <p>
     * 例如："时间管理"、"读书笔记"、"职场"、"心理学"等。
     * 建议在数据库层面建立唯一索引：UNIQUE(userId, name)，
     * 确保同一用户不会创建重名标签。
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

    /**
     * 使用次数 - 该标签被引用的次数统计
     * <p>
     * 冗余字段，用于：
     * <ul>
     *   <li>标签列表按使用频率排序，高频标签排在前面</li>
     *   <li>展示"热门标签"或"常用标签"</li>
     *   <li>识别长期未使用的标签（usageCount=0），提示用户清理</li>
     * </ul>
     * 每次给摘录添加或移除此标签时，需要同步更新此字段的值。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer usageCount;

    /**
     * 创建时间 - 标签的创建时间
     * <p>
     * 未使用 BaseEntity 的自动填充，需在业务层手动设置或依赖数据库默认值。
     * 数据库字段类型建议：DATETIME DEFAULT CURRENT_TIMESTAMP
     */
    private LocalDateTime createdAt;
}
