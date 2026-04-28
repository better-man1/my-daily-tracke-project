package com.dailytracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好设置 Mapper
 */
@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
