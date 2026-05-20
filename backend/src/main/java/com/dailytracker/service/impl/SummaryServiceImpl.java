package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.SummaryCreateRequest;
import com.dailytracker.dto.response.SummaryResponse;
import com.dailytracker.entity.DailySummary;
import com.dailytracker.mapper.DailySummaryMapper;
import com.dailytracker.service.SummaryService;
import com.dailytracker.util.DateUtils;
import com.dailytracker.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每日总结服务实现类（Summary Service Implementation）
 *
 * 【类设计说明】
 * 本类是 SummaryService 接口的具体实现，负责每日总结模块的全部业务逻辑。
 * 包括总结 CRUD、今日总结查询、连续打卡统计、情绪趋势分析等。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入依赖
 *
 * 【核心设计】
 * - 逻辑删除恢复机制：创建总结时，如果发现已被逻辑删除的同日期记录，
 *   会恢复并更新该记录（而非插入新记录），避免唯一索引冲突。
 * - 连续天数算法：通过遍历排序后的日期列表，计算当前连续天数和历史最长连续天数。
 * - JSON 字段存储：感恩事项（gratitude）和标签（tags）以 JSON 格式存储在数据库中。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    /** 每日总结数据访问层 */
    private final DailySummaryMapper summaryMapper;
    /** Jackson JSON 序列化工具 */
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SummaryResponse create(SummaryCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        DailySummary existing = summaryMapper.selectByDateIgnoreDeleted(userId, request.getSummaryDate());
        
        if (existing != null) {
            if (existing.getIsDeleted() == 0) {
                throw new BusinessException(ResultCode.SUMMARY_ALREADY_EXISTS);
            } else {
                // 如果存在已被逻辑删除的记录，则直接更新并恢复(is_deleted=0)
                BeanUtils.copyProperties(request, existing, "gratitude", "tags");
                existing.setGratitude(toJson(request.getGratitude()));
                existing.setTags(toJson(request.getTags()));
                
                summaryMapper.restoreAndUpdate(existing);
                log.info("恢复并更新被逻辑删除的总结: userId={}, date={}", userId, request.getSummaryDate());
                return toResponse(existing);
            }
        }

        DailySummary summary = new DailySummary();
        BeanUtils.copyProperties(request, summary, "gratitude", "tags");
        summary.setUserId(userId);
        summary.setGratitude(toJson(request.getGratitude()));
        summary.setTags(toJson(request.getTags()));

        summaryMapper.insert(summary);
        log.info("创建总结成功: userId={}, date={}", userId, request.getSummaryDate());
        return toResponse(summary);
    }

    @Override
    public List<SummaryResponse> list(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 简单分页实现（基于 offset）
        int offset = (pageNum - 1) * pageSize;
        List<DailySummary> list = summaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .ge(startDate != null, DailySummary::getSummaryDate, startDate)
                        .le(endDate != null, DailySummary::getSummaryDate, endDate)
                        .orderByDesc(DailySummary::getSummaryDate)
                        .last("LIMIT " + pageSize + " OFFSET " + offset)
        );
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SummaryResponse getById(Long id) {
        return toResponse(getAndValidate(id));
    }

    @Override
    @Transactional
    public SummaryResponse update(Long id, SummaryCreateRequest request) {
        DailySummary summary = getAndValidate(id);
        BeanUtils.copyProperties(request, summary, "id", "userId", "summaryDate", "gratitude", "tags");
        summary.setGratitude(toJson(request.getGratitude()));
        summary.setTags(toJson(request.getTags()));
        summaryMapper.updateById(summary);
        return toResponse(summary);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        summaryMapper.deleteById(id);
    }

    @Override
    public SummaryResponse getToday() {
        Long userId = SecurityUtils.getCurrentUserId();
        DailySummary summary = summaryMapper.selectOne(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .eq(DailySummary::getSummaryDate, DateUtils.today()));
        return summary != null ? toResponse(summary) : null;
    }

    @Override
    public Map<String, Object> getStreak() {
        Long userId = SecurityUtils.getCurrentUserId();
        // 获取所有总结日期（降序）
        List<DailySummary> all = summaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .orderByDesc(DailySummary::getSummaryDate));

        if (all.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("currentStreak", 0);
            result.put("longestStreak", 0);
            result.put("totalDays", 0);
            return result;
        }

        List<LocalDate> dates = all.stream()
                .map(DailySummary::getSummaryDate).collect(Collectors.toList());

        // 计算当前连续天数
        int currentStreak = 0;
        LocalDate yesterday = DateUtils.today();
        for (LocalDate date : dates) {
            if (date.equals(yesterday) || date.equals(yesterday.minusDays(1))) {
                currentStreak++;
                yesterday = date;
            } else {
                break;
            }
        }

        // 计算历史最长连续天数
        int longestStreak = 0, tempStreak = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).minusDays(1).equals(dates.get(i))) {
                tempStreak++;
                longestStreak = Math.max(longestStreak, tempStreak);
            } else {
                tempStreak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, currentStreak);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentStreak", currentStreak);
        result.put("longestStreak", longestStreak);
        result.put("totalDays", dates.size());
        result.put("lastSummaryDate", dates.get(0));
        return result;
    }

    @Override
    public List<Map<String, Object>> getMoodTrend(int days) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate startDate = DateUtils.today().minusDays(days - 1);

        List<DailySummary> list = summaryMapper.selectList(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .ge(DailySummary::getSummaryDate, startDate)
                        .orderByAsc(DailySummary::getSummaryDate));

        return list.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", s.getSummaryDate());
            map.put("mood", s.getMood());
            map.put("score", s.getScore());
            return map;
        }).collect(Collectors.toList());
    }

    // =================== 私有辅助方法 ===================

    /**
     * 查询并校验总结归属（私有方法）
     *
     * @param id 总结ID
     * @return DailySummary 总结实体
     * @throws BusinessException 总结不存在或不属于当前用户时抛出异常
     */
    private DailySummary getAndValidate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        DailySummary summary = summaryMapper.selectOne(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getId, id)
                        .eq(DailySummary::getUserId, userId));
        if (summary == null) {
            throw new BusinessException(ResultCode.SUMMARY_NOT_FOUND);
        }
        return summary;
    }

    /**
     * 实体转响应 DTO（私有方法）
     *
     * @param summary 总结实体
     * @return SummaryResponse 响应 DTO
     */
    private SummaryResponse toResponse(DailySummary summary) {
        SummaryResponse response = new SummaryResponse();
        BeanUtils.copyProperties(summary, response);
        return response;
    }

    /**
     * 对象转 JSON 字符串（私有工具方法）
     *
     * 增加 null 检查，避免对 null 对象进行序列化。
     *
     * @param obj 待序列化的对象
     * @return JSON 字符串，obj 为 null 时返回 null
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }
}
