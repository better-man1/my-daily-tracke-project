package com.dailytracker.service;

import com.dailytracker.dto.request.TagCreateRequest;
import com.dailytracker.dto.response.TagResponse;

import java.util.List;

/**
 * 计划标签服务接口
 */
public interface PlanTagService {

    /**
     * 创建标签
     *
     * @param request 创建请求
     * @return 创建的标签
     */
    TagResponse create(TagCreateRequest request);

    /**
     * 获取用户的所有标签
     *
     * @return 标签列表
     */
    List<TagResponse> list();

    /**
     * 更新标签
     *
     * @param id      标签ID
     * @param request 更新请求
     * @return 更新后的标签
     */
    TagResponse update(Long id, TagCreateRequest request);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void delete(Long id);

    /**
     * 为任务添加标签
     *
     * @param planId 任务ID
     * @param tagIds 标签ID列表
     */
    void addTagsToPlan(Long planId, List<Long> tagIds);

    /**
     * 从任务移除标签
     *
     * @param planId 任务ID
     * @param tagIds 标签ID列表
     */
    void removeTagsFromPlan(Long planId, List<Long> tagIds);

    /**
     * 获取任务的标签
     *
     * @param planId 任务ID
     * @return 标签列表
     */
    List<TagResponse> getPlanTags(Long planId);
}
