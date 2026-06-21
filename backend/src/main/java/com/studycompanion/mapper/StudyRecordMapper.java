package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学习记录表Mapper接口
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /**
     * 获取用户学习统计（SQL聚合，避免加载全部记录）
     */
    @Select("SELECT " +
            "COUNT(DISTINCT study_date) as totalDays, " +
            "COALESCE(SUM(duration), 0) as totalDuration " +
            "FROM study_record WHERE user_id = #{userId}")
    Map<String, Object> getStudyStatsAggregated(@Param("userId") Long userId);

    /**
     * 获取指定日期范围内的学习时长
     */
    @Select("SELECT COALESCE(SUM(duration), 0) FROM study_record " +
            "WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate}")
    Integer getDurationBetween(@Param("userId") Long userId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    /**
     * 获取最近7天每日学习时长
     */
    @Select("SELECT study_date as studyDate, COALESCE(SUM(duration), 0) as totalDuration " +
            "FROM study_record " +
            "WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY study_date ORDER BY study_date")
    List<Map<String, Object>> getDailyDurationsBetween(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 获取科目学习时长分布
     */
    @Select("SELECT s.name as subjectName, COALESCE(SUM(sr.duration), 0) as totalDuration " +
            "FROM study_record sr " +
            "LEFT JOIN subject s ON sr.subject_id = s.id " +
            "WHERE sr.user_id = #{userId} " +
            "GROUP BY s.name")
    List<Map<String, Object>> getSubjectStats(@Param("userId") Long userId);

    /**
     * 获取指定日期范围内的科目学习时长分布
     */
    @Select("SELECT s.name as subjectName, COALESCE(SUM(sr.duration), 0) as totalDuration " +
            "FROM study_record sr " +
            "LEFT JOIN subject s ON sr.subject_id = s.id " +
            "WHERE sr.user_id = #{userId} AND sr.study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY s.name")
    List<Map<String, Object>> getSubjectStatsBetween(@Param("userId") Long userId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * 获取指定日期范围内的每日学习时长（按天分组）
     */
    @Select("SELECT study_date as studyDate, COALESCE(SUM(duration), 0) as totalDuration " +
            "FROM study_record " +
            "WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY study_date ORDER BY study_date")
    List<Map<String, Object>> getDailyDurationsByDateRange(@Param("userId") Long userId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    /**
     * 获取指定日期范围内的学习统计（SQL聚合）
     */
    @Select("SELECT " +
            "COUNT(DISTINCT study_date) as totalDays, " +
            "COALESCE(SUM(duration), 0) as totalDuration " +
            "FROM study_record WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate}")
    Map<String, Object> getStudyStatsAggregatedBetween(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 获取指定日期范围内的最长单日学习时长
     */
    @Select("SELECT COALESCE(MAX(daily_duration), 0) FROM (" +
            "SELECT SUM(duration) as daily_duration FROM study_record " +
            "WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY study_date) t")
    Integer getMaxDailyDuration(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    /**
     * 获取每小时学习时长分布（用于分析最佳学习时段）
     */
    @Select("SELECT HOUR(start_time) as hour, COALESCE(SUM(duration), 0) as totalDuration " +
            "FROM study_record " +
            "WHERE user_id = #{userId} AND start_time IS NOT NULL " +
            "AND study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY HOUR(start_time) ORDER BY hour")
    List<Map<String, Object>> getHourlyDistribution(@Param("userId") Long userId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 获取专注度趋势（按日期）
     */
    @Select("SELECT study_date as studyDate, " +
            "COALESCE(AVG(focus_level), 0) as avgFocus, " +
            "COALESCE(AVG(ai_focus_level), 0) as avgAiFocus " +
            "FROM study_record " +
            "WHERE user_id = #{userId} AND study_date BETWEEN #{startDate} AND #{endDate} " +
            "AND focus_level IS NOT NULL " +
            "GROUP BY study_date ORDER BY study_date")
    List<Map<String, Object>> getFocusTrend(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * 获取科目专注度分布
     */
    @Select("SELECT s.name as subjectName, " +
            "COALESCE(AVG(sr.focus_level), 0) as avgFocus, " +
            "COUNT(*) as sessionCount " +
            "FROM study_record sr " +
            "LEFT JOIN subject s ON sr.subject_id = s.id " +
            "WHERE sr.user_id = #{userId} AND sr.focus_level IS NOT NULL " +
            "AND sr.study_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY s.name")
    List<Map<String, Object>> getSubjectFocusStats(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
