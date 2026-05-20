package com.dailytracker.service;

import com.dailytracker.dto.request.SummaryCreateRequest;
import com.dailytracker.dto.response.SummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日总结服务接口（Summary Service Interface）
 *
 * 【职责说明】
 * 本接口负责每日总结/日记模块的业务逻辑，包括：
 * - 每日总结的 CRUD 操作
 * - 获取今日总结（快捷入口）
 * - 连续记录天数统计（打卡功能，类似 GitHub 贡献图）
 * - 情绪趋势分析（追踪情绪变化）
 *
 * 【业务领域说明】
 * 每日总结（DailySummary）是系统的核心功能之一，鼓励用户每天回顾总结。
 * 总结记录包含以下核心信息：
 * - 总结日期（summaryDate）：每天只能有一篇总结
 * - 心情评分（mood）：1-5 的整数评分
 * - 自评得分（score）：用户对自己一天表现的自评
 * - 感恩事项（gratitude）：JSON 格式的感恩列表
 * - 标签（tags）：JSON 格式的标签列表
 * - 内容（content）：总结正文
 *
 * 【设计模式】
 * - 唯一约束处理模式：
 *   每天只允许一篇总结。创建时如果发现已有被逻辑删除的记录，
 *   会恢复并更新该记录，而非插入新记录，避免唯一索引冲突。
 *
 * 【SOLID 原则体现】
 * - 单一职责：只关注每日总结的业务逻辑
 * - 迪米特法则：Controller 层不需要了解"逻辑删除恢复"等内部实现细节
 */
public interface SummaryService {

    /**
     * 创建每日总结
     *
     * 【业务流程】
     * 1. 获取当前用户ID
     * 2. 查询指定日期是否已有总结（包含已逻辑删除的记录）
     * 3. 如果存在活跃记录 -> 抛出"总结已存在"异常
     * 4. 如果存在已删除记录 -> 恢复并更新该记录（避免唯一索引冲突）
     * 5. 如果不存在任何记录 -> 创建新的总结记录
     *
     * @param request 创建请求 DTO，包含日期、内容、心情、评分、感恩事项、标签等
     * @return SummaryResponse 创建后的总结响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 当日总结已存在时抛出异常
     */
    SummaryResponse create(SummaryCreateRequest request);

    /**
     * 分页查询总结列表
     *
     * 【设计说明】
     * 使用简单的 LIMIT/OFFSET 分页实现，按总结日期降序排列。
     * 支持日期范围筛选，方便查看特定时间段的总结。
     *
     * @param pageNum   当前页码
     * @param pageSize  每页条数
     * @param startDate 开始日期（可为 null）
     * @param endDate   结束日期（可为 null）
     * @return 总结列表（按日期降序）
     */
    List<SummaryResponse> list(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate);

    /**
     * 获取总结详情
     *
     * @param id 总结ID
     * @return SummaryResponse 总结详情
     * @throws com.dailytracker.common.exception.BusinessException 总结不存在时抛出异常
     */
    SummaryResponse getById(Long id);

    /**
     * 更新总结
     *
     * @param id      总结ID
     * @param request 更新请求 DTO
     * @return SummaryResponse 更新后的总结响应 DTO
     */
    SummaryResponse update(Long id, SummaryCreateRequest request);

    /**
     * 删除总结
     *
     * @param id 总结ID
     */
    void delete(Long id);

    /**
     * 获取今日总结
     *
     * 快捷方法，查询当前用户今天的总结。如果今天还没有写总结，返回 null。
     *
     * @return 今日总结，不存在则返回 null
     */
    SummaryResponse getToday();

    /**
     * 获取连续记录天数（打卡统计）
     *
     * 【算法说明】
     * 1. 查询用户所有总结的日期列表（降序排列）
     * 2. 从最近的日期开始，向前逐天检查是否连续
     * 3. 统计当前连续天数（currentStreak）
     * 4. 遍历所有日期，计算历史最长连续天数（longestStreak）
     *
     * 【返回数据包含】
     * - currentStreak: 当前连续天数
     * - longestStreak: 历史最长连续天数
     * - totalDays: 总记录天数
     * - lastSummaryDate: 最后一次写总结的日期
     *
     * @return 打卡统计数据 Map
     */
    Map<String, Object> getStreak();

    /**
     * 获取情绪趋势数据（近N天）
     *
     * 【设计说明】
     * 查询最近 N 天的总结记录，提取日期和心情评分，
     * 用于前端绘制情绪变化折线图。帮助用户了解自己的情绪波动规律。
     *
     * @param days 查询天数（如 7 表示近一周，30 表示近一个月）
     * @return 情绪趋势列表，每项包含 date、mood、score
     */
    List<Map<String, Object>> getMoodTrend(int days);
}
