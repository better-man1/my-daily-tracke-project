package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.AccountingCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 记账分类 Mapper
 */
@Mapper
public interface AccountingCategoryMapper extends BaseMapper<AccountingCategory> {
}
