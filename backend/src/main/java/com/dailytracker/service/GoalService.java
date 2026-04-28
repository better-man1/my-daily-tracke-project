package com.dailytracker.service;

import com.dailytracker.dto.request.GoalCreateRequest;
import com.dailytracker.dto.response.GoalResponse;

import java.util.List;
import java.util.Map;

/**
 * 目标管理服务接口
 */
public interface GoalService {

    /** 创建目标 */
    GoalResponse create(GoalCreateRequest request);

    /** 目标列表（支持类型/分类/状态筛选） */
    List<GoalResponse> list(String goalType, String category, String status);

    /** 目标详情 */
    GoalResponse getById(Long id);

    /** 更新目标 */
    GoalResponse update(Long id, GoalCreateRequest request);

    /** 删除目标 */
    void delete(Long id);

    /** 更新目标进度 */
    void updateProgress(Long id, Integer progress);

    /** 目标树形结构（含子目标） */
    List<GoalResponse> getTree(String goalType);

    /** 目标统计分析 */
    Map<String, Object> getStatistics();
}
