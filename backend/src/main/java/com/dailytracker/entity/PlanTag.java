package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计划标签实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_plan_tag")
public class PlanTag extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 标签名称 */
    private String name;

    /** 标签颜色 */
    private String color;
}
