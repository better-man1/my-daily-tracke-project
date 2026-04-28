package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Excerpt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日摘录 Mapper
 */
@Mapper
public interface ExcerptMapper extends BaseMapper<Excerpt> {
}
