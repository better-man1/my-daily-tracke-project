package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.ExcerptTagRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 摘录-标签关联 Mapper
 */
@Mapper
public interface ExcerptTagRelMapper extends BaseMapper<ExcerptTagRel> {
}
