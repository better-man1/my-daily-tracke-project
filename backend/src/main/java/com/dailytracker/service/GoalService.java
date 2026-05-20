package com.dailytracker.service;

import com.dailytracker.dto.request.GoalCreateRequest;
import com.dailytracker.dto.response.GoalResponse;

import java.util.List;
import java.util.Map;

/**
 * 目标管理服务接口（Goal Service Interface）
 *
 * 【职责说明】
 * 本接口负责个人目标管理模块的业务逻辑，支持多种类型的目标管理：
 * - 年度目标（YEARLY）、月度目标（MONTHLY）、周目标（WEEKLY）等
 * - 目标可以形成父子层级关系（树形结构）
 * - 每个目标可以关联多个关键结果（OKR 模式：Objective + Key Results）
 * - 支持进度追踪和自动状态更新
 *
 * 【设计模式】
 * - 树形结构模式（Tree / Composite Pattern）：
 *   目标支持父子层级关系（parentId 字段），getTree 方法返回完整的树形结构。
 *   这与"组合模式"的设计思想一致——将对象组合成树形结构以表示"部分-整体"的层次结构。
 *
 * - OKR 模式（Objectives and Key Results）：
 *   每个目标（Objective）可以关联多个关键结果（Key Results），
 *   通过量化关键结果来衡量目标的完成进度。
 *
 * 【SOLID 原则体现】
 * - 单一职责：只关注目标管理的业务逻辑
 * - 开闭原则：新增目标类型无需修改已有代码
 */
public interface GoalService {

    /**
     * 创建目标
     *
     * 【业务流程】
     * 1. 获取当前用户ID
     * 2. 如果指定了父目标，校验父目标存在且属于当前用户
     * 3. 创建目标记录
     * 4. 保存关联的关键结果（Key Results）
     *
     * @param request 目标创建请求 DTO，包含标题、描述、类型、分类、时间范围、关键结果等
     * @return GoalResponse 创建后的目标响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 父目标不存在时抛出异常
     */
    GoalResponse create(GoalCreateRequest request);

    /**
     * 目标列表（支持类型/分类/状态筛选）
     *
     * 【设计说明】
     * 支持三个可选的筛选条件，均为 null 时不筛选（返回全部）。
     * 使用 MyBatis-Plus 的条件构造器实现动态 SQL。
     * 结果按开始日期升序、ID降序排列。
     *
     * @param goalType 目标类型（YEARLY/MONTHLY/WEEKLY，可为 null）
     * @param category 目标分类（可为 null）
     * @param status   目标状态（NOT_STARTED/IN_PROGRESS/COMPLETED/ABANDONED，可为 null）
     * @return 目标响应列表
     */
    List<GoalResponse> list(String goalType, String category, String status);

    /**
     * 目标详情
     *
     * @param id 目标ID
     * @return GoalResponse 目标详情（包含关联的关键结果）
     * @throws com.dailytracker.common.exception.BusinessException 目标不存在时抛出异常
     */
    GoalResponse getById(Long id);

    /**
     * 更新目标
     *
     * 【业务流程】
     * 1. 校验目标归属
     * 2. 更新目标基本信息
     * 3. 重新保存关键结果（先删后增的简单策略）
     *
     * @param id      目标ID
     * @param request 更新请求 DTO
     * @return GoalResponse 更新后的目标响应 DTO
     */
    GoalResponse update(Long id, GoalCreateRequest request);

    /**
     * 删除目标
     *
     * 同时删除关联的关键结果（级联删除）。
     *
     * @param id 目标ID
     */
    void delete(Long id);

    /**
     * 更新目标进度
     *
     * 【自动状态更新机制】
     * - 进度设为 100% -> 状态自动变为 COMPLETED
     * - 进度从 0 变为 >0 且当前状态为 NOT_STARTED -> 自动变为 IN_PROGRESS
     * - 进度值会被限制在 0~100 之间
     *
     * @param id        目标ID
     * @param progress  新进度值（0-100）
     */
    void updateProgress(Long id, Integer progress);

    /**
     * 目标树形结构（含子目标）
     *
     * 【算法说明】
     * 1. 查询用户所有满足条件的目标
     * 2. 遍历所有目标，根据 parentId 构建父子关系
     * 3. 没有父目标的是根节点，有父目标的挂载到对应父节点的 children 列表
     * 4. 如果父目标因类型筛选未包含在结果中，子目标提升为当前视图的根节点
     *
     * @param goalType 目标类型（可为 null，表示查所有类型）
     * @return 树形结构的目标列表（只有根节点，子节点嵌套在 children 属性中）
     */
    List<GoalResponse> getTree(String goalType);

    /**
     * 目标统计分析
     *
     * 【返回数据包含】
     * - total: 目标总数
     * - notStarted/inProgress/completed/abandoned: 各状态数量
     * - byType: 按类型分组的数量统计
     * - byCategory: 按分类分组的数量统计
     * - avgProgress: 平均进度
     *
     * @return 统计数据 Map
     */
    Map<String, Object> getStatistics();
}
