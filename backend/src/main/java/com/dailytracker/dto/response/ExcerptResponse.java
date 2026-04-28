package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 摘录响应 DTO
 */
@Data
public class ExcerptResponse {

    private Long id;
    private Long userId;
    private String content;
    private String sourceType;
    private String sourceTitle;
    private String sourceUrl;
    private String thought;
    private String images;
    private Integer isFavorite;
    private LocalDate excerptDate;

    /** 关联标签列表 */
    private List<TagInfo> tags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }
}
