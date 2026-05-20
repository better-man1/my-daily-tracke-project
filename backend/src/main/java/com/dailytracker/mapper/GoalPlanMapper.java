package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.GoalPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 目标计划数据访问层（Mapper接口）- 提供目标计划表（t_goal_plan）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link GoalPlan}</li>
 *   <li>数据库表：t_goal_plan</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>自关联：parentId 指向本表 id（支持目标层级分解）</li>
 *   <li>与 t_goal_kr 表为一对多关系（一个目标有多个关键结果）</li>
 *   <li>与 t_daily_plan 表为一对多关系（一个目标可关联多个每日计划）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <GoalPlan>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(GoalPlan entity)} - 创建新目标</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询目标</li>
 *   <li>{@code selectList(Wrapper)} - 按条件查询目标列表（如按用户、按类型、按状态等）</li>
 *   <li>{@code selectPage(Page, Wrapper)} - 分页查询目标列表</li>
 *   <li>{@code updateById(GoalPlan entity)} - 更新目标（进度、状态等）</li>
 *   <li>{@code deleteById(Long id)} - 删除目标（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>创建年度目标：设置 userId、goalType="YEARLY"、title 等，调用 {@code insert}</li>
 *   <li>查询用户所有顶级目标：{@code selectList(new QueryWrapper<GoalPlan>().eq("userId", userId).isNull("parentId"))}</li>
 *   <li>查询某个目标的子目标：{@code selectList(new QueryWrapper<GoalPlan>().eq("parentId", parentId))}</li>
 *   <li>更新目标进度：修改 progress 和 status 后调用 {@code updateById}</li>
 *   <li>按类型查询：{@code selectList(new QueryWrapper<GoalPlan>().eq("userId", userId).eq("goalType", "YEARLY"))}</li>
 * </ul>
 *
 * <p><b>【树形结构查询的挑战】</b></p>
 * <p>由于目标通过 parentId 形成树形结构，BaseMapper 的单表查询无法一次性获取完整的树。
 * 常见解决方案：
 * <ul>
 *   <li>递归查询：先查顶级目标，再递归查每个目标的子目标（简单但有 N+1 查询问题）</li>
 *   <li>一次性查所有目标，在内存中组装树结构（适合数据量不大的场景）</li>
 *   <li>使用 MySQL 8.0+ 的 CTE（WITH RECURSIVE）递归查询（需要自定义 SQL）</li>
 * </ul>
 *
 * @see GoalPlan 目标计划实体类
 * @see GoalKrMapper 关键结果 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface GoalPlanMapper extends BaseMapper<GoalPlan> {
}
