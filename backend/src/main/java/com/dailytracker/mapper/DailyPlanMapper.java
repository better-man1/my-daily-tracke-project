package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.DailyPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日计划 Mapper
 */
@Mapper
public interface DailyPlanMapper extends BaseMapper<DailyPlan> {
}
