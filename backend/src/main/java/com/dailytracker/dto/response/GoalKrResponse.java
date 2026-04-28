package com.dailytracker.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class GoalKrResponse {
    private Long id;
    private String title;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private Integer progress;
    private Integer sortOrder;
}
