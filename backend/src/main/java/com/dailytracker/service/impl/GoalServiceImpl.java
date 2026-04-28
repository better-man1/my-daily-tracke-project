package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.GoalCreateRequest;
import com.dailytracker.dto.response.GoalResponse;
import com.dailytracker.entity.GoalPlan;
import com.dailytracker.mapper.GoalPlanMapper;
import com.dailytracker.service.GoalService;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 目标管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalPlanMapper goalPlanMapper;

    @Override
    @Transactional
    public GoalResponse create(GoalCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查父目标是否存在
        if (request.getParentId() != null) {
            GoalPlan parent = goalPlanMapper.selectOne(
                    new LambdaQueryWrapper<GoalPlan>()
                            .eq(GoalPlan::getId, request.getParentId())
                            .eq(GoalPlan::getUserId, userId));
            if (parent == null) {
                throw new BusinessException(ResultCode.GOAL_PARENT_NOT_FOUND);
            }
        }

        GoalPlan goal = new GoalPlan();
        BeanUtils.copyProperties(request, goal);
        goal.setUserId(userId);
        goalPlanMapper.insert(goal);
        log.info("创建目标成功: userId={}, title={}", userId, goal.getTitle());
        return toResponse(goal);
    }

    @Override
    public List<GoalResponse> list(String goalType, String category, String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<GoalPlan> goals = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getUserId, userId)
                        .eq(goalType != null, GoalPlan::getGoalType, goalType)
                        .eq(category != null, GoalPlan::getCategory, category)
                        .eq(status != null, GoalPlan::getStatus, status)
                        .orderByAsc(GoalPlan::getSortOrder)
                        .orderByDesc(GoalPlan::getId));
        return goals.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public GoalResponse getById(Long id) {
        return toResponse(getAndValidate(id));
    }

    @Override
    @Transactional
    public GoalResponse update(Long id, GoalCreateRequest request) {
        GoalPlan goal = getAndValidate(id);
        BeanUtils.copyProperties(request, goal, "id", "userId");
        goalPlanMapper.updateById(goal);
        return toResponse(goal);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        goalPlanMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateProgress(Long id, Integer progress) {
        GoalPlan goal = getAndValidate(id);
        goal.setProgress(Math.max(0, Math.min(100, progress)));
        // 自动更新状态
        if (goal.getProgress() == 100) {
            goal.setStatus("COMPLETED");
        } else if (goal.getProgress() > 0 && "NOT_STARTED".equals(goal.getStatus())) {
            goal.setStatus("IN_PROGRESS");
        }
        goalPlanMapper.updateById(goal);
    }

    @Override
    public List<GoalResponse> getTree(String goalType) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<GoalPlan> wrapper = new LambdaQueryWrapper<GoalPlan>()
                .eq(GoalPlan::getUserId, userId)
                .eq(goalType != null, GoalPlan::getGoalType, goalType)
                .orderByAsc(GoalPlan::getSortOrder);

        List<GoalPlan> all = goalPlanMapper.selectList(wrapper);
        Map<Long, GoalResponse> responseMap = all.stream()
                .collect(Collectors.toMap(GoalPlan::getId, this::toResponse));

        List<GoalResponse> roots = new ArrayList<>();
        for (GoalPlan goal : all) {
            GoalResponse response = responseMap.get(goal.getId());
            if (goal.getParentId() == null) {
                roots.add(response);
            } else {
                GoalResponse parent = responseMap.get(goal.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(response);
                }
            }
        }
        return roots;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<GoalPlan> all = goalPlanMapper.selectList(
                new LambdaQueryWrapper<GoalPlan>().eq(GoalPlan::getUserId, userId));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());
        stats.put("notStarted", all.stream().filter(g -> "NOT_STARTED".equals(g.getStatus())).count());
        stats.put("inProgress", all.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count());
        stats.put("completed", all.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count());
        stats.put("abandoned", all.stream().filter(g -> "ABANDONED".equals(g.getStatus())).count());

        // 各类型数量
        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(GoalPlan::getGoalType, Collectors.counting()));
        stats.put("byType", byType);

        // 各分类数量
        Map<String, Long> byCategory = all.stream()
                .collect(Collectors.groupingBy(GoalPlan::getCategory, Collectors.counting()));
        stats.put("byCategory", byCategory);

        // 平均进度
        OptionalDouble avgProgress = all.stream().mapToInt(GoalPlan::getProgress).average();
        stats.put("avgProgress", avgProgress.isPresent() ? Math.round(avgProgress.getAsDouble()) : 0);
        return stats;
    }

    // =================== 私有方法 ===================

    private GoalPlan getAndValidate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        GoalPlan goal = goalPlanMapper.selectOne(
                new LambdaQueryWrapper<GoalPlan>()
                        .eq(GoalPlan::getId, id)
                        .eq(GoalPlan::getUserId, userId));
        if (goal == null) {
            throw new BusinessException(ResultCode.GOAL_NOT_FOUND);
        }
        return goal;
    }

    private GoalResponse toResponse(GoalPlan goal) {
        GoalResponse response = new GoalResponse();
        BeanUtils.copyProperties(goal, response);
        return response;
    }
}
