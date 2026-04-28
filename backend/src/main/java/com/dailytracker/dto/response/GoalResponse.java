package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 目标响应 DTO
 */
@Data
public class GoalResponse {

    private Long id;
    private Long userId;
    private Long parentId;
    private String title;
    private String description;
    private String goalType;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer progress;
    private String status;
    private Integer priority;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 子目标列表（树形展示时使用） */
    private List<GoalResponse> children;

    /** 关键结果列表 */
    private List<GoalKrResponse> keyResults;
}
