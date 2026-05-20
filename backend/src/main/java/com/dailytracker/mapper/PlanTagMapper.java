package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.PlanTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计划标签数据访问层（Mapper接口）- 提供计划标签表（t_plan_tag）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link PlanTag}</li>
 *   <li>数据库表：t_plan_tag</li>
 *   <li>用于每日计划（DailyPlan）模块的标签管理</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_daily_plan 表为多对多关系（通过 t_plan_tag_relation 中间表关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <PlanTag>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(PlanTag entity)} - 创建新标签</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询标签</li>
 *   <li>{@code selectList(Wrapper)} - 查询标签列表（如按用户查询所有标签）</li>
 *   <li>{@code updateById(PlanTag entity)} - 更新标签（名称、颜色）</li>
 *   <li>{@code deleteById(Long id)} - 删除标签（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>查询用户所有计划标签：{@code selectList(new QueryWrapper<PlanTag>().eq("userId", userId))}</li>
 *   <li>创建新标签：设置 userId、name、color，调用 {@code insert}</li>
 *   <li>修改标签颜色：修改 color 后调用 {@code updateById}</li>
 * </ul>
 *
 * <p><b>【与 TagMapper（摘录标签）的区别】</b></p>
 * <ul>
 *   <li>本 Mapper 操作的是计划标签（t_plan_tag），用于 DailyPlan 模块</li>
 *   <li>{@link TagMapper} 操作的是摘录标签（t_tag），用于 Excerpt 模块</li>
 *   <li>两套标签独立管理，数据不互通</li>
 * </ul>
 *
 * @see PlanTag 计划标签实体类
 * @see PlanTagRelationMapper 计划-标签关联 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface PlanTagMapper extends BaseMapper<PlanTag> {
}
