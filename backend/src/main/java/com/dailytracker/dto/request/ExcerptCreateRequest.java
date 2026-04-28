package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建/更新摘录请求 DTO
 */
@Data
public class ExcerptCreateRequest {

    /** 摘录内容（必填） */
    @NotBlank(message = "摘录内容不能为空")
    private String content;

    /** 来源类型：BOOK/ARTICLE/VIDEO/PODCAST/OTHER */
    private String sourceType = "OTHER";

    /** 来源标题 */
    private String sourceTitle;

    /** 来源链接 */
    private String sourceUrl;

    /** 个人感悟 */
    private String thought;

    /** 图片URL列表 */
    private List<String> images;

    /** 摘录日期（必填） */
    @NotNull(message = "摘录日期不能为空")
    private LocalDate excerptDate;

    /** 标签ID列表 */
    private List<Long> tagIds;

    /** 是否收藏 */
    private Integer isFavorite = 0;
}
