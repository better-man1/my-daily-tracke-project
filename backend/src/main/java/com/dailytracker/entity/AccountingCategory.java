package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 记账分类实体
 */
@Data
@TableName("t_accounting_category")
public class AccountingCategory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（NULL=系统预设） */
    private Long userId;

    /** 父分类ID */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 图标 */
    private String icon;

    /** 类型：INCOME/EXPENSE */
    private String type;

    /** 排序 */
    private Integer sortOrder;

    /** 是否系统预设 */
    private Integer isSystem;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
