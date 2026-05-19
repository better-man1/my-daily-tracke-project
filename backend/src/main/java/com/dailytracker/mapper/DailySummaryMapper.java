package com.dailytracker.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailytracker.entity.DailySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDate;

@Mapper
public interface DailySummaryMapper extends BaseMapper<DailySummary> {
    @Select("SELECT * FROM t_daily_summary WHERE user_id = #{userId} AND summary_date = #{summaryDate}")
    DailySummary selectByDateIgnoreDeleted(@Param("userId") Long userId, @Param("summaryDate") LocalDate summaryDate);

    @Update("UPDATE t_daily_summary SET is_deleted = 0, mood = #{s.mood}, score = #{s.score}, achievement = #{s.achievement}, improvement = #{s.improvement}, tomorrow_plan = #{s.tomorrowPlan}, gratitude = #{s.gratitude}, health_note = #{s.healthNote}, free_writing = #{s.freeWriting}, tags = #{s.tags}, updated_at = NOW() WHERE id = #{s.id}")
    int restoreAndUpdate(@Param("s") DailySummary s);
}
