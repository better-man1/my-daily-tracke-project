package com.dailytracker.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 更新重复规则请求 DTO
 */
@Data
public class RepeatUpdateRequest {

    /** 重复类型：NONE/DAILY/WEEKLY/MONTHLY/CUSTOM */
    @Pattern(regexp = "NONE|DAILY|WEEKLY|MONTHLY|CUSTOM", message = "重复类型格式错误")
    private String repeatType;

    /** 重复模式（JSON格式，如 {"days":[1,3,5]} 表示周一三五） */
    private String repeatPattern;

    /** 重复结束日期 */
    private LocalDate repeatEndDate;
}
