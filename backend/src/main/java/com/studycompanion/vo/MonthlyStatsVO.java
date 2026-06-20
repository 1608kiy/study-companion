package com.studycompanion.vo;

import lombok.Data;

import java.util.Map;

@Data
public class MonthlyStatsVO {

    private Integer totalDays;
    private Integer totalDuration;
    private Map<Integer, Integer> dailyDuration;
    private Map<String, Integer> subjectDuration;
}
