package com.dailytracker.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dailytracker.dto.request.AccountingCreateRequest;
import com.dailytracker.dto.request.BudgetCreateRequest;
import com.dailytracker.dto.response.AccountingResponse;
import com.dailytracker.dto.response.AccountingStatsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 记账服务接口
 */
public interface AccountingService {

    /** 创建账目 */
    AccountingResponse create(AccountingCreateRequest request);

    /** 分页查询账目列表 */
    IPage<AccountingResponse> page(int pageNum, int pageSize,
                                   LocalDate startDate, LocalDate endDate, String type);

    /** 账目详情 */
    AccountingResponse getById(Long id);

    /** 更新账目 */
    AccountingResponse update(Long id, AccountingCreateRequest request);

    /** 删除账目 */
    void delete(Long id);

    /** 日统计 */
    AccountingStatsResponse dailyStats(LocalDate date);

    /** 月统计 */
    AccountingStatsResponse monthlyStats(int year, int month);

    /** 年统计 */
    AccountingStatsResponse yearlyStats(int year);

    /** 分类统计 */
    List<AccountingStatsResponse.CategoryStat> categoryStats(LocalDate startDate, LocalDate endDate, String type);

    /** 设置预算 */
    void setBudget(BudgetCreateRequest request);

    /** 获取预算及执行情况 */
    Map<String, Object> getBudget(int year, int month);

    /** 获取记账分类树 */
    List<Map<String, Object>> getCategories(String type);
}
