package com.dailytracker.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 记账明细响应 DTO
 */
@Data
public class AccountingResponse {

    private Long id;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private String parentCategoryName;
    private String accountType;
    private String remark;
    private String images;
    private LocalDate accountingDate;
    private LocalTime accountingTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
