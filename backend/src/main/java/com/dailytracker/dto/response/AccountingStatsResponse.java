package com.dailytracker.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 记账统计响应 DTO
 */
@Data
public class AccountingStatsResponse {

    /** 统计起始日期 */
    private LocalDate startDate;

    /** 统计结束日期 */
    private LocalDate endDate;

    /** 总收入 */
    private BigDecimal totalIncome;

    /** 总支出 */
    private BigDecimal totalExpense;

    /** 结余（收入-支出） */
    private BigDecimal balance;

    /** 分类统计列表 */
    private List<CategoryStat> categoryStats;

    @Data
    public static class CategoryStat {
        /** 分类ID */
        private Long categoryId;
        /** 分类名称 */
        private String categoryName;
        /** 类型：INCOME/EXPENSE */
        private String type;
        /** 金额 */
        private BigDecimal amount;
        /** 占比（%） */
        private Double percentage;
    }
}
