package com.dailytracker.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新任务请求 DTO
 */
@Data
public class PlanBatchUpdateRequest {

    /** 任务ID列表 */
    @NotEmpty(message = "任务ID列表不能为空")
    private List<Long> ids;

    /** 新优先级（可选） */
    private String priority;

    /** 新分类（可选） */
    private String category;

    /** 新状态（可选） */
    private String status;
}
