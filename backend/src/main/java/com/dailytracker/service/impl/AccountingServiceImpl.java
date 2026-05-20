package com.dailytracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailytracker.common.exception.BusinessException;
import com.dailytracker.common.result.ResultCode;
import com.dailytracker.dto.request.AccountingCreateRequest;
import com.dailytracker.dto.request.BudgetCreateRequest;
import com.dailytracker.dto.response.AccountingResponse;
import com.dailytracker.dto.response.AccountingStatsResponse;
import com.dailytracker.entity.Accounting;
import com.dailytracker.entity.AccountingCategory;
import com.dailytracker.entity.Budget;
import com.dailytracker.mapper.AccountingCategoryMapper;
import com.dailytracker.mapper.AccountingMapper;
import com.dailytracker.mapper.BudgetMapper;
import com.dailytracker.service.AccountingService;
import com.dailytracker.util.DateUtils;
import com.dailytracker.util.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 记账服务实现类（Accounting Service Implementation）
 *
 * 【类设计说明】
 * 本类是 AccountingService 接口的具体实现，负责记账模块的全部业务逻辑。
 * 包括账目 CRUD、多维度统计（日/月/年/分类）、预算管理、分类树查询等。
 *
 * 【注解解释】
 * @Slf4j      - Lombok 注解，自动生成 SLF4J 日志记录器
 * @Service    - Spring 注解，标记为业务层 Bean
 * @RequiredArgsConstructor - Lombok 注解，通过构造器注入所有 final 依赖
 *
 * 【核心技术点】
 * - MyBatis-Plus 的 LambdaQueryWrapper：类型安全的条件构造器
 * - BeanUtils.copyProperties：Spring 提供的对象属性拷贝工具
 * - BigDecimal 精确计算：金融场景必须使用 BigDecimal 而非 double/float
 * - Jackson ObjectMapper：JSON 序列化/反序列化（处理图片列表等字段）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

    /** 记账数据访问层 */
    private final AccountingMapper accountingMapper;
    /** 记账分类数据访问层 */
    private final AccountingCategoryMapper categoryMapper;
    /** 预算数据访问层 */
    private final BudgetMapper budgetMapper;
    /** Jackson JSON 序列化工具（用于将图片列表等字段序列化为 JSON 字符串存储） */
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AccountingResponse create(AccountingCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 检查分类是否存在
        AccountingCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.ACCOUNTING_CATEGORY_NOT_FOUND);
        }

        Accounting accounting = new Accounting();
        BeanUtils.copyProperties(request, accounting, "images");
        accounting.setUserId(userId);
        if (request.getImages() != null) {
            accounting.setImages(toJson(request.getImages()));
        }
        accountingMapper.insert(accounting);
        log.info("创建账目成功: userId={}, amount={}", userId, accounting.getAmount());
        return toResponse(accounting, category);
    }

    @Override
    public IPage<AccountingResponse> page(int pageNum, int pageSize,
                                          LocalDate startDate, LocalDate endDate, String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Accounting> wrapper = new LambdaQueryWrapper<Accounting>()
                .eq(Accounting::getUserId, userId)
                .ge(startDate != null, Accounting::getAccountingDate, startDate)
                .le(endDate != null, Accounting::getAccountingDate, endDate)
                .eq(type != null, Accounting::getType, type)
                .orderByDesc(Accounting::getAccountingDate)
                .orderByDesc(Accounting::getId);

        IPage<Accounting> accountingPage = accountingMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return accountingPage.convert(a -> {
            AccountingCategory cat = categoryMapper.selectById(a.getCategoryId());
            return toResponse(a, cat);
        });
    }

    @Override
    public AccountingResponse getById(Long id) {
        Accounting accounting = getAndValidate(id);
        AccountingCategory category = categoryMapper.selectById(accounting.getCategoryId());
        return toResponse(accounting, category);
    }

    @Override
    @Transactional
    public AccountingResponse update(Long id, AccountingCreateRequest request) {
        Accounting accounting = getAndValidate(id);
        BeanUtils.copyProperties(request, accounting, "id", "userId", "images");
        if (request.getImages() != null) {
            accounting.setImages(toJson(request.getImages()));
        }
        accountingMapper.updateById(accounting);
        AccountingCategory category = categoryMapper.selectById(accounting.getCategoryId());
        return toResponse(accounting, category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getAndValidate(id);
        accountingMapper.deleteById(id);
    }

    @Override
    public AccountingStatsResponse dailyStats(LocalDate date) {
        LocalDate d = date != null ? date : DateUtils.today();
        return buildStats(d, d);
    }

    @Override
    public AccountingStatsResponse monthlyStats(int year, int month) {
        return buildStats(DateUtils.monthStart(year, month), DateUtils.monthEnd(year, month));
    }

    @Override
    public AccountingStatsResponse yearlyStats(int year) {
        return buildStats(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    @Override
    public List<AccountingStatsResponse.CategoryStat> categoryStats(LocalDate startDate, LocalDate endDate, String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Accounting> list = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(startDate != null, Accounting::getAccountingDate, startDate)
                        .le(endDate != null, Accounting::getAccountingDate, endDate)
                        .eq(type != null, Accounting::getType, type));

        // 计算总金额
        BigDecimal totalAmount = list.stream().map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 按分类聚合
        Map<Long, BigDecimal> categoryAmountMap = list.stream()
                .collect(Collectors.groupingBy(Accounting::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Accounting::getAmount, BigDecimal::add)));

        return categoryAmountMap.entrySet().stream().map(e -> {
            AccountingStatsResponse.CategoryStat stat = new AccountingStatsResponse.CategoryStat();
            stat.setCategoryId(e.getKey());
            AccountingCategory cat = categoryMapper.selectById(e.getKey());
            stat.setCategoryName(cat != null ? cat.getName() : "未知");
            stat.setType(type);
            stat.setAmount(e.getValue());
            stat.setPercentage(totalAmount.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    e.getValue().divide(totalAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue());
            return stat;
        }).sorted(Comparator.comparing(AccountingStatsResponse.CategoryStat::getAmount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setBudget(BudgetCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 查询是否已存在
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getBudgetYear, request.getBudgetYear())
                .eq(Budget::getBudgetMonth, request.getBudgetMonth());
        if (request.getCategoryId() != null) {
            wrapper.eq(Budget::getCategoryId, request.getCategoryId());
        } else {
            wrapper.isNull(Budget::getCategoryId);
        }

        Budget existing = budgetMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setAmount(request.getAmount());
            budgetMapper.updateById(existing);
        } else {
            Budget budget = new Budget();
            budget.setUserId(userId);
            budget.setCategoryId(request.getCategoryId());
            budget.setBudgetYear(request.getBudgetYear());
            budget.setBudgetMonth(request.getBudgetMonth());
            budget.setAmount(request.getAmount());
            budgetMapper.insert(budget);
        }
    }

    @Override
    public Map<String, Object> getBudget(int year, int month) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 查询所有预算
        List<Budget> budgets = budgetMapper.selectList(
                new LambdaQueryWrapper<Budget>()
                        .eq(Budget::getUserId, userId)
                        .eq(Budget::getBudgetYear, year)
                        .eq(Budget::getBudgetMonth, month));

        // 查询该月实际支出
        AccountingStatsResponse monthlyStats = monthlyStats(year, month);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("month", month);
        result.put("budgets", budgets);
        result.put("totalExpense", monthlyStats.getTotalExpense());
        result.put("totalIncome", monthlyStats.getTotalIncome());
        result.put("balance", monthlyStats.getBalance());

        // 总预算执行情况
        budgets.stream().filter(b -> b.getCategoryId() == null).findFirst().ifPresent(total -> {
            result.put("totalBudget", total.getAmount());
            BigDecimal expense = monthlyStats.getTotalExpense() != null ? monthlyStats.getTotalExpense() : BigDecimal.ZERO;
            result.put("budgetUsed", expense);
            result.put("budgetRemaining", total.getAmount().subtract(expense));
            result.put("budgetRate", total.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                    expense.divide(total.getAmount(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> getCategories(String type) {
        LambdaQueryWrapper<AccountingCategory> wrapper = new LambdaQueryWrapper<AccountingCategory>()
                .isNull(AccountingCategory::getUserId) // 系统预设
                .isNull(AccountingCategory::getParentId) // 一级分类
                .eq(type != null, AccountingCategory::getType, type)
                .orderByAsc(AccountingCategory::getSortOrder);

        List<AccountingCategory> parents = categoryMapper.selectList(wrapper);
        return parents.stream().map(parent -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", parent.getId());
            map.put("name", parent.getName());
            map.put("icon", parent.getIcon());
            map.put("type", parent.getType());
            // 查询子分类
            List<AccountingCategory> children = categoryMapper.selectList(
                    new LambdaQueryWrapper<AccountingCategory>()
                            .eq(AccountingCategory::getParentId, parent.getId())
                            .orderByAsc(AccountingCategory::getSortOrder));
            map.put("children", children);
            return map;
        }).collect(Collectors.toList());
    }

    // =================== 私有辅助方法 ===================

    /**
     * 查询并校验账目归属（私有方法）
     *
     * 根据ID和用户ID查询账目，如果不存在则抛出异常。
     * 同时校验数据归属，确保用户只能操作自己的数据（数据安全）。
     *
     * @param id 账目ID
     * @return Accounting 账目实体
     * @throws BusinessException 账目不存在或不属于当前用户时抛出异常
     */
    private Accounting getAndValidate(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Accounting accounting = accountingMapper.selectOne(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getId, id)
                        .eq(Accounting::getUserId, userId));
        if (accounting == null) {
            throw new BusinessException(ResultCode.ACCOUNTING_NOT_FOUND);
        }
        return accounting;
    }

    /**
     * 构建指定时间段的收支统计（私有方法）
     *
     * 查询指定时间范围内的所有记账记录，使用 BigDecimal 精确计算
     * 收入总额、支出总额和余额。
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return AccountingStatsResponse 统计结果
     */
    private AccountingStatsResponse buildStats(LocalDate startDate, LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Accounting> list = accountingMapper.selectList(
                new LambdaQueryWrapper<Accounting>()
                        .eq(Accounting::getUserId, userId)
                        .ge(Accounting::getAccountingDate, startDate)
                        .le(Accounting::getAccountingDate, endDate));

        BigDecimal income = list.stream()
                .filter(a -> "INCOME".equals(a.getType()))
                .map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = list.stream()
                .filter(a -> "EXPENSE".equals(a.getType()))
                .map(Accounting::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AccountingStatsResponse stats = new AccountingStatsResponse();
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        stats.setTotalIncome(income);
        stats.setTotalExpense(expense);
        stats.setBalance(income.subtract(expense));
        return stats;
    }

    /**
     * 实体转响应 DTO（私有方法）
     *
     * 将 Accounting 实体和 AccountingCategory 实体组装为前端需要的响应格式。
     * 同时查询父分类名称，提供完整的分类层级信息。
     *
     * @param accounting 账目实体
     * @param category   分类实体（可为 null）
     * @return AccountingResponse 响应 DTO
     */
    private AccountingResponse toResponse(Accounting accounting, AccountingCategory category) {
        AccountingResponse response = new AccountingResponse();
        BeanUtils.copyProperties(accounting, response);
        if (category != null) {
            response.setCategoryName(category.getName());
            // 查询父分类名称
            if (category.getParentId() != null) {
                AccountingCategory parent = categoryMapper.selectById(category.getParentId());
                if (parent != null) {
                    response.setParentCategoryName(parent.getName());
                }
            }
        }
        return response;
    }

    /**
     * 对象转 JSON 字符串（私有工具方法）
     *
     * 封装 Jackson 的序列化操作，处理异常时返回 null 并记录日志。
     * 用于将图片列表等 Java 对象序列化为 JSON 字符串存储到数据库。
     *
     * @param obj 待序列化的对象
     * @return JSON 字符串，序列化失败时返回 null
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }
}
