package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.DailySummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日总结 Mapper
 */
@Mapper
public interface DailySummaryMapper extends BaseMapper<DailySummary> {
}
