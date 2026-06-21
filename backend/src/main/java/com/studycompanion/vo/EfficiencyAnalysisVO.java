package com.studycompanion.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EfficiencyAnalysisVO {
    // 最佳学习时段（每小时分布）
    private Map<Integer, Integer> hourlyDistribution;
    // 最佳学习时段（推荐）
    private List<Integer> bestHours;
    
    // 科目分布
    private Map<String, Integer> subjectDistribution;
    // 科目专注度
    private Map<String, Double> subjectFocus;
    
    // 专注度趋势（最近30天）
    private List<FocusTrendItem> focusTrend;
    
    // 总体统计
    private Integer totalSessions;
    private Double avgFocusLevel;
    private Integer bestDayOfWeek; // 1=周一, 7=周日
    private Integer bestHourOfDay;
    
    @Data
    public static class FocusTrendItem {
        private String date;
        private Double focusLevel;
        private Double aiFocusLevel;
    }
}
