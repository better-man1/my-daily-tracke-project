package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.response.PlanResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日计划服务接口
 */
public interface DailyPlanService {

    /**
     * 创建任务
     */
    PlanResponse create(PlanCreateRequest request);

    /**
     * 按日期查询任务列表
     *
     * @param planDate 日期（null 表示今天）
     * @return 任务列表
     */
    List<PlanResponse> listByDate(LocalDate planDate);

    /**
     * 查询任务详情
     */
    PlanResponse getById(Long id);

    /**
     * 更新任务
     */
    PlanResponse update(Long id, PlanCreateRequest request);

    /**
     * 删除任务
     */
    void delete(Long id);

    /**
     * 更新任务状态
     *
     * @param id     任务ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 批量更新排序
     *
     * @param sortMap 任务ID -> 排序权重
     */
    void batchUpdateSort(Map<Long, Integer> sortMap);

    /**
     * 将任务顺延至下一天
     */
    PlanResponse postpone(Long id);

    /**
     * 获取任务统计（指定日期的完成率等）
     *
     * @param planDate 日期（null 表示今天）
     * @return 统计数据 Map
     */
    Map<String, Object> getStatistics(LocalDate planDate);

    /**
     * 保存为模板
     *
     * @param id           原任务ID
     * @param templateName 模板名称
     */
    void saveAsTemplate(Long id, String templateName);

    /**
     * 获取模板列表
     */
    List<PlanResponse> listTemplates();
}
