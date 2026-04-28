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
 * 每日总结服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final DailySummaryMapper summaryMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SummaryResponse create(SummaryCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查该日期是否已有总结
        DailySummary existing = summaryMapper.selectOne(
                new LambdaQueryWrapper<DailySummary>()
                        .eq(DailySummary::getUserId, userId)
                        .eq(DailySummary::getSummaryDate, request.getSummaryDate()));
        if (existing != null) {
            throw new BusinessException(ResultCode.SUMMARY_ALREADY_EXISTS);
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

    // =================== 私有方法 ===================

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

    private SummaryResponse toResponse(DailySummary summary) {
        SummaryResponse response = new SummaryResponse();
        BeanUtils.copyProperties(summary, response);
        return response;
    }

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
