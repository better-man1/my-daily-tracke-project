package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Data
@TableName("t_tag")
public class Tag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 标签名 */
    private String name;

    /** 标签颜色 */
    private String color;

    /** 使用次数 */
    private Integer usageCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
