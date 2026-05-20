package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.GoalCreateRequest;
import com.dailytracker.dto.request.GoalKrRequest;
import com.dailytracker.dto.response.GoalKrResponse;
import com.dailytracker.dto.response.GoalResponse;
import com.dailytracker.entity.GoalKr;
import com.dailytracker.entity.GoalPlan;
import com.dailytracker.mapper.GoalKrMapper;
import com.dailytracker.mapper.GoalPlanMapper;
import com.dailytracker.service.GoalService;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 目标管理服务实现类（Goal Service Implementation）
 *
 * 【类设计说明】
 * 本类是 GoalService 接口的具体实现，负责目标管理模块的业务逻辑。
 * 支持多级目标（父子关系）、OKR 模式（目标+关键结果）、进度追踪和自动状态更新。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入依赖
 *
 * 【核心设计】
 * - 树形结构构建（getTree 方法）：遍历所有目标，按 parentId 构建父子层级关系
 * - OKR 进度计算（saveKeyResults 方法）：根据关键结果的当前值/目标值自动计算进度百分比
 * - 自动状态更新（updateProgress 方法）：进度达 100% 时自动标记为 COMPLETED
 * - 先删后增策略（update 方法）：更新关键结果时，先删除旧的再插入新的，实现简单
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    /** 目标计划数据访问层 */
    private final GoalPlanMapper goalPlanMapper;
    /** 目标关键结果数据访问层 */
    private final GoalKrMapper goalKrMapper;

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
        
        // 保存关键结果
        saveKeyResults(goal.getId(), userId, request.getKeyResults());
        
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
                        .orderByAsc(GoalPlan::getStartDate)
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
        
        // 更新关键结果：简单处理，先删后增
        goalKrMapper.delete(new LambdaQueryWrapper<GoalKr>().eq(GoalKr::getGoalId, id));
        saveKeyResults(id, goal.getUserId(), request.getKeyResults());
        
        return toResponse(goal);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        goalPlanMapper.deleteById(id);
        // 删除关联的关键结果
        goalKrMapper.delete(new LambdaQueryWrapper<GoalKr>().eq(GoalKr::getGoalId, id));
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
                .orderByAsc(GoalPlan::getStartDate);

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
                } else {
                    // 如果父目标因为类型筛选（例如：只查月度，而父级是年度）未被查询出来，
                    // 则将当前目标作为当前视图的根节点展示
                    roots.add(response);
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

    // =================== 私有辅助方法 ===================

    /**
     * 查询并校验目标归属（私有方法）
     *
     * @param id 目标ID
     * @return GoalPlan 目标实体
     * @throws BusinessException 目标不存在或不属于当前用户时抛出异常
     */
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

    /**
     * 实体转响应 DTO（私有方法）
     *
     * 将目标实体转换为响应 DTO，同时查询关联的关键结果列表。
     *
     * @param goal 目标实体
     * @return GoalResponse 响应 DTO（含关键结果列表）
     */
    private GoalResponse toResponse(GoalPlan goal) {
        GoalResponse response = new GoalResponse();
        BeanUtils.copyProperties(goal, response);
        
        // 查询关联的关键结果
        List<GoalKr> krs = goalKrMapper.selectList(new LambdaQueryWrapper<GoalKr>()
                .eq(GoalKr::getGoalId, goal.getId())
                .orderByAsc(GoalKr::getSortOrder));
        if (!krs.isEmpty()) {
            response.setKeyResults(krs.stream().map(kr -> {
                GoalKrResponse krResp = new GoalKrResponse();
                BeanUtils.copyProperties(kr, krResp);
                return krResp;
            }).collect(Collectors.toList()));
        }
        
        return response;
    }

    /**
     * 保存关键结果列表（私有方法）
     *
     * 遍历关键结果请求列表，为每个关键结果计算进度百分比：
     * progress = (currentValue / targetValue) * 100，上限为 100。
     *
     * @param goalId     目标ID
     * @param userId     用户ID
     * @param keyResults 关键结果请求列表
     */
    private void saveKeyResults(Long goalId, Long userId, List<GoalKrRequest> keyResults) {
        if (keyResults == null || keyResults.isEmpty()) {
            return;
        }
        for (GoalKrRequest krRequest : keyResults) {
            GoalKr kr = new GoalKr();
            BeanUtils.copyProperties(krRequest, kr);
            kr.setGoalId(goalId);
            kr.setUserId(userId);
            // 计算进度
            if (kr.getTargetValue() != null && kr.getTargetValue().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal current = kr.getCurrentValue() != null ? kr.getCurrentValue() : BigDecimal.ZERO;
                int progress = current.divide(kr.getTargetValue(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).intValue();
                kr.setProgress(Math.min(100, progress));
            } else {
                kr.setProgress(0);
            }
            goalKrMapper.insert(kr);
        }
    }
}
