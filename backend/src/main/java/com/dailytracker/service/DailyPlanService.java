package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.request.RepeatUpdateRequest;
import com.dailytracker.dto.response.PlanResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日计划服务接口（Daily Plan Service Interface）
 *
 * 【职责说明】
 * 本接口是系统中功能最丰富的服务之一，负责每日计划/任务管理的全部业务逻辑，包括：
 * - 任务的 CRUD（创建、查询、更新、删除）
 * - 任务状态管理（TODO / IN_PROGRESS / DONE / CANCELLED）
 * - 任务排序（手动拖拽排序）
 * - 任务顺延（推迟到下一天）
 * - 重复任务（每日/每周/每月/自定义间隔的重复规则）
 * - 子任务管理（创建子任务、级联更新父任务进度、子任务转主任务）
 * - 模板管理（将任务保存为模板、从模板创建任务）
 * - 时间块管理（时间分配、时间冲突检测）
 * - 批量操作（批量更新、删除、顺延、完成）
 * - 数据统计（完成率趋势、分类分布、优先级分布、时间分配）
 *
 * 【设计模式】
 * - 模板方法模式（Template Method）的变体：
 *   "保存为模板"和"从模板创建"形成了模板复用机制。
 *
 * - 组合模式（Composite Pattern）的变体：
 *   子任务与父任务形成树形结构，支持层级的任务管理。
 *
 * - 策略模式（Strategy Pattern）的变体：
 *   重复任务支持多种策略（每日/每周/每月/自定义），通过 repeatType 字段区分。
 *
 * 【SOLID 原则体现】
 * - 单一职责原则（SRP）：虽然方法较多，但都围绕"每日计划"这一核心领域。
 * - 接口隔离原则（ISP）注意：此接口方法较多，在实际大型项目中可考虑
 *   按功能拆分为多个小接口（如 PlanQueryService、PlanBatchService、PlanRepeatService 等）。
 */
public interface DailyPlanService {

    /**
     * 创建任务
     *
     * 【业务流程】
     * 1. 获取当前登录用户ID
     * 2. 将请求 DTO 转换为实体，设置初始状态为 TODO
     * 3. 插入数据库
     * 4. 如果设置了重复规则，自动生成未来 30 天的重复实例
     *
     * @param request 创建请求 DTO，包含标题、描述、日期、优先级、分类、时间等信息
     * @return PlanResponse 创建后的任务响应 DTO
     */
    PlanResponse create(PlanCreateRequest request);

    /**
     * 按日期查询任务列表
     *
     * 【设计说明】
     * 查询指定日期的所有非模板任务，按排序权重和ID升序排列。
     * planDate 为 null 时默认查询今天。这是前端"日视图"的数据来源。
     *
     * @param planDate 日期（null 表示今天）
     * @return 任务列表
     */
    List<PlanResponse> listByDate(LocalDate planDate);

    /**
     * 查询任务详情
     *
     * @param id 任务ID
     * @return PlanResponse 任务详情
     * @throws com.dailytracker.common.exception.BusinessException 任务不存在或不属于当前用户时抛出异常
     */
    PlanResponse getById(Long id);

    /**
     * 更新任务
     *
     * 【业务流程】
     * 1. 校验任务归属
     * 2. 复制属性并更新数据库
     * 3. 如果修改了重复规则，同步更新未来的重复实例
     * 4. 如果清除了重复规则，清理未来未完成的重复实例
     *
     * @param id      任务ID
     * @param request 更新请求 DTO
     * @return PlanResponse 更新后的任务响应 DTO
     */
    PlanResponse update(Long id, PlanCreateRequest request);

    /**
     * 删除任务
     *
     * @param id 任务ID
     */
    void delete(Long id);

    /**
     * 更新任务状态
     *
     * 【业务流程】
     * 1. 查询并校验任务归属
     * 2. 更新状态字段
     * 3. 如果状态变为 DONE，记录完成时间（completedAt）
     * 4. 如果从 DONE 改为其他状态，清除完成时间
     *
     * @param id     任务ID
     * @param status 新状态（TODO / IN_PROGRESS / DONE / CANCELLED）
     */
    void updateStatus(Long id, String status);

    /**
     * 批量更新排序
     *
     * 【设计说明】
     * 接收一个 Map（任务ID -> 排序权重），用于前端拖拽排序后的批量保存。
     * 使用 LambdaUpdateWrapper 进行条件更新，确保只更新属于当前用户的任务。
     *
     * @param sortMap 任务ID -> 排序权重（值越小越靠前）
     */
    void batchUpdateSort(Map<Long, Integer> sortMap);

    /**
     * 将任务顺延至下一天
     *
     * 【业务逻辑】
     * 将 planDate 加 1 天，如果任务已完成或已取消则重置为 TODO 状态。
     * 这是 GTD（Getting Things Done）方法论中常见的"推迟"操作。
     *
     * @param id 任务ID
     * @return PlanResponse 顺延后的任务响应 DTO
     */
    PlanResponse postpone(Long id);

    /**
     * 获取任务统计（指定日期的完成率等）
     *
     * 【返回数据包含】
     * - date: 统计日期
     * - total/done/inProgress/todo/cancelled: 各状态的任务数量
     * - completionRate: 完成率（百分比）
     * - totalEstimatedMins/totalActualMins: 预计/实际总耗时（分钟）
     *
     * @param planDate 日期（null 表示今天）
     * @return 统计数据 Map
     */
    Map<String, Object> getStatistics(LocalDate planDate);

    /**
     * 保存为模板
     *
     * 【设计说明】
     * 将已有任务的关键属性（标题、描述、优先级、分类等）复制一份，
     * 标记为模板（isTemplate=1）。后续用户可以基于模板快速创建新任务。
     * 这是"原型模式"（Prototype Pattern）的应用场景。
     *
     * @param id           原任务ID
     * @param templateName 模板名称
     */
    void saveAsTemplate(Long id, String templateName);

    /**
     * 获取模板列表
     *
     * @return 模板任务列表（isTemplate=1 的记录）
     */
    List<PlanResponse> listTemplates();

    /**
     * 生成重复任务实例
     *
     * 【业务流程】
     * 1. 获取原任务的重复规则
     * 2. 从 startDate 到 endDate 逐天判断是否需要创建任务
     * 3. 根据重复类型（每日/每周/每月/自定义）匹配日期
     * 4. 跳过已存在相同标题任务的日期（避免重复创建）
     * 5. 批量插入新任务实例
     *
     * @param planId    原任务ID（作为重复模板）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 生成的任务列表
     */
    List<PlanResponse> generateRepeatInstances(Long planId, LocalDate startDate, LocalDate endDate);

    /**
     * 更新重复规则
     *
     * @param id      任务ID
     * @param request 重复规则请求，包含 repeatType、repeatPattern、repeatEndDate
     * @return 更新后的任务
     */
    PlanResponse updateRepeatRule(Long id, RepeatUpdateRequest request);

    /**
     * 停止重复
     *
     * 将重复类型设为 NONE，并清除重复模式和结束日期。
     *
     * @param id 任务ID
     */
    void stopRepeat(Long id);

    /**
     * 创建子任务
     *
     * 【业务规则】
     * - 父任务不能是子任务（不支持多级嵌套，最多两级）
     * - 子任务继承父任务的计划日期
     * - 创建后更新父任务的子任务计数
     *
     * @param parentId 父任务ID
     * @param request  创建请求
     * @return 创建的子任务
     * @throws com.dailytracker.common.exception.BusinessException 父任务本身是子任务时抛出异常
     */
    PlanResponse createSubtask(Long parentId, PlanCreateRequest request);

    /**
     * 获取任务的子任务列表
     *
     * @param parentId 父任务ID
     * @return 子任务列表，按排序权重和ID升序排列
     */
    List<PlanResponse> getSubtasks(Long parentId);

    /**
     * 更新子任务状态（级联更新父任务进度）
     *
     * 【级联逻辑】
     * 当子任务状态变化时，重新统计父任务下所有子任务的完成情况，
     * 更新父任务的 subtaskCount 和 completedSubtaskCount 字段。
     *
     * @param id     子任务ID
     * @param status 新状态
     */
    void updateSubtaskStatus(Long id, String status);

    /**
     * 将子任务转换为主任务
     *
     * 【业务逻辑】
     * 清除子任务的 parentId，使其成为独立的主任务。
     * 同时更新原父任务的子任务计数。
     *
     * @param id 子任务ID
     * @throws com.dailytracker.common.exception.BusinessException 该任务不是子任务时抛出异常
     */
    void convertToMainTask(Long id);

    /**
     * 获取完成率趋势
     *
     * 【设计说明】
     * 逐天统计指定时间段内的任务完成率，为前端折线图提供数据。
     * 数据包含每天的总任务数、已完成数和完成率。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表，每项包含 date、total、done、completionRate
     */
    List<Map<String, Object>> getCompletionTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取分类分布
     *
     * 按任务分类分组统计数量，用于前端饼图/环形图展示。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类分布数据，包含 data（分类->数量映射）和 total（总数）
     */
    Map<String, Object> getCategoryDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取优先级分布
     *
     * 按任务优先级（P1/P2/P3/P4）分组统计数量。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 优先级分布数据
     */
    Map<String, Object> getPriorityDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取时间分配统计
     *
     * 按任务分类统计实际耗时（actualMins），用于分析时间投入分布。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 时间分配数据，包含 data（分类->分钟数映射）和 total（总分钟数）
     */
    Map<String, Object> getTimeDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 检测时间冲突
     *
     * 【算法说明】
     * 查询指定日期的所有时间块任务，两两比较时间段是否重叠。
     * 时间重叠判断条件：start1 < end2 && end1 > start2
     *
     * @param planDate 计划日期
     * @return 冲突的任务列表（包含所有参与冲突的时间块）
     */
    List<PlanResponse> detectTimeConflicts(LocalDate planDate);

    /**
     * 获取当日时间块列表
     *
     * 查询标记为时间块（isTimeblock=1）的任务，按开始时间排序。
     *
     * @param planDate 计划日期
     * @return 时间块列表
     */
    List<PlanResponse> getTimeBlocks(LocalDate planDate);

    /**
     * 批量更新任务
     *
     * 【业务流程】
     * 1. 验证所有任务都属于当前用户
     * 2. 构建条件更新，只更新非 null 的字段
     * 3. 如果更新状态为 DONE，同时设置完成时间
     *
     * @param ids      任务ID列表
     * @param priority 新优先级（可选，P1/P2/P3/P4）
     * @param category 新分类（可选）
     * @param status   新状态（可选，TODO/IN_PROGRESS/DONE/CANCELLED）
     */
    void batchUpdate(List<Long> ids, String priority, String category, String status);

    /**
     * 批量删除任务
     *
     * 只删除属于当前用户的任务，保证数据安全性。
     *
     * @param ids 任务ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 批量顺延任务
     *
     * 将所有指定任务的日期设为明天，状态重置为 TODO。
     *
     * @param ids 任务ID列表
     */
    void batchPostpone(List<Long> ids);

    /**
     * 批量完成任务
     *
     * 使用 LambdaUpdateWrapper 进行条件批量更新，避免逐条操作数据库。
     * 同时设置完成时间为当前时间。
     *
     * @param ids 任务ID列表
     */
    void batchComplete(List<Long> ids);
}
