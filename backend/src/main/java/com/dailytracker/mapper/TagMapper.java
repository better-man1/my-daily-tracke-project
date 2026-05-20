package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签数据访问层（Mapper接口）- 提供摘录标签表（t_tag）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link Tag}</li>
 *   <li>数据库表：t_tag</li>
 *   <li>用于摘录（Excerpt）模块的标签管理</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_excerpt 表为多对多关系（通过 t_excerpt_tag_rel 中间表关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <Tag>} 后，自动拥有完整的单表操作方法。
 * 注意：Tag 未继承 BaseEntity，没有逻辑删除功能。</p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>查询用户所有标签：{@code selectList(new QueryWrapper<Tag>().eq("userId", userId).orderByDesc("usageCount"))}</li>
 *   <li>创建新标签：设置 userId、name、color，调用 {@code insert}</li>
 *   <li>更新标签使用次数：修改 usageCount 后调用 {@code updateById}</li>
 *   <li>删除标签：调用 {@code deleteById}（物理删除，同时应清理 t_excerpt_tag_rel 中的关联记录）</li>
 * </ul>
 *
 * <p><b>【与 PlanTagMapper 的区别】</b></p>
 * <ul>
 *   <li>本 Mapper 操作的是摘录标签（t_tag）</li>
 *   <li>{@link PlanTagMapper} 操作的是计划标签（t_plan_tag）</li>
 *   <li>两套标签独立管理，互不影响</li>
 * </ul>
 *
 * @see Tag 标签实体类
 * @see ExcerptTagRelMapper 摘录-标签关联的 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
