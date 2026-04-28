package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.PlanCreateRequest;
import com.dailytracker.dto.response.PlanResponse;
import com.dailytracker.entity.DailyPlan;
import com.dailytracker.mapper.DailyPlanMapper;
import com.dailytracker.service.DailyPlanService;
import com.dailytracker.util.DateUtils;
import com.dailytracker.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日计划服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPlanServiceImpl implements DailyPlanService {

    private final DailyPlanMapper dailyPlanMapper;

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

    // =================== 私有方法 ===================

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

    private PlanResponse toResponse(DailyPlan plan) {
        PlanResponse response = new PlanResponse();
        BeanUtils.copyProperties(plan, response);
        return response;
    }
}
