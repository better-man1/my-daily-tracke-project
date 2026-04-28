package com.dailytracker.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dailytracker.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 每日摘录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_excerpt")
public class Excerpt extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 摘录内容 */
    private String content;

    /** 来源类型：BOOK/ARTICLE/VIDEO/PODCAST/OTHER */
    private String sourceType;

    /** 来源标题 */
    private String sourceTitle;

    /** 来源链接 */
    private String sourceUrl;

    /** 个人感悟 */
    private String thought;

    /** 图片URL列表（JSON） */
    private String images;

    /** 是否收藏 */
    private Integer isFavorite;

    /** 摘录日期 */
    private LocalDate excerptDate;
}
