package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.PlanReminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务提醒数据访问层（Mapper接口）- 提供任务提醒表（t_plan_reminder）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link PlanReminder}</li>
 *   <li>数据库表：t_plan_reminder</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>与 t_daily_plan 表为多对一关系（通过 planId 关联）</li>
 *   <li>一个任务可以有多个提醒（一对多）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <PlanReminder>} 后，自动拥有完整的单表操作方法：
 * <ul>
 *   <li>{@code insert(PlanReminder entity)} - 为任务创建提醒</li>
 *   <li>{@code selectById(Long id)} - 根据ID查询提醒</li>
 *   <li>{@code selectList(Wrapper)} - 查询提醒列表（如查询未发送的提醒）</li>
 *   <li>{@code updateById(PlanReminder entity)} - 更新提醒（如标记为已发送）</li>
 *   <li>{@code deleteById(Long id)} - 删除提醒（逻辑删除）</li>
 * </ul>
 * </p>
 *
 * <p><b>【常见使用场景】</b></p>
 * <ul>
 *   <li>创建任务提醒：设置 planId、userId、reminderTime、reminderType，调用 {@code insert}</li>
 *   <li>查询待发送的提醒（定时任务使用）：
 *       {@code selectList(new QueryWrapper<PlanReminder>().eq("isSent", false).le("reminderTime", LocalDateTime.now()))}</li>
 *   <li>标记提醒为已发送：设置 isSent=true 后调用 {@code updateById}</li>
 *   <li>查询任务的所有提醒：{@code selectList(new QueryWrapper<PlanReminder>().eq("planId", planId))}</li>
 *   <li>删除任务时清理提醒：{@code delete(new QueryWrapper<PlanReminder>().eq("planId", planId))}</li>
 * </ul>
 *
 * <p><b>【定时任务扫描优化建议】</b></p>
 * <p>定时任务频繁扫描待发送提醒时，查询性能很关键：
 * <ul>
 *   <li>应在 reminder_time 和 is_sent 字段上建立复合索引</li>
 *   <li>索引建议：INDEX(is_sent, reminder_time)，使定时查询走索引</li>
 *   <li>避免全表扫描：查询条件应精确限定时间范围（如只查未来1小时内的提醒）</li>
 * </ul>
 *
 * @see PlanReminder 任务提醒实体类
 * @see DailyPlanMapper 每日计划 Mapper
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface PlanReminderMapper extends BaseMapper<PlanReminder> {
}
