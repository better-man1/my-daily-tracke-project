package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.DailyPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日计划数据访问层（Mapper接口）- 提供每日计划表（t_daily_plan）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link DailyPlan}</li>
 *   <li>数据库表：t_daily_plan</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_goal_plan 表为多对一关系（通过 goalId 关联）</li>
 *   <li>自关联：parentId 指向本表 id（子任务功能）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <DailyPlan>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(DailyPlan entity)} - 创建新计划</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询计划</li>
 *   <li>{@code selectList(Wrapper)} - 根据条件查询计划列表（如按日期、按用户）</li>
 *   <li>{@code selectPage(Page, Wrapper)} - 分页查询计划列表</li>
 *   <li>{@code updateById(DailyPlan entity)} - 更新计划（状态、进度等）</li>
 *   <li>{@code deleteById(Long id)} - 删除计划（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>查询用户某天的所有计划：{@code selectList(new QueryWrapper<DailyPlan>().eq("userId", userId).eq("planDate", date))}</li>
 *   <li>更新任务状态为已完成：先设置 status="DONE" 和 completedAt，再 {@code updateById}</li>
 *   <li>查询某个任务的子任务：{@code selectList(new QueryWrapper<DailyPlan>().eq("parentId", parentId))}</li>
 *   <li>查询关联某个目标的所有计划：{@code selectList(new QueryWrapper<DailyPlan>().eq("goalId", goalId))}</li>
 * </ul>
 *
 * <p><b>【如何自定义复杂查询】</b></p>
 * <p>当需要多表关联查询（如查询计划同时带出目标名称、标签列表）时，
 * BaseMapper 的单表查询无法满足，此时需要：
 * <ul>
 *   <li>在本接口中声明自定义方法</li>
 *   <li>使用 @Select 注解或在 XML 文件中编写 JOIN SQL</li>
 * </ul>
 *
 * @see DailyPlan 每日计划实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface DailyPlanMapper extends BaseMapper<DailyPlan> {
}
