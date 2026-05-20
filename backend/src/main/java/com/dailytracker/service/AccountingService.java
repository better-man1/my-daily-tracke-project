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
 * 记账服务接口（Accounting Service Interface）
 *
 * 【职责说明】
 * 本接口负责个人记账/财务管理模块的全部业务逻辑，包括：
 * - 账目的增删改查（CRUD）操作
 * - 按日/月/年维度的收支统计
 * - 按分类维度的支出分析
 * - 预算设置与执行情况跟踪
 * - 记账分类树的管理（系统预设 + 用户自定义）
 *
 * 【设计模式】
 * - CRUD 模式：接口方法围绕"创建、查询、更新、删除"四个核心操作组织，
 *   这是业务服务层最常见的设计范式。
 *
 * - 分页查询模式：使用 MyBatis-Plus 的 IPage 接口封装分页信息，
 *   避免一次性加载大量数据导致的内存溢出和性能问题。
 *
 * - 统计聚合模式：提供多个维度的统计方法（日/月/年/分类），
 *   使用专门的 AccountingStatsResponse DTO 承载统计结果。
 *
 * 【SOLID 原则体现】
 * - 单一职责：只关注记账相关的业务逻辑
 * - 接口隔离：方法粒度适中，每个方法专注于一个特定的业务操作
 */
public interface AccountingService {

    /**
     * 创建账目记录
     *
     * 【业务流程】
     * 1. 获取当前登录用户ID
     * 2. 校验记账分类是否存在
     * 3. 将请求 DTO 转换为实体并填充用户ID
     * 4. 将图片列表序列化为 JSON 存储
     * 5. 插入数据库并返回响应 DTO
     *
     * @param request 创建请求 DTO，包含金额、类型（收入/支出）、分类ID、备注、图片等
     * @return AccountingResponse 创建后的账目响应 DTO
     * @throws com.dailytracker.common.exception.BusinessException 分类不存在时抛出异常
     */
    AccountingResponse create(AccountingCreateRequest request);

    /**
     * 分页查询账目列表
     *
     * 【设计说明】
     * 使用 MyBatis-Plus 的 IPage 进行分页，支持多条件动态筛选：
     * - 日期范围筛选（startDate ~ endDate）
     * - 类型筛选（INCOME/EXPENSE）
     * 结果按记账日期和ID倒序排列（最新的记录排在前面）。
     *
     * @param pageNum   当前页码（从 1 开始）
     * @param pageSize  每页条数
     * @param startDate 开始日期（可为 null，表示不限制起始日期）
     * @param endDate   结束日期（可为 null，表示不限制结束日期）
     * @param type      账目类型（INCOME/EXPENSE，可为 null 表示查全部）
     * @return IPage<AccountingResponse> 分页结果对象，包含记录列表和分页信息
     */
    IPage<AccountingResponse> page(int pageNum, int pageSize,
                                   LocalDate startDate, LocalDate endDate, String type);

    /**
     * 获取账目详情
     *
     * 【业务流程】
     * 1. 根据ID查询账目记录（并校验归属用户）
     * 2. 查询关联的分类信息
     * 3. 组装响应 DTO 返回
     *
     * @param id 账目ID
     * @return AccountingResponse 账目详情
     * @throws com.dailytracker.common.exception.BusinessException 账目不存在或不属于当前用户时抛出异常
     */
    AccountingResponse getById(Long id);

    /**
     * 更新账目
     *
     * 【业务流程】
     * 1. 查询并校验账目归属
     * 2. 使用 BeanUtils.copyProperties 将请求 DTO 属性复制到实体（排除 id、userId、images）
     * 3. 如果有图片更新，重新序列化为 JSON
     * 4. 更新数据库并返回最新数据
     *
     * @param id      账目ID
     * @param request 更新请求 DTO
     * @return AccountingResponse 更新后的账目响应 DTO
     */
    AccountingResponse update(Long id, AccountingCreateRequest request);

    /**
     * 删除账目
     *
     * @param id 账目ID
     * @throws com.dailytracker.common.exception.BusinessException 账目不存在或不属于当前用户时抛出异常
     */
    void delete(Long id);

    /**
     * 按日统计收支数据
     *
     * @param date 统计日期（为 null 时默认今天）
     * @return AccountingStatsResponse 统计结果，包含总收入、总支出、余额
     */
    AccountingStatsResponse dailyStats(LocalDate date);

    /**
     * 按月统计收支数据
     *
     * @param year  年份（如 2026）
     * @param month 月份（1-12）
     * @return AccountingStatsResponse 统计结果
     */
    AccountingStatsResponse monthlyStats(int year, int month);

    /**
     * 按年统计收支数据
     *
     * @param year 年份（如 2026）
     * @return AccountingStatsResponse 统计结果
     */
    AccountingStatsResponse yearlyStats(int year);

    /**
     * 按分类统计支出/收入分布
     *
     * 【设计说明】
     * 返回每个分类的金额和占比百分比，用于前端饼图/环形图渲染。
     * 结果按金额降序排列，方便用户快速定位主要支出项。
     *
     * @param startDate 开始日期（可为 null）
     * @param endDate   结束日期（可为 null）
     * @param type      账目类型（INCOME/EXPENSE，可为 null）
     * @return 分类统计列表，每个元素包含 categoryId、categoryName、amount、percentage
     */
    List<AccountingStatsResponse.CategoryStat> categoryStats(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 设置预算
     *
     * 【业务流程】
     * 1. 查询该月是否已有预算记录
     * 2. 如果已存在 -> 更新金额
     * 3. 如果不存在 -> 新建预算记录
     *
     * 支持设置"总预算"（categoryId 为 null）和"分类预算"（指定 categoryId）。
     *
     * @param request 预算创建请求 DTO，包含年份、月份、金额、分类ID
     */
    void setBudget(BudgetCreateRequest request);

    /**
     * 获取预算及执行情况
     *
     * 【返回数据包含】
     * - budgets: 该月所有预算记录列表
     * - totalExpense: 该月实际总支出
     * - totalIncome: 该月实际总收入
     * - balance: 该月收支差额
     * - totalBudget: 总预算金额
     * - budgetUsed: 已使用预算金额
     * - budgetRemaining: 剩余预算金额
     * - budgetRate: 预算使用率（百分比）
     *
     * @param year  年份
     * @param month 月份
     * @return 预算执行情况数据 Map
     */
    Map<String, Object> getBudget(int year, int month);

    /**
     * 获取记账分类树
     *
     * 【设计说明】
     * 返回树形结构的分类数据（一级分类 + 子分类列表）。
     * 目前只返回系统预设分类（userId 为 null），后续可扩展用户自定义分类。
     * 结果按 sortOrder 升序排列，保证前端展示的顺序一致性。
     *
     * @param type 分类类型（INCOME/EXPENSE，可为 null 表示查全部）
     * @return 分类树列表，每个元素包含 id、name、icon、type、children
     */
    List<Map<String, Object>> getCategories(String type);
}
