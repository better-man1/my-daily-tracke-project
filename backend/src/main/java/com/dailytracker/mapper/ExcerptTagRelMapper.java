package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.ExcerptTagRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 摘录-标签关联数据访问层（Mapper接口）- 提供摘录与标签关联表（t_excerpt_tag_rel）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link ExcerptTagRel}</li>
 *   <li>数据库表：t_excerpt_tag_rel（多对多关联中间表）</li>
 *   <li>将摘录（t_excerpt）和标签（t_tag）关联起来</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <ExcerptTagRel>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(ExcerptTagRel entity)} - 为摘录添加标签关联</li>
 *   <li>{@code selectList(Wrapper)} - 查询关联列表（如查询某个摘录的所有标签、某个标签的所有摘录）</li>
 *   <li>{@code delete(Wrapper)} - 删除关联（如移除摘录上的某个标签）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>为摘录添加标签：设置 excerptId 和 tagId，调用 {@code insert}</li>
 *   <li>查询摘录的所有标签ID：{@code selectList(new QueryWrapper<ExcerptTagRel>().eq("excerptId", excerptId))}
 *       得到 tagId 列表，再用 TagMapper 查询标签详情</li>
 *   <li>移除摘录的标签：{@code delete(new QueryWrapper<ExcerptTagRel>().eq("excerptId", excerptId).eq("tagId", tagId))}</li>
 *   <li>删除摘录时清理所有关联：{@code delete(new QueryWrapper<ExcerptTagRel>().eq("excerptId", excerptId))}</li>
 * </ul>
 *
 * <p><b>【中间表操作的注意事项】</b></p>
 * <ul>
 *   <li>插入前应检查是否已存在相同的关联（excerptId + tagId 唯一）</li>
 *   <li>删除摘录时应级联清理本表中的关联记录</li>
 *   <li>删除标签时应级联清理本表中的关联记录，并更新 Tag 的 usageCount</li>
 * </ul>
 *
 * @see ExcerptTagRel 摘录-标签关联实体类
 * @see ExcerptMapper 摘录 Mapper
 * @see TagMapper 标签 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface ExcerptTagRelMapper extends BaseMapper<ExcerptTagRel> {
}
