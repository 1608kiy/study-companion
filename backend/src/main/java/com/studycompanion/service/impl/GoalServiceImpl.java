package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.CreateGoalRequest;
import com.studycompanion.dto.UpdateGoalRequest;
import com.studycompanion.entity.Goal;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.GoalMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.GoalService;
import com.studycompanion.vo.GoalVO;
import com.studycompanion.vo.MonthlyStatsVO;
import com.studycompanion.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalMapper goalMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final SubjectMapper subjectMapper;

    @Override
    public List<GoalVO> getGoalList(Long userId, Integer goalType) {
        LambdaQueryWrapper<Goal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goal::getUserId, userId);
        if (goalType != null) {
            wrapper.eq(Goal::getGoalType, goalType);
        }
        wrapper.orderByDesc(Goal::getCreateTime);

        List<Goal> goals = goalMapper.selectList(wrapper);
        return goals.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalVO createGoal(Long userId, CreateGoalRequest request) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(request.getGoalType());
        goal.setGoalValue(request.getGoalValue());
        goal.setCurrentValue(0);
        goal.setGoalDate(request.getGoalDate());
        goal.setIsCompleted(0);
        goal.setAiSuggested(0);

        goalMapper.insert(goal);
        return convertToVO(goal);
    }

    @Override
    @Transactional
    public GoalVO updateGoal(Long userId, Long goalId, UpdateGoalRequest request) {
        Goal goal = getGoalById(userId, goalId);

        if (request.getGoalType() != null) {
            goal.setGoalType(request.getGoalType());
        }
        if (request.getGoalValue() != null) {
            goal.setGoalValue(request.getGoalValue());
        }
        if (request.getGoalDate() != null) {
            goal.setGoalDate(request.getGoalDate());
        }
        if (request.getIsCompleted() != null) {
            goal.setIsCompleted(request.getIsCompleted());
        }

        goalMapper.updateById(goal);
        return convertToVO(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = getGoalById(userId, goalId);
        goalMapper.deleteById(goalId);
    }

    @Override
    public GoalVO aiSuggestGoal(Long userId, Integer goalType) {
        // TODO: 接入AI服务生成目标建议
        // 目前简单实现：根据历史数据生成建议
        int suggestedValue = 120; // 默认每日2小时

        if (goalType == 2) {
            suggestedValue = 840; // 每周14小时
        } else if (goalType == 3) {
            suggestedValue = 3600; // 每月60小时
        }

        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setGoalType(goalType);
        goal.setGoalValue(suggestedValue);
        goal.setCurrentValue(0);
        goal.setGoalDate(LocalDate.now());
        goal.setIsCompleted(0);
        goal.setAiSuggested(1);

        goalMapper.insert(goal);
        return convertToVO(goal);
    }

    @Override
    public StudyStatsVO getDailyStats(Long userId, String date) {
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return calculateStats(userId, targetDate, targetDate);
    }

    @Override
    public StudyStatsVO getWeeklyStats(Long userId, String week) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart;
        
        if (week != null) {
            weekStart = LocalDate.parse(week);
        } else {
            weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        
        LocalDate weekEnd = weekStart.plusDays(6);
        return calculateStats(userId, weekStart, weekEnd);
    }

    @Override
    public StudyStatsVO getMonthlyStats(Long userId, String month) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        return calculateStats(userId, monthStart, monthEnd);
    }

    @Override
    public MonthlyStatsVO getMonthlyCalendarStats(Long userId, String month) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .ge(StudyRecord::getStudyDate, monthStart)
               .le(StudyRecord::getStudyDate, monthEnd);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        MonthlyStatsVO stats = new MonthlyStatsVO();
        
        // 统计总学习天数
        Set<LocalDate> uniqueDays = records.stream()
                .map(StudyRecord::getStudyDate)
                .collect(Collectors.toSet());
        stats.setTotalDays(uniqueDays.size());

        // 统计总学习时长
        int totalDuration = records.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setTotalDuration(totalDuration);

        // 统计每日学习时长
        Map<Integer, Integer> dailyDuration = new HashMap<>();
        for (StudyRecord record : records) {
            int day = record.getStudyDate().getDayOfMonth();
            dailyDuration.merge(day, record.getDuration(), Integer::sum);
        }
        stats.setDailyDuration(dailyDuration);

        // 统计科目学习时长
        Map<String, Integer> subjectDuration = new HashMap<>();
        for (StudyRecord record : records) {
            Subject subject = subjectMapper.selectById(record.getSubjectId());
            if (subject != null) {
                subjectDuration.merge(subject.getName(), record.getDuration(), Integer::sum);
            }
        }
        stats.setSubjectDuration(subjectDuration);

        return stats;
    }

    private StudyStatsVO calculateStats(Long userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
               .ge(StudyRecord::getStudyDate, startDate)
               .le(StudyRecord::getStudyDate, endDate);
        List<StudyRecord> records = studyRecordMapper.selectList(wrapper);

        StudyStatsVO stats = new StudyStatsVO();

        // 统计总学习天数
        Set<LocalDate> uniqueDays = records.stream()
                .map(StudyRecord::getStudyDate)
                .collect(Collectors.toSet());
        stats.setTotalDays(uniqueDays.size());

        // 统计总学习时长
        int totalDuration = records.stream()
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setTotalDuration(totalDuration);

        // 统计今日学习时长
        LocalDate today = LocalDate.now();
        int todayDuration = records.stream()
                .filter(r -> r.getStudyDate().equals(today))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setTodayDuration(todayDuration);

        // 统计本周学习时长
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        int weekDuration = records.stream()
                .filter(r -> !r.getStudyDate().isBefore(weekStart))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setWeekDuration(weekDuration);

        // 统计本月学习时长
        LocalDate monthStart = today.withDayOfMonth(1);
        int monthDuration = records.stream()
                .filter(r -> !r.getStudyDate().isBefore(monthStart))
                .mapToInt(StudyRecord::getDuration)
                .sum();
        stats.setMonthDuration(monthDuration);

        // 统计科目学习时长
        Map<String, Integer> subjectStats = new HashMap<>();
        for (StudyRecord record : records) {
            Subject subject = subjectMapper.selectById(record.getSubjectId());
            if (subject != null) {
                subjectStats.merge(subject.getName(), record.getDuration(), Integer::sum);
            }
        }
        stats.setSubjectStats(subjectStats);

        return stats;
    }

    private Goal getGoalById(Long userId, Long goalId) {
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !goal.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.GOAL_NOT_FOUND);
        }
        return goal;
    }

    private GoalVO convertToVO(Goal goal) {
        GoalVO vo = new GoalVO();
        vo.setId(goal.getId());
        vo.setGoalType(goal.getGoalType());
        vo.setGoalTypeName(getGoalTypeName(goal.getGoalType()));
        vo.setGoalValue(goal.getGoalValue());
        vo.setCurrentValue(goal.getCurrentValue());
        vo.setGoalDate(goal.getGoalDate());
        vo.setIsCompleted(goal.getIsCompleted() == 1);
        vo.setAiSuggested(goal.getAiSuggested() == 1);
        vo.setCreateTime(goal.getCreateTime());
        vo.setUpdateTime(goal.getUpdateTime());
        return vo;
    }

    private String getGoalTypeName(Integer goalType) {
        switch (goalType) {
            case 1: return "每日目标";
            case 2: return "每周目标";
            case 3: return "每月目标";
            default: return "未知类型";
        }
    }
}
