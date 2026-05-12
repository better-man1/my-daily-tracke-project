package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.request.RepeatUpdateRequest;
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

    /**
     * 生成重复任务实例
     *
     * @param planId   原任务ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 生成的任务列表
     */
    List<PlanResponse> generateRepeatInstances(Long planId, LocalDate startDate, LocalDate endDate);

    /**
     * 更新重复规则
     *
     * @param id      任务ID
     * @param request 重复规则请求
     * @return 更新后的任务
     */
    PlanResponse updateRepeatRule(Long id, RepeatUpdateRequest request);

    /**
     * 停止重复
     *
     * @param id 任务ID
     */
    void stopRepeat(Long id);

    /**
     * 创建子任务
     *
     * @param parentId 父任务ID
     * @param request  创建请求
     * @return 创建的子任务
     */
    PlanResponse createSubtask(Long parentId, PlanCreateRequest request);

    /**
     * 获取任务的子任务列表
     *
     * @param parentId 父任务ID
     * @return 子任务列表
     */
    List<PlanResponse> getSubtasks(Long parentId);

    /**
     * 更新子任务状态（级联更新父任务进度）
     *
     * @param id     子任务ID
     * @param status 新状态
     */
    void updateSubtaskStatus(Long id, String status);

    /**
     * 将子任务转换为主任务
     *
     * @param id 子任务ID
     */
    void convertToMainTask(Long id);

    /**
     * 获取完成率趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表
     */
    List<Map<String, Object>> getCompletionTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取分类分布
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类分布数据
     */
    Map<String, Object> getCategoryDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取优先级分布
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 优先级分布数据
     */
    Map<String, Object> getPriorityDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取时间分配统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 时间分配数据
     */
    Map<String, Object> getTimeDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 检测时间冲突
     *
     * @param planDate 计划日期
     * @return 冲突的任务列表
     */
    List<PlanResponse> detectTimeConflicts(LocalDate planDate);

    /**
     * 获取当日时间块列表
     *
     * @param planDate 计划日期
     * @return 时间块列表
     */
    List<PlanResponse> getTimeBlocks(LocalDate planDate);

    /**
     * 批量更新任务
     *
     * @param ids     任务ID列表
     * @param priority 新优先级（可选）
     * @param category 新分类（可选）
     * @param status   新状态（可选）
     */
    void batchUpdate(List<Long> ids, String priority, String category, String status);

    /**
     * 批量删除任务
     *
     * @param ids 任务ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量顺延任务
     *
     * @param ids 任务ID列表
     */
    void batchPostpone(List<Long> ids);

    /**
     * 批量完成任务
     *
     * @param ids 任务ID列表
     */
    void batchComplete(List<Long> ids);
}
