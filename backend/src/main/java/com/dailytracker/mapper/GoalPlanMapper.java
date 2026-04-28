package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.GoalPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 目标计划 Mapper
 */
@Mapper
public interface GoalPlanMapper extends BaseMapper<GoalPlan> {
}
