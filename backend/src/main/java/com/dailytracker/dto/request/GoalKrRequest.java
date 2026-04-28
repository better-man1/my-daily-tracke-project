package com.dailytracker.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalKrRequest {
    private Long id;
    private String title;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private Integer sortOrder;
}
