package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 每日摘录实体类 - 记录用户从各种来源中摘录的有价值的内容和个人感悟
 *
 * <p><b>【数据库表映射】</b></p>
 * <ul>
 *   <li>对应数据库表：{@code t_excerpt}</li>
 *   <li>继承自 {@link BaseEntity}，包含 id、createdAt、updatedAt、isDeleted 公共字段</li>
 * </ul>
 *
 * <p><b>【ORM映射关系】</b></p>
 * <ul>
 *   <li>与 User 实体为多对一关系：多条摘录属于一个用户（通过 userId 关联）</li>
 *   <li>与 Tag 实体为多对多关系：通过中间表 t_excerpt_tag_rel（ExcerptTagRel）关联</li>
 * </ul>
 *
 * <p><b>【类注解说明】</b></p>
 * <ul>
 *   <li>{@code @Data} - Lombok 注解，自动生成 getter/setter/toString/equals/hashCode</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = true)} - 包含父类字段参与 equals 和 hashCode 计算</li>
 *   <li>{@code @TableName("t_excerpt")} - 指定映射的数据库表名</li>
 * </ul>
 *
 * <p><b>【业务场景】</b></p>
 * <p>摘录功能帮助用户收集和整理从各种渠道获取的有价值信息，构建个人知识库。
 * 支持：
 * <ul>
 *   <li>多来源摘录：书籍、文章、视频、播客等</li>
 *   <li>个人感悟：每条摘录可以附上自己的思考和见解</li>
 *   <li>标签管理：通过标签对摘录进行分类和检索</li>
 *   <li>收藏功能：标记重要的摘录便于快速查找</li>
 *   <li>图片记录：可以上传图片作为摘录的补充</li>
 * </ul>
 * 这种设计参考了"卡片笔记法"（Zettelkasten）和"渐进式总结"（Progressive Summarization）
 * 等知识管理方法论。</p>
 *
 * <p><b>【技术知识点 - 多对多关系的中间表设计】</b></p>
 * <p>摘录与标签之间是多对多关系（一条摘录可以有多个标签，一个标签可以打给多条摘录），
 * 通过中间表 {@link ExcerptTagRel}（t_excerpt_tag_rel）实现关联。
 * 中间表只需存储两个外键（excerptId 和 tagId）即可。</p>
 *
 * @see BaseEntity 父类，包含公共字段
 * @see Tag 标签实体
 * @see ExcerptTagRel 摘录-标签关联中间表实体
 * @see com.dailytracker.mapper.ExcerptMapper 对应的数据访问层
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_excerpt")
public class Excerpt extends BaseEntity {

    /**
     * 用户ID - 关联 t_user 表的主键 id
     * <p>
     * 标识此摘录属于哪个用户，确保数据隔离。
     * 数据库字段类型建议：BIGINT NOT NULL，应建立索引
     */
    private Long userId;

    /**
     * 摘录内容 - 用户摘录的原文内容
     * <p>
     * 可以是一段话、一句名言、一个观点等。
     * 建议前端提供富文本编辑器或 Markdown 支持。
     * <p>
     * 数据库字段类型建议：TEXT NOT NULL
     */
    private String content;

    /**
     * 来源类型 - 摘录内容的来源媒介类型
     * <p>
     * 预定义类型：
     * <ul>
     *   <li>"BOOK" - 书籍摘录</li>
     *   <li>"ARTICLE" - 网络文章/博客摘录</li>
     *   <li>"VIDEO" - 视频内容摘录（如 B站、YouTube）</li>
     *   <li>"PODCAST" - 播客/音频内容摘录</li>
     *   <li>"OTHER" - 其他来源（如线下对话、课程笔记等）</li>
     * </ul>
     * 数据库字段类型建议：VARCHAR(20)
     */
    private String sourceType;

    /**
     * 来源标题 - 摘录来源的标题
     * <p>
     * 例如：书名《原子习惯》、文章标题"如何高效学习"、视频标题等。
     * 便于用户回忆摘录的出处。
     * <p>
     * 数据库字段类型建议：VARCHAR(200)
     */
    private String sourceTitle;

    /**
     * 来源链接 - 摘录来源的URL地址
     * <p>
     * 对于网络文章、视频等在线资源，记录原始链接方便后续回溯。
     * 对于书籍等离线来源，此字段可以为 null。
     * <p>
     * 数据库字段类型建议：VARCHAR(500)
     */
    private String sourceUrl;

    /**
     * 个人感悟 - 用户对摘录内容的思考和见解
     * <p>
     * 这是摘录功能的核心价值所在。单纯摘录只是信息的搬运，
     * 加上个人感悟才是知识的内化。鼓励用户在摘录时记录：
     * <ul>
     *   <li>为什么觉得这段话有价值</li>
     *   <li>这段话如何与自己已有的知识产生关联</li>
     *   <li>可以如何应用到实际生活中</li>
     * </ul>
     * <p>
     * 数据库字段类型建议：TEXT
     */
    private String thought;

    /**
     * 图片URL列表（JSON）- 摘录附带的图片列表
     * <p>
     * 以 JSON 数组格式存储图片 URL，例如：
     * <pre>
     * ["https://oss.example.com/img/excerpt1.jpg"]
     * </pre>
     * 适用于拍摄书页、截图保存等场景。
     * <p>
     * 数据库字段类型建议：JSON 或 TEXT
     */
    private String images;

    /**
     * 是否收藏 - 标记此摘录是否被用户收藏
     * <p>
     * <ul>
     *   <li>0 - 未收藏</li>
     *   <li>1 - 已收藏</li>
     * </ul>
     * 收藏的摘录会出现在用户的"收藏列表"中，便于快速访问重要内容。
     * <p>
     * 数据库字段类型建议：INT DEFAULT 0
     */
    private Integer isFavorite;

    /**
     * 摘录日期 - 摘录创建的日期
     * <p>
     * 使用 {@link LocalDate} 类型，便于按日期筛选和归档。
     * 数据库字段类型建议：DATE，应建立索引
     */
    private LocalDate excerptDate;
}
