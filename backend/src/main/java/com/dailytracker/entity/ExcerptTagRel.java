package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 摘录-标签关联实体
 */
@Data
@TableName("t_excerpt_tag_rel")
public class ExcerptTagRel implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 摘录ID */
    private Long excerptId;

    /** 标签ID */
    private Long tagId;
}
