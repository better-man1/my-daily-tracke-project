package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应 DTO
 */
@Data
public class TagResponse {

    private Long id;
    private Long userId;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
