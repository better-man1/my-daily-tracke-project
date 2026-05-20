package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.GoalKr;
import org.apache.ibatis.annotations.Mapper;

/**
 * 目标关键结果数据访问层（Mapper接口）- 提供目标关键结果表（t_goal_kr）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link GoalKr}</li>
 *   <li>数据库表：t_goal_kr</li>
 *   <li>与 t_goal_plan 表为多对一关系（通过 goalId 关联）</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 冗余关联）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <GoalKr>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(GoalKr entity)} - 为目标新增一个关键结果</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询关键结果</li>
 *   <li>{@code selectList(Wrapper)} - 查询关键结果列表（如按目标ID查询）</li>
 *   <li>{@code updateById(GoalKr entity)} - 更新关键结果（当前值、进度等）</li>
 *   <li>{@code deleteById(Long id)} - 物理删除关键结果（注意：无逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>为目标添加关键结果：设置 goalId、userId、title、targetValue、unit，调用 {@code insert}</li>
 *   <li>查询目标的所有关键结果：{@code selectList(new QueryWrapper<GoalKr>().eq("goalId", goalId).orderByAsc("sortOrder"))}</li>
 *   <li>更新关键结果进度：修改 currentValue 和 progress 后调用 {@code updateById}</li>
 *   <li>删除关键结果：调用 {@code deleteById}（物理删除，应同时更新目标的总进度）</li>
 * </ul>
 *
 * <p><b>【注意 - 无逻辑删除】</b></p>
 * <p>GoalKr 未继承 BaseEntity，没有 @TableLogic 注解字段。
 * deleteById 执行的是物理删除（DELETE FROM t_goal_kr WHERE id = ?），
 * 删除后数据不可恢复。删除前应提示用户确认。</p>
 *
 * @see GoalKr 目标关键结果实体类
 * @see GoalPlanMapper 目标计划 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface GoalKrMapper extends BaseMapper<GoalKr> {
}
