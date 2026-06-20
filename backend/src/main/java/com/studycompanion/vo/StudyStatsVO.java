package com.studycompanion.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StudyStatsVO {

    private Integer totalDays;
    private Integer totalDuration;
    private Integer todayDuration;
    private Integer weekDuration;
    private Integer monthDuration;
    private Map<String, Integer> subjectStats;

    // 新增字段
    private Integer currentStreak;
    private List<Integer> weeklyDurations;
    private Integer avgDuration;
    private Integer maxDuration;
    private Map<Integer, Integer> dailyDurations;
}
