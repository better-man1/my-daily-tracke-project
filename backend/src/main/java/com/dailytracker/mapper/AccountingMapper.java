package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.Accounting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记账明细 Mapper
 */
@Mapper
public interface AccountingMapper extends BaseMapper<Accounting> {
}
