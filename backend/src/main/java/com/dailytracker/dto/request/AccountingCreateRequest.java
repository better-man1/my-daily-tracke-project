package com.dailytracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 创建/更新记账请求 DTO
 */
@Data
public class AccountingCreateRequest {

    /** 类型：INCOME/EXPENSE（必填） */
    @NotBlank(message = "记账类型不能为空")
    private String type;

    /** 金额（必填，大于0） */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0", message = "金额不能小于0")
    private BigDecimal amount;

    /** 分类ID（必填） */
    @NotNull(message = "记账分类不能为空")
    private Long categoryId;

    /** 账户：CASH/WECHAT/ALIPAY/BANK */
    private String accountType = "WECHAT";

    /** 备注 */
    private String remark;

    /** 凭证图片列表 */
    private List<String> images;

    /** 记账日期（必填） */
    @NotNull(message = "记账日期不能为空")
    private LocalDate accountingDate;

    /** 记账时间 */
    private LocalTime accountingTime;
}
