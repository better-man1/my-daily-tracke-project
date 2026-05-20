package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Excerpt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日摘录数据访问层（Mapper接口）- 提供摘录表（t_excerpt）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link Excerpt}</li>
 *   <li>数据库表：t_excerpt</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_tag 表为多对多关系（通过 t_excerpt_tag_rel 中间表关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <Excerpt>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(Excerpt entity)} - 新增一条摘录</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询摘录</li>
 *   <li>{@code selectList(Wrapper)} - 按条件查询摘录列表（如按用户、按来源类型、按收藏状态等）</li>
 *   <li>{@code selectPage(Page, Wrapper)} - 分页查询摘录列表</li>
 *   <li>{@code updateById(Excerpt entity)} - 更新摘录内容</li>
 *   <li>{@code deleteById(Long id)} - 删除摘录（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>创建摘录：设置 content、sourceType、thought 等，调用 {@code insert}</li>
 *   <li>查询用户的所有摘录：{@code selectList(new QueryWrapper<Excerpt>().eq("userId", userId).orderByDesc("createdAt"))}</li>
 *   <li>查询收藏的摘录：{@code selectList(new QueryWrapper<Excerpt>().eq("userId", userId).eq("isFavorite", 1))}</li>
 *   <li>按来源类型筛选：{@code selectList(new QueryWrapper<Excerpt>().eq("sourceType", "BOOK"))}</li>
 *   <li>搜索摘录内容：{@code selectList(new QueryWrapper<Excerpt>().like("content", keyword))}</li>
 * </ul>
 *
 * <p><b>【多对多关系查询】</b></p>
 * <p>查询带标签的摘录列表需要联合 t_excerpt_tag_rel 和 t_tag 表进行查询。
 * BaseMapper 只能操作单表，此类查询需要在 Service 层组合多个 Mapper 的结果，
 * 或编写自定义的多表 JOIN SQL。</p>
 *
 * @see Excerpt 每日摘录实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface ExcerptMapper extends BaseMapper<Excerpt> {
}
