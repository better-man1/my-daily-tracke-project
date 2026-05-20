package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计划-标签关联数据访问层（Mapper接口）- 管理每日计划（DailyPlan）与计划标签（PlanTag）之间的多对多关联
 *
 * <p><b>【对应表】</b></p>
 * <ul>
 *   <li>数据库表：t_plan_tag_relation（多对多关联中间表）</li>
 *   <li>注意：本 Mapper 的泛型参数为 {@code Object} 而非具体实体类，
 *       因为系统中没有定义对应的 PlanTagRelation 实体类。
 *       这意味着使用时需要通过 Wrapper 或自定义 SQL 来操作中间表。</li>
 * </ul>
 *
 * <p><b>【为什么泛型是 Object？】</b></p>
 * <p>本 Mapper 的声明为 {@code BaseMapper<Object>}，这是因为在系统设计时
 * 没有为 t_plan_tag_relation 表创建独立的实体类。
 * <ul>
 *   <li>优点：减少了一个实体类的维护成本</li>
 *   <li>缺点：无法使用 BaseMapper 的类型安全方法（如 insert、selectById）</li>
 * </ul>
 * 这种设计下，中间表的操作通常通过以下方式实现：
 * <ul>
 *   <li>使用自定义 SQL（@Insert、@Delete 注解）直接操作表</li>
 *   <li>使用 MyBatis 的 Map 参数传递 planId 和 tagId</li>
 * </ul>
 *
 * <p><b>【中间表结构推测】</b></p>
 * <p>根据关联关系，t_plan_tag_relation 表应该包含以下字段：
 * <ul>
 *   <li>id - 主键（可选，有些中间表不设主键）</li>
 *   <li>plan_id - 关联 t_daily_plan 表的主键 id</li>
 *   <li>tag_id - 关联 t_plan_tag 表的主键 id</li>
 * </ul>
 * 应建立唯一索引：UNIQUE(plan_id, tag_id)，防止重复关联。</p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>为计划添加标签：INSERT INTO t_plan_tag_relation(plan_id, tag_id) VALUES(?, ?)</li>
 *   <li>移除计划的标签：DELETE FROM t_plan_tag_relation WHERE plan_id = ? AND tag_id = ?</li>
 *   <li>查询计划的所有标签：SELECT tag_id FROM t_plan_tag_relation WHERE plan_id = ?</li>
 *   <li>删除计划时清理关联：DELETE FROM t_plan_tag_relation WHERE plan_id = ?</li>
 * </ul>
 *
 * <p><b>【改进建议】</b></p>
 * <p>建议创建 PlanTagRelation 实体类（类似 {@link com.dailytracker.entity.ExcerptTagRel}），
 * 使本 Mapper 的泛型从 {@code Object} 改为 {@code PlanTagRelation}，
 * 这样就可以使用 BaseMapper 的类型安全方法，代码更健壮、更易维护。</p>
 *
 * @see PlanTagMapper 计划标签 Mapper
 * @see DailyPlanMapper 每日计划 Mapper
 * @see com.dailytracker.entity.ExcerptTagRel 摘录模块的类似中间表实体（参考实现）
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface PlanTagRelationMapper extends BaseMapper<Object> {
}
