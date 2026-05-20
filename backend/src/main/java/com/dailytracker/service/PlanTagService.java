package com.dailytracker.service;

import com.dailytracker.dto.request.TagCreateRequest;
import com.dailytracker.dto.response.TagResponse;

import java.util.List;

/**
 * 计划标签服务接口（Plan Tag Service Interface）
 *
 * 【职责说明】
 * 本接口负责每日计划模块的标签管理功能，包括：
 * - 标签的 CRUD 操作（创建、查询、更新、删除）
 * - 任务与标签的关联管理（添加/移除标签）
 * - 查询任务关联的标签列表
 *
 * 【设计模式】
 * - 多对多关联管理模式：
 *   任务（DailyPlan）和标签（PlanTag）之间通过中间表（t_plan_tag_relation）关联。
 *   本接口封装了中间表的操作细节，上层代码只需调用 addTagsToPlan / removeTagsFromPlan。
 *
 * 【与 ExcerptService 中标签的区别】
 * - ExcerptService 使用独立的 Tag 实体和 ExcerptTagRel 中间表管理摘录标签
 * - PlanTagService 使用 PlanTag 实体和 t_plan_tag_relation 中间表管理计划标签
 * 两者虽然功能类似，但服务于不同的业务领域，各自独立管理。
 *
 * 【SOLID 原则体现】
 * - 单一职责：只关注计划标签的管理
 * - 接口隔离：标签的 CRUD 和关联操作在同一个接口中，因为它们紧密相关
 */
public interface PlanTagService {

    /**
     * 创建标签
     *
     * 【业务规则】
     * 同一用户下不允许创建同名标签。标签颜色默认为 #6366f1。
     *
     * @param request 创建请求 DTO，包含标签名称和颜色
     * @return TagResponse 创建后的标签响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 标签名称已存在时抛出异常
     */
    TagResponse create(TagCreateRequest request);

    /**
     * 获取用户的所有标签
     *
     * 返回当前用户创建的所有计划标签，按名称升序排列。
     *
     * @return 标签列表
     */
    List<TagResponse> list();

    /**
     * 更新标签
     *
     * 【业务规则】
     * - 更新名称时，需检查新名称是否与其他标签冲突
     * - 不能与其他标签重名
     *
     * @param id      标签ID
     * @param request 更新请求 DTO
     * @return TagResponse 更新后的标签响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 标签不存在或名称冲突时抛出异常
     */
    TagResponse update(Long id, TagCreateRequest request);

    /**
     * 删除标签
     *
     * 【业务流程】
     * 1. 校验标签存在且属于当前用户
     * 2. 删除中间表中该标签的所有关联记录
     * 3. 删除标签本身
     *
     * 注意：删除顺序很重要，先删关联再删标签，否则会留下孤立数据。
     *
     * @param id 标签ID
     */
    void delete(Long id);

    /**
     * 为任务添加标签（批量操作）
     *
     * 【设计说明】
     * 使用 INSERT IGNORE 语句，如果关联记录已存在则跳过（幂等操作）。
     * 通过 JdbcTemplate 的 batchUpdate 实现批量插入，提高效率。
     *
     * @param planId 任务ID
     * @param tagIds 标签ID列表
     */
    void addTagsToPlan(Long planId, List<Long> tagIds);

    /**
     * 从任务移除标签（批量操作）
     *
     * @param planId 任务ID
     * @param tagIds 要移除的标签ID列表
     */
    void removeTagsFromPlan(Long planId, List<Long> tagIds);

    /**
     * 获取任务的标签列表
     *
     * 【设计说明】
     * 使用原生 SQL 的 JOIN 查询，一次性获取任务的标签信息。
     * 这是比"先查关联表再查标签表"更高效的实现方式。
     *
     * @param planId 任务ID
     * @return 标签列表
     */
    List<TagResponse> getPlanTags(Long planId);
}
