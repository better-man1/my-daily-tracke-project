package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.PlanReminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务提醒 Mapper
 */
@Mapper
public interface PlanReminderMapper extends BaseMapper<PlanReminder> {
}
