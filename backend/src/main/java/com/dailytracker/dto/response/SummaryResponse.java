package com.dailytracker.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日总结响应 DTO
 */
@Data
public class SummaryResponse {

    private Long id;
    private Long userId;
    private LocalDate summaryDate;
    private Integer mood;
    private Integer score;
    private String achievement;
    private String improvement;
    private String tomorrowPlan;
    private String gratitude;
    private String healthNote;
    private String freeWriting;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
