package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 用户偏好设置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_settings")
public class UserSettings extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 主题(light/dark) */
    private String theme;

    /** 默认首页视图 */
    private String defaultView;

    /** 每日提醒时间 */
    private LocalTime reminderTime;

    /** 周起始日(1=周一,7=周日) */
    private Integer weekStartDay;

    /** 语言 */
    private String language;
}
