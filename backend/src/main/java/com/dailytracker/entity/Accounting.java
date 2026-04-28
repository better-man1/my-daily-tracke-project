package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 记账明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_accounting")
public class Accounting extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 类型：INCOME/EXPENSE */
    private String type;

    /** 金额 */
    private BigDecimal amount;

    /** 分类ID */
    private Long categoryId;

    /** 账户：CASH/WECHAT/ALIPAY/BANK */
    private String accountType;

    /** 备注 */
    private String remark;

    /** 凭证图片（JSON） */
    private String images;

    /** 记账日期 */
    private LocalDate accountingDate;

    /** 记账时间 */
    private LocalTime accountingTime;
}
