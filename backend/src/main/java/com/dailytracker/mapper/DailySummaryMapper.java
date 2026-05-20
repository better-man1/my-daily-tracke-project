package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.DailySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 每日总结数据访问层（Mapper接口）- 提供每日总结表（t_daily_summary）的数据库操作方法
 *
 * <p><b>【对应实体与表】</b></p>
 * <ul>
 *   <li>实体类：{@link DailySummary}</li>
 *   <li>数据库表：t_daily_summary</li>
 *   <li>与 t_user 表为多对一关系（通过 userId 关联）</li>
 *   <li>每个用户每天只有一条总结记录（userId + summaryDate 唯一）</li>
 * </ul>
 *
 * <p><b>【BaseMapper 自动提供的 CRUD 方法】</b></p>
 * <p>继承 {@link BaseMapper}{@code <DailySummary>} 后，自动拥有完整的单表操作方法。
 * 由于 DailySummary 继承了 BaseEntity（含 @TableLogic 的 isDeleted 字段），
 * 所有自动生成的 SELECT 语句都会自动追加 WHERE is_deleted = 0 条件。</p>
 *
 * <p><b>【为什么需要自定义SQL方法？】</b></p>
 * <p>DailySummary 面临一个特殊的业务问题：<b>逻辑删除与唯一索引冲突</b>。
 * <ul>
 *   <li>数据库表有唯一约束：UNIQUE(userId, summaryDate)</li>
 *   <li>用户删除某天的总结后，isDeleted 变为 1</li>
 *   <li>用户再想为同一天创建总结时，BaseMapper 的 insert 方法会执行 INSERT，
 *       但数据库中已存在相同 userId + summaryDate 的记录（虽然 isDeleted=1），
 *       导致唯一索引冲突报错</li>
 * </ul>
 * 解决方案：通过自定义 SQL 方法绕过逻辑删除机制，查找已删除的记录并恢复更新。</p>
 *
 * <p><b>【自定义方法说明】</b></p>
 * <ul>
 *   <li>{@link #selectByDateIgnoreDeleted} - 绕过逻辑删除，查询包括已删除在内的记录</li>
 *   <li>{@link #restoreAndUpdate} - 恢复已删除的记录并更新其内容</li>
 * </ul>
 *
 * <p><b>【@Select 和 @Update 注解说明】</b></p>
 * <ul>
 *   <li>{@code @Select} - MyBatis 注解，用于在接口方法上直接编写 SELECT SQL 语句</li>
 *   <li>{@code @Update} - MyBatis 注解，用于在接口方法上直接编写 UPDATE SQL 语句</li>
 *   <li>{@code @Param} - MyBatis 注解，用于给 SQL 中的参数命名，
 *       使得 SQL 中可以通过 #{参数名} 引用对应的 Java 方法参数</li>
 * </ul>
 *
 * @see DailySummary 每日总结实体类
 * @see BaseMapper MyBatis-Plus 提供的基础 Mapper 接口
 */
@Mapper
public interface DailySummaryMapper extends BaseMapper<DailySummary> {

    /**
     * 根据用户ID和日期查询总结记录（忽略逻辑删除标志）
     *
     * <p>此方法直接编写原生 SQL，绕过 MyBatis-Plus 的逻辑删除自动过滤机制。
     * 因为 BaseMapper 自动生成的 SELECT 语句会追加 WHERE is_deleted = 0，
     * 但我们在此场景下需要查找 is_deleted = 1 的记录（已被软删除的总结），
     * 所以必须手写 SQL 来实现。</p>
     *
     * @param userId      用户ID
     * @param summaryDate 总结日期
     * @return 该用户在该日期的总结记录（包括已逻辑删除的），不存在则返回 null
     */
    @Select("SELECT * FROM t_daily_summary WHERE user_id = #{userId} AND summary_date = #{summaryDate}")
    DailySummary selectByDateIgnoreDeleted(@Param("userId") Long userId, @Param("summaryDate") LocalDate summaryDate);

    /**
     * 恢复已删除的总结记录并更新其内容
     *
     * <p>将 is_deleted 字段从 1 改回 0（恢复记录），同时更新总结的所有业务字段。
     * 这样做的好处是：
     * <ul>
     *   <li>避免了 INSERT 操作导致的唯一索引冲突</li>
     *   <li>复用了已有记录，保留了原始的 id 和 created_at</li>
     *   <li>一条 SQL 完成恢复+更新，操作原子性有保障</li>
     * </ul>
     * </p>
     *
     * <p><b>SQL 解析：</b></p>
     * <pre>
     * UPDATE t_daily_summary
     * SET is_deleted = 0,              -- 恢复逻辑删除标志
     *     mood = #{s.mood},            -- 更新心情评分
     *     score = #{s.score},          -- 更新今日评分
     *     achievement = #{s.achievement},  -- 更新今日成就
     *     improvement = #{s.improvement},  -- 更新今日不足
     *     tomorrow_plan = #{s.tomorrowPlan}, -- 更新明日计划
     *     gratitude = #{s.gratitude},      -- 更新感恩记录
     *     health_note = #{s.healthNote},   -- 更新健康记录
     *     free_writing = #{s.freeWriting}, -- 更新自由日记
     *     tags = #{s.tags},               -- 更新标签
     *     updated_at = NOW()              -- 更新修改时间为当前时间
     * WHERE id = #{s.id}                  -- 根据主键定位记录
     * </pre>
     *
     * @param s 包含更新内容的 DailySummary 对象（必须包含 id 字段）
     * @return 受影响的行数（1 表示成功，0 表示未找到记录）
     */
    @Update("UPDATE t_daily_summary SET is_deleted = 0, mood = #{s.mood}, score = #{s.score}, achievement = #{s.achievement}, improvement = #{s.improvement}, tomorrow_plan = #{s.tomorrowPlan}, gratitude = #{s.gratitude}, health_note = #{s.healthNote}, free_writing = #{s.freeWriting}, tags = #{s.tags}, updated_at = NOW() WHERE id = #{s.id}")
    int restoreAndUpdate(@Param("s") DailySummary s);
}
