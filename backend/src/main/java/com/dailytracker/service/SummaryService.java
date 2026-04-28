package com.dailytracker.service;

import com.dailytracker.dto.request.SummaryCreateRequest;
import com.dailytracker.dto.response.SummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日总结服务接口
 */
public interface SummaryService {

    /** 创建总结 */
    SummaryResponse create(SummaryCreateRequest request);

    /** 总结列表（分页） */
    List<SummaryResponse> list(int pageNum, int pageSize, LocalDate startDate, LocalDate endDate);

    /** 总结详情 */
    SummaryResponse getById(Long id);

    /** 更新总结 */
    SummaryResponse update(Long id, SummaryCreateRequest request);

    /** 删除总结 */
    void delete(Long id);

    /** 获取今日总结 */
    SummaryResponse getToday();

    /** 获取连续记录天数 */
    Map<String, Object> getStreak();

    /** 情绪趋势（近N天） */
    List<Map<String, Object>> getMoodTrend(int days);
}
