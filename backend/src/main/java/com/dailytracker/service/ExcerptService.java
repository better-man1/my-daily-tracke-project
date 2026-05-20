package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.ExcerptCreateRequest;
import com.dailytracker.dto.response.ExcerptResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日摘录服务接口（Excerpt Service Interface）
 *
 * 【职责说明】
 * 本接口负责每日摘录/读书笔记模块的业务逻辑，包括：
 * - 摘录的增删改查（支持丰富的筛选条件）
 * - 收藏管理（切换收藏状态）
 * - 标签管理（创建标签、按标签筛选）
 * - 全文搜索（按内容、心得、来源标题搜索）
 * - 随机获取摘录（每日推荐功能）
 * - 数据导出为 Markdown 格式
 *
 * 【设计模式】
 * - 多对多关联模式：摘录与标签通过中间表（excerpt_tag_rel）关联，
 *   一个摘录可以有多个标签，一个标签也可以关联多个摘录。
 *
 * - 数据传输对象模式（DTO Pattern）：使用专门的 Request/Response DTO
 *   在 Service 层与 Controller 层之间传递数据，保护内部实体结构。
 *
 * 【SOLID 原则体现】
 * - 单一职责：只关注摘录相关的业务逻辑
 * - 迪米特法则（LoD）：Controller 层不需要了解摘录与标签之间的关联细节
 */
public interface ExcerptService {

    /**
     * 创建摘录
     *
     * 【业务流程】
     * 1. 获取当前用户ID
     * 2. 将请求 DTO 转换为实体，图片列表序列化为 JSON
     * 3. 插入数据库
     * 4. 处理标签关联（批量插入关联记录，更新标签使用计数）
     *
     * @param request 创建请求 DTO，包含内容、心得、来源、日期、图片、标签ID列表等
     * @return ExcerptResponse 创建后的摘录响应 DTO
     */
    ExcerptResponse create(ExcerptCreateRequest request);

    /**
     * 分页查询摘录列表
     *
     * 【设计说明】
     * 支持多条件筛选，所有筛选条件均为可选：
     * - 日期范围：startDate ~ endDate
     * - 来源类型：书籍/文章/视频等
     * - 标签筛选：通过中间表查询关联的摘录ID
     * - 收藏筛选：只看收藏的摘录
     *
     * 【标签筛选的实现策略】
     * 当指定 tagId 时，先查询中间表获取关联的摘录ID列表，
     * 再用 IN 条件过滤主查询。如果关联列表为空，直接返回空结果，
     * 避免无效的数据库查询。这种"两步查询"策略在多对多关联筛选中很常见。
     *
     * @param pageNum    当前页码
     * @param pageSize   每页条数
     * @param startDate  开始日期（可为 null）
     * @param endDate    结束日期（可为 null）
     * @param sourceType 来源类型（可为 null）
     * @param tagId      标签ID（可为 null）
     * @param isFavorite 是否收藏（1=是，0=否，null=不限）
     * @return 分页结果对象
     */
    IPage<ExcerptResponse> page(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate,
                                String sourceType, Long tagId, Integer isFavorite);

    /**
     * 获取摘录详情
     *
     * @param id 摘录ID
     * @return ExcerptResponse 摘录详情（含关联的标签列表）
     * @throws com.dailytracker.common.exception.BusinessException 摘录不存在时抛出异常
     */
    ExcerptResponse getById(Long id);

    /**
     * 更新摘录
     *
     * 【业务流程】
     * 1. 校验摘录归属
     * 2. 更新基本信息
     * 3. 如果传入了新的标签列表，先删除旧的关联再创建新的关联
     *
     * @param id      摘录ID
     * @param request 更新请求 DTO
     * @return ExcerptResponse 更新后的摘录响应 DTO
     */
    ExcerptResponse update(Long id, ExcerptCreateRequest request);

    /**
     * 删除摘录
     *
     * 同时删除摘录与标签的关联记录。
     *
     * @param id 摘录ID
     */
    void delete(Long id);

    /**
     * 切换收藏状态
     *
     * 将 isFavorite 字段在 0 和 1 之间切换（toggle 操作）。
     *
     * @param id 摘录ID
     */
    void toggleFavorite(Long id);

    /**
     * 随机获取一条摘录
     *
     * 【设计说明】
     * 从当前用户的所有摘录中随机选取一条，可用于"每日金句"等推荐功能。
     * 当前实现是查询所有摘录后在内存中随机选择，
     * 在数据量较大时，可优化为使用数据库的 ORDER BY RAND() LIMIT 1。
     *
     * @return 随机一条摘录，如果没有摘录则返回 null
     */
    ExcerptResponse getRandom();

    /**
     * 获取用户所有标签
     *
     * 返回按使用次数降序排列的标签列表，方便前端展示"常用标签"。
     *
     * @return 标签列表，每个元素包含 id、name、color、usageCount
     */
    List<Map<String, Object>> getAllTags();

    /**
     * 全文搜索
     *
     * 【设计说明】
     * 使用 SQL LIKE 进行模糊匹配，搜索范围包括：
     * - 摘录内容（content）
     * - 个人心得（thought）
     * - 来源标题（sourceTitle）
     * 三个字段之间使用 OR 条件，只要任一字段匹配即返回。
     *
     * 注意：LIKE 搜索在大数据量下性能有限，生产环境中建议使用
     * Elasticsearch 等专业全文检索引擎。
     *
     * @param keyword  搜索关键词
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页搜索结果
     */
    IPage<ExcerptResponse> search(String keyword, int pageNum, int pageSize);

    /**
     * 导出摘录为 Markdown 格式
     *
     * 【设计说明】
     * 将用户的所有摘录数据格式化为 Markdown 文本，便于备份和跨平台使用。
     * 输出结构：标题 -> 日期/类型/收藏标记 -> 原文 -> 心得 -> 分隔线
     *
     * @param userId 用户ID
     * @return Markdown 格式的字符串
     */
    String exportMarkdown(Long userId);

    /**
     * 创建标签
     *
     * 【业务逻辑】
     * 如果同名标签已存在，直接返回已有标签（幂等操作）。
     * 如果不存在，创建新标签并返回。颜色默认为 #6366f1（紫色）。
     *
     * @param name  标签名称
     * @param color 标签颜色（可为 null，使用默认颜色）
     * @return 标签信息 Map，包含 id、name、color、usageCount
     */
    Map<String, Object> createTag(String name, String color);
}
