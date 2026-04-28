package com.dailytracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 设置预算请求 DTO
 */
@Data
public class BudgetCreateRequest {

    /** 分类ID（null=总预算） */
    private Long categoryId;

    /** 预算年份 */
    @NotNull(message = "预算年份不能为空")
    private Integer budgetYear;

    /** 预算月份 */
    @NotNull(message = "预算月份不能为空")
    private Integer budgetMonth;

    /** 预算金额 */
    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;
}
