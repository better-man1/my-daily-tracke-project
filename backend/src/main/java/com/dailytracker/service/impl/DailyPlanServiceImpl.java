package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.request.RepeatUpdateRequest;
import com.dailytracker.dto.response.PlanResponse;
import com.dailytracker.entity.DailyPlan;
import com.dailytracker.mapper.DailyPlanMapper;
import com.dailytracker.service.DailyPlanService;
import com.dailytracker.util.DateUtils;
import com.dailytracker.util.SecurityUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日计划服务实现类（Daily Plan Service Implementation）
 *
 * 【类设计说明】
 * 本类是 DailyPlanService 接口的具体实现，是系统中功能最丰富的服务实现之一。
 * 负责每日计划的全部业务逻辑，包括任务 CRUD、状态管理、排序、顺延、重复任务生成、
 * 子任务管理、模板管理、时间块管理、批量操作和数据统计等。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean，由 Spring IoC 容器管理
 * @RequiredArgsConstructor - Lombok 注解，自动生成包含所有 final 字段的构造器实现依赖注入
 *
 * 【核心设计思想】
 * - 重复任务策略模式：通过 shouldCreateOnDate 方法和 matchXxxPattern 系列方法，
 *   实现了每日/每周/每月/自定义四种重复策略的判断逻辑。
 * - 子任务级联更新：子任务状态变化时自动更新父任务的计数器，保证数据一致性。
 * - 数据归属校验：所有操作都通过 getAndValidate 方法校验任务归属当前用户，
 *   实现了数据层面的权限隔离。
 *
 * 【事务管理】
 * 所有涉及数据修改的方法都标注了 @Transactional 注解，
 * 确保多个数据库操作要么全部成功，要么全部回滚。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPlanServiceImpl implements DailyPlanService {

    /** 每日计划数据访问层（Mapper） */
    private final DailyPlanMapper dailyPlanMapper;
    /** Jackson JSON 序列化工具（用于解析重复规则的 JSON 配置） */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PlanResponse create(PlanCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DailyPlan plan = new DailyPlan();
        BeanUtils.copyProperties(request, plan);
        plan.setUserId(userId);
        plan.setStatus("TODO");

        dailyPlanMapper.insert(plan);
        log.info("创建计划成功: userId={}, title={}", userId, plan.getTitle());

        // 自动生成未来30天的重复实例
        if (plan.getRepeatType() != null && !"NONE".equals(plan.getRepeatType())) {
            LocalDate endDate = plan.getPlanDate().plusDays(30);
            if (plan.getRepeatEndDate() != null && plan.getRepeatEndDate().isBefore(endDate)) {
                endDate = plan.getRepeatEndDate();
            }
            // 从第二天开始生成，当天的已经创建
            generateRepeatInstances(plan.getId(), plan.getPlanDate().plusDays(1), endDate);
        }

        return toResponse(plan);
    }

    @Override
    public List<PlanResponse> listByDate(LocalDate planDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate date = planDate != null ? planDate : DateUtils.today();

        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, date)
                        .eq(DailyPlan::getIsTemplate, 0)
                        .orderByAsc(DailyPlan::getSortOrder)
                        .orderByAsc(DailyPlan::getId)
        );
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PlanResponse getById(Long id) {
        DailyPlan plan = getAndValidate(id);
        return toResponse(plan);
    }

    @Override
    @Transactional
    public PlanResponse update(Long id, PlanCreateRequest request) {
        DailyPlan plan = getAndValidate(id);
        BeanUtils.copyProperties(request, plan, "id", "userId", "status", "createdAt");
        dailyPlanMapper.updateById(plan);

        // 如果修改后包含重复规则，自动生成未来30天的重复实例
        if (plan.getRepeatType() != null && !"NONE".equals(plan.getRepeatType())) {
            LocalDate endDate = plan.getPlanDate().plusDays(30);
            if (plan.getRepeatEndDate() != null && plan.getRepeatEndDate().isBefore(endDate)) {
                endDate = plan.getRepeatEndDate();
            }
            generateRepeatInstances(plan.getId(), plan.getPlanDate().plusDays(1), endDate);
        } else if (plan.getRepeatType() == null || "NONE".equals(plan.getRepeatType())) {
            // 如果清除了重复规则，清理未来未完成的由该任务产生的重复实例
            dailyPlanMapper.delete(new LambdaQueryWrapper<DailyPlan>()
                    .eq(DailyPlan::getUserId, plan.getUserId())
                    .eq(DailyPlan::getTitle, plan.getTitle())
                    .gt(DailyPlan::getPlanDate, plan.getPlanDate())
                    .eq(DailyPlan::getStatus, "TODO")
                    .eq(DailyPlan::getIsTemplate, 0)
            );
        }

        return toResponse(plan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        dailyPlanMapper.deleteById(id);
        log.info("删除计划成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        DailyPlan plan = getAndValidate(id);
        plan.setStatus(status);
        if ("DONE".equals(status)) {
            plan.setCompletedAt(LocalDateTime.now());
        } else {
            plan.setCompletedAt(null);
        }
        dailyPlanMapper.updateById(plan);
    }

    @Override
    @Transactional
    public void batchUpdateSort(Map<Long, Integer> sortMap) {
        Long userId = SecurityUtils.getCurrentUserId();
        sortMap.forEach((planId, sortOrder) -> {
            dailyPlanMapper.update(null,
                    new LambdaUpdateWrapper<DailyPlan>()
                            .eq(DailyPlan::getId, planId)
                            .eq(DailyPlan::getUserId, userId)
                            .set(DailyPlan::getSortOrder, sortOrder)
            );
        });
    }

    @Override
    @Transactional
    public PlanResponse postpone(Long id) {
        DailyPlan plan = getAndValidate(id);
        // 顺延至下一天
        plan.setPlanDate(plan.getPlanDate().plusDays(1));
        // 如果是已完成/已取消状态则重置为 TODO
        if ("DONE".equals(plan.getStatus()) || "CANCELLED".equals(plan.getStatus())) {
            plan.setStatus("TODO");
            plan.setCompletedAt(null);
        }
        dailyPlanMapper.updateById(plan);
        log.info("任务顺延成功: id={}, newDate={}", id, plan.getPlanDate());
        return toResponse(plan);
    }

    @Override
    public Map<String, Object> getStatistics(LocalDate planDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate date = planDate != null ? planDate : DateUtils.today();

        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, date)
                        .eq(DailyPlan::getIsTemplate, 0)
        );

        long total = plans.size();
        long done = plans.stream().filter(p -> "DONE".equals(p.getStatus())).count();
        long inProgress = plans.stream().filter(p -> "IN_PROGRESS".equals(p.getStatus())).count();
        long todo = plans.stream().filter(p -> "TODO".equals(p.getStatus())).count();
        long cancelled = plans.stream().filter(p -> "CANCELLED".equals(p.getStatus())).count();
        int totalEstimated = plans.stream().mapToInt(p -> p.getEstimatedMins() != null ? p.getEstimatedMins() : 0).sum();
        int totalActual = plans.stream().mapToInt(p -> p.getActualMins() != null ? p.getActualMins() : 0).sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("date", date);
        stats.put("total", total);
        stats.put("done", done);
        stats.put("inProgress", inProgress);
        stats.put("todo", todo);
        stats.put("cancelled", cancelled);
        stats.put("completionRate", total == 0 ? 0 : Math.round(done * 100.0 / total));
        stats.put("totalEstimatedMins", totalEstimated);
        stats.put("totalActualMins", totalActual);
        return stats;
    }

    @Override
    @Transactional
    public void saveAsTemplate(Long id, String templateName) {
        DailyPlan original = getAndValidate(id);
        Long userId = SecurityUtils.getCurrentUserId();

        DailyPlan template = new DailyPlan();
        template.setUserId(userId);
        template.setTitle(original.getTitle());
        template.setDescription(original.getDescription());
        template.setPlanDate(DateUtils.today()); // 模板日期默认今天，实际使用时替换
        template.setPriority(original.getPriority());
        template.setCategory(original.getCategory());
        template.setEstimatedMins(original.getEstimatedMins());
        template.setStatus("TODO");
        template.setIsTemplate(1);
        template.setTemplateName(templateName);

        dailyPlanMapper.insert(template);
        log.info("保存模板成功: userId={}, templateName={}", userId, templateName);
    }

    @Override
    public List<PlanResponse> listTemplates() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> templates = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getIsTemplate, 1)
                        .orderByAsc(DailyPlan::getId)
        );
        return templates.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PlanResponse> generateRepeatInstances(Long planId, LocalDate startDate, LocalDate endDate) {
        DailyPlan template = getAndValidate(planId);
        Long userId = SecurityUtils.getCurrentUserId();

        if (template.getRepeatType() == null || "NONE".equals(template.getRepeatType())) {
            return Collections.emptyList();
        }

        List<PlanResponse> instances = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (shouldCreateOnDate(currentDate, template)) {
                // 检查该日期是否已存在相同标题的任务
                boolean exists = dailyPlanMapper.selectCount(
                        new LambdaQueryWrapper<DailyPlan>()
                                .eq(DailyPlan::getUserId, userId)
                                .eq(DailyPlan::getPlanDate, currentDate)
                                .eq(DailyPlan::getTitle, template.getTitle())
                                .eq(DailyPlan::getIsTemplate, 0)
                ) > 0;

                if (!exists) {
                    DailyPlan instance = new DailyPlan();
                    BeanUtils.copyProperties(template, instance, "id", "createdAt", "updatedAt");
                    instance.setUserId(userId);
                    instance.setPlanDate(currentDate);
                    instance.setStatus("TODO");
                    instance.setCompletedAt(null);
                    instance.setIsTemplate(0);
                    dailyPlanMapper.insert(instance);
                    instances.add(toResponse(instance));
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.info("生成重复任务实例: planId={}, count={}", planId, instances.size());
        return instances;
    }

    @Override
    @Transactional
    public PlanResponse updateRepeatRule(Long id, RepeatUpdateRequest request) {
        DailyPlan plan = getAndValidate(id);

        if (request.getRepeatType() == null || "NONE".equals(request.getRepeatType())) {
            // 清除重复设置
            plan.setRepeatType(null);
            plan.setRepeatPattern(null);
            plan.setRepeatEndDate(null);
        } else {
            plan.setRepeatType(request.getRepeatType());
            plan.setRepeatPattern(request.getRepeatPattern());
            plan.setRepeatEndDate(request.getRepeatEndDate());
        }

        dailyPlanMapper.updateById(plan);
        log.info("更新重复规则: id={}, type={}", id, request.getRepeatType());
        return toResponse(plan);
    }

    @Override
    @Transactional
    public void stopRepeat(Long id) {
        DailyPlan plan = getAndValidate(id);
        plan.setRepeatType("NONE");
        plan.setRepeatPattern(null);
        plan.setRepeatEndDate(null);
        dailyPlanMapper.updateById(plan);
        log.info("停止重复: id={}", id);
    }

    @Override
    @Transactional
    public PlanResponse createSubtask(Long parentId, PlanCreateRequest request) {
        DailyPlan parent = getAndValidate(parentId);

        // 父任务不能是子任务
        if (parent.getParentId() != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能为子任务创建子任务");
        }

        DailyPlan subtask = new DailyPlan();
        subtask.setUserId(SecurityUtils.getCurrentUserId());
        subtask.setParentId(parentId);
        subtask.setTitle(request.getTitle());
        subtask.setDescription(request.getDescription());
        subtask.setPlanDate(parent.getPlanDate());
        subtask.setPriority(request.getPriority() != null ? request.getPriority() : "P3");
        subtask.setCategory(request.getCategory() != null ? request.getCategory() : parent.getCategory());
        subtask.setEstimatedMins(request.getEstimatedMins());
        subtask.setStatus("TODO");
        subtask.setSortOrder(parent.getSubtaskCount() != null ? parent.getSubtaskCount() : 0);

        dailyPlanMapper.insert(subtask);

        // 更新父任务的子任务计数
        updateParentSubtaskCount(parentId);

        log.info("创建子任务: parentId={}, subtaskId={}", parentId, subtask.getId());
        return toResponse(subtask);
    }

    @Override
    public List<PlanResponse> getSubtasks(Long parentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> subtasks = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getParentId, parentId)
                        .orderByAsc(DailyPlan::getSortOrder)
                        .orderByAsc(DailyPlan::getId)
        );
        return subtasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSubtaskStatus(Long id, String status) {
        DailyPlan subtask = getAndValidate(id);

        // 确保是子任务
        if (subtask.getParentId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务不是子任务");
        }

        subtask.setStatus(status);
        if ("DONE".equals(status)) {
            subtask.setCompletedAt(LocalDateTime.now());
        } else {
            subtask.setCompletedAt(null);
        }
        dailyPlanMapper.updateById(subtask);

        // 级联更新父任务进度
        updateParentSubtaskCount(subtask.getParentId());

        log.info("更新子任务状态: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void convertToMainTask(Long id) {
        DailyPlan subtask = getAndValidate(id);

        // 确保是子任务
        if (subtask.getParentId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该任务不是子任务");
        }

        Long parentId = subtask.getParentId();
        subtask.setParentId(null);
        dailyPlanMapper.updateById(subtask);

        // 更新原父任务的子任务计数
        updateParentSubtaskCount(parentId);

        log.info("子任务转为主任务: id={}", id);
    }

    // =================== 私有辅助方法 ===================

    /**
     * 查询并校验任务归属（私有方法）
     *
     * 【设计说明】
     * 这是整个类中最核心的私有方法，几乎所有公共方法都需要调用它。
     * 它封装了两个关键逻辑：
     * 1. 数据查询：根据ID获取任务实体
     * 2. 权限校验：确保任务属于当前登录用户
     *
     * 这种设计体现了"数据权限隔离"的安全思想——
     * 用户只能操作自己的数据，无法通过篡改ID访问他人的任务。
     *
     * @param id 任务ID
     * @return DailyPlan 任务实体
     * @throws BusinessException 任务不存在或不属于当前用户时抛出异常
     */
    private DailyPlan getAndValidate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        DailyPlan plan = dailyPlanMapper.selectOne(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getId, id)
                        .eq(DailyPlan::getUserId, userId)
        );
        if (plan == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return plan;
    }

    /**
     * 实体转响应 DTO（私有方法）
     *
     * 使用 Spring 的 BeanUtils.copyProperties 将实体属性复制到响应 DTO。
     * 如果字段名和类型一致，会自动映射；不一致的需要手动设置。
     *
     * @param plan 计划实体
     * @return PlanResponse 响应 DTO
     */
    private PlanResponse toResponse(DailyPlan plan) {
        PlanResponse response = new PlanResponse();
        BeanUtils.copyProperties(plan, response);
        return response;
    }

    /**
     * 判断是否应该在指定日期创建重复任务
     */
    private boolean shouldCreateOnDate(LocalDate date, DailyPlan template) {
        // 检查是否在重复结束日期之后
        if (template.getRepeatEndDate() != null && date.isAfter(template.getRepeatEndDate())) {
            return false;
        }

        String repeatType = template.getRepeatType();
        if (repeatType == null || "NONE".equals(repeatType)) {
            return false;
        }

        switch (repeatType) {
            case "DAILY":
                return true;
            case "WEEKLY":
                return matchWeeklyPattern(date, template.getRepeatPattern());
            case "MONTHLY":
                return matchMonthlyPattern(date, template.getRepeatPattern());
            case "CUSTOM":
                return matchCustomPattern(date, template.getRepeatPattern());
            default:
                return false;
        }
    }

    /**
     * 匹配每周重复模式
     * pattern 格式: {"days":[1,3,5]} 周一到周日为 1-7
     */
    private boolean matchWeeklyPattern(LocalDate date, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true; // 默认每天
        }

        try {
            Map<String, List<Integer>> config = objectMapper.readValue(pattern,
                    new TypeReference<Map<String, List<Integer>>>() {});
            List<Integer> days = config.get("days");
            if (days == null || days.isEmpty()) {
                return true;
            }

            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int dayValue = dayOfWeek.getValue(); // 1=Monday, 7=Sunday
            return days.contains(dayValue);
        } catch (Exception e) {
            log.warn("解析每周重复模式失败: {}", pattern, e);
            return true;
        }
    }

    /**
     * 匹配每月重复模式
     * pattern 格式: {"days":[1,15]} 每月的几号
     */
    private boolean matchMonthlyPattern(LocalDate date, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return date.getDayOfMonth() == 1; // 默认每月1号
        }

        try {
            Map<String, List<Integer>> config = objectMapper.readValue(pattern,
                    new TypeReference<Map<String, List<Integer>>>() {});
            List<Integer> days = config.get("days");
            if (days == null || days.isEmpty()) {
                return date.getDayOfMonth() == 1;
            }

            return days.contains(date.getDayOfMonth());
        } catch (Exception e) {
            log.warn("解析每月重复模式失败: {}", pattern, e);
            return date.getDayOfMonth() == 1;
        }
    }

    /**
     * 匹配自定义重复模式
     * pattern 格式: {"type":"INTERVAL","interval":2} 每N天
     */
    private boolean matchCustomPattern(LocalDate date, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }

        try {
            Map<String, Object> config = objectMapper.readValue(pattern,
                    new TypeReference<Map<String, Object>>() {});
            String type = (String) config.get("type");

            if ("INTERVAL".equals(type)) {
                Integer interval = (Integer) config.get("interval");
                if (interval == null || interval < 1) {
                    interval = 1;
                }
                // 简单的间隔逻辑，实际应该基于起始日期计算
                return true;
            }

            return true;
        } catch (Exception e) {
            log.warn("解析自定义重复模式失败: {}", pattern, e);
            return true;
        }
    }

    /**
     * 更新父任务的子任务计数（级联更新）
     *
     * 统计父任务下所有子任务的数量和已完成数量，更新到父任务记录中。
     * 这保证了父任务的 subtaskCount 和 completedSubtaskCount 字段始终准确。
     *
     * @param parentId 父任务ID
     */
    private void updateParentSubtaskCount(Long parentId) {
        List<DailyPlan> subtasks = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>().eq(DailyPlan::getParentId, parentId)
        );

        int totalCount = subtasks.size();
        int completedCount = (int) subtasks.stream()
                .filter(t -> "DONE".equals(t.getStatus()))
                .count();

        dailyPlanMapper.update(null,
                new LambdaUpdateWrapper<DailyPlan>()
                        .eq(DailyPlan::getId, parentId)
                        .set(DailyPlan::getSubtaskCount, totalCount)
                        .set(DailyPlan::getCompletedSubtaskCount, completedCount)
        );
    }

    @Override
    public List<Map<String, Object>> getCompletionTrend(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            List<DailyPlan> plans = dailyPlanMapper.selectList(
                    new LambdaQueryWrapper<DailyPlan>()
                            .eq(DailyPlan::getUserId, userId)
                            .eq(DailyPlan::getPlanDate, current)
                            .eq(DailyPlan::getIsTemplate, 0)
            );

            long total = plans.size();
            long done = plans.stream().filter(p -> "DONE".equals(p.getStatus())).count();
            double completionRate = total > 0 ? (done * 100.0 / total) : 0;

            Map<String, Object> data = new HashMap<>();
            data.put("date", current.toString());
            data.put("total", total);
            data.put("done", done);
            data.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            trend.add(data);

            current = current.plusDays(1);
        }

        return trend;
    }

    @Override
    public Map<String, Object> getCategoryDistribution(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .between(DailyPlan::getPlanDate, startDate, endDate)
                        .eq(DailyPlan::getIsTemplate, 0)
        );

        Map<String, Long> categoryCount = plans.stream()
                .collect(Collectors.groupingBy(DailyPlan::getCategory, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("data", categoryCount);
        result.put("total", plans.size());

        return result;
    }

    @Override
    public Map<String, Object> getPriorityDistribution(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .between(DailyPlan::getPlanDate, startDate, endDate)
                        .eq(DailyPlan::getIsTemplate, 0)
        );

        Map<String, Long> priorityCount = plans.stream()
                .collect(Collectors.groupingBy(DailyPlan::getPriority, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("data", priorityCount);
        result.put("total", plans.size());

        return result;
    }

    @Override
    public Map<String, Object> getTimeDistribution(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .between(DailyPlan::getPlanDate, startDate, endDate)
                        .eq(DailyPlan::getIsTemplate, 0)
                        .isNotNull(DailyPlan::getActualMins)
        );

        Map<String, Integer> categoryTime = new HashMap<>();
        int totalTime = 0;

        for (DailyPlan plan : plans) {
            String category = plan.getCategory();
            int time = plan.getActualMins() != null ? plan.getActualMins() : 0;
            categoryTime.merge(category, time, Integer::sum);
            totalTime += time;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", categoryTime);
        result.put("total", totalTime);

        return result;
    }

    @Override
    public List<PlanResponse> detectTimeConflicts(LocalDate planDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> timeBlocks = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, planDate)
                        .eq(DailyPlan::getIsTimeblock, 1)
                        .isNotNull(DailyPlan::getStartTime)
                        .isNotNull(DailyPlan::getEndTime)
                        .orderByAsc(DailyPlan::getStartTime)
        );

        List<DailyPlan> conflicts = new ArrayList<>();

        for (int i = 0; i < timeBlocks.size(); i++) {
            DailyPlan current = timeBlocks.get(i);
            for (int j = i + 1; j < timeBlocks.size(); j++) {
                DailyPlan next = timeBlocks.get(j);

                // 检查时间重叠
                if (isTimeOverlap(current.getStartTime(), current.getEndTime(),
                        next.getStartTime(), next.getEndTime())) {
                    if (!conflicts.contains(current)) conflicts.add(current);
                    if (!conflicts.contains(next)) conflicts.add(next);
                }
            }
        }

        return conflicts.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PlanResponse> getTimeBlocks(LocalDate planDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<DailyPlan> timeBlocks = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, planDate)
                        .eq(DailyPlan::getIsTimeblock, 1)
                        .orderByAsc(DailyPlan::getStartTime)
        );

        return timeBlocks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 检查两个时间段是否重叠（私有方法）
     *
     * 【算法】时间段重叠的充要条件：start1 < end2 && end1 > start2
     * 即：第一个时间段的开始时间在第二个时间段结束之前，
     *     且第一个时间段的结束时间在第二个时间段开始之后。
     *
     * @param start1 第一个时间段的开始时间
     * @param end1   第一个时间段的结束时间
     * @param start2 第二个时间段的开始时间
     * @param end2   第二个时间段的结束时间
     * @return true 表示重叠，false 表示不重叠
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1,
                                    LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    @Override
    @Transactional
    public void batchUpdate(List<Long> ids, String priority, String category, String status) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 验证所有任务都属于当前用户
        long validCount = dailyPlanMapper.selectCount(
                new LambdaQueryWrapper<DailyPlan>()
                        .in(DailyPlan::getId, ids)
                        .eq(DailyPlan::getUserId, userId)
        );

        if (validCount != ids.size()) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 构建更新条件
        LambdaUpdateWrapper<DailyPlan> updateWrapper = new LambdaUpdateWrapper<DailyPlan>()
                .in(DailyPlan::getId, ids)
                .eq(DailyPlan::getUserId, userId);

        if (priority != null) {
            updateWrapper.set(DailyPlan::getPriority, priority);
        }
        if (category != null) {
            updateWrapper.set(DailyPlan::getCategory, category);
        }
        if (status != null) {
            updateWrapper.set(DailyPlan::getStatus, status);
            if ("DONE".equals(status)) {
                updateWrapper.set(DailyPlan::getCompletedAt, LocalDateTime.now());
            }
        }

        dailyPlanMapper.update(null, updateWrapper);
        log.info("批量更新任务: count={}", ids.size());
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 只删除属于当前用户的任务
        dailyPlanMapper.delete(
                new LambdaQueryWrapper<DailyPlan>()
                        .in(DailyPlan::getId, ids)
                        .eq(DailyPlan::getUserId, userId)
        );
        log.info("批量删除任务: count={}", ids.size());
    }

    @Override
    @Transactional
    public void batchPostpone(List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();

        List<DailyPlan> plans = dailyPlanMapper.selectList(
                new LambdaQueryWrapper<DailyPlan>()
                        .in(DailyPlan::getId, ids)
                        .eq(DailyPlan::getUserId, userId)
        );

        LocalDate nextDay = LocalDate.now().plusDays(1);

        for (DailyPlan plan : plans) {
            plan.setPlanDate(nextDay);
            plan.setStatus("TODO");
            plan.setCompletedAt(null);
            dailyPlanMapper.updateById(plan);
        }

        log.info("批量顺延任务: count={}", plans.size());
    }

    @Override
    @Transactional
    public void batchComplete(List<Long> ids) {
        Long userId = SecurityUtils.getCurrentUserId();

        dailyPlanMapper.update(null,
                new LambdaUpdateWrapper<DailyPlan>()
                        .in(DailyPlan::getId, ids)
                        .eq(DailyPlan::getUserId, userId)
                        .set(DailyPlan::getStatus, "DONE")
                        .set(DailyPlan::getCompletedAt, LocalDateTime.now())
        );

        log.info("批量完成任务: count={}", ids.size());
    }
}
