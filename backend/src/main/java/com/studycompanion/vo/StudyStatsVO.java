package com.studycompanion.vo;

import lombok.Data;

import java.util.Map;

@Data
public class StudyStatsVO {

    private Integer totalDays;
    private Integer totalDuration;
    private Integer todayDuration;
    private Integer weekDuration;
    private Integer monthDuration;
    private Map<String, Integer> subjectStats;
}
