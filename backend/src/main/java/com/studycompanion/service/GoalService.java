package com.studycompanion.service;

import com.studycompanion.dto.CreateGoalRequest;
import com.studycompanion.dto.UpdateGoalRequest;
import com.studycompanion.vo.GoalVO;
import com.studycompanion.vo.MonthlyStatsVO;
import com.studycompanion.vo.StudyStatsVO;

import java.util.List;

public interface GoalService {

    List<GoalVO> getGoalList(Long userId, Integer goalType);

    GoalVO createGoal(Long userId, CreateGoalRequest request);

    GoalVO updateGoal(Long userId, Long goalId, UpdateGoalRequest request);

    void deleteGoal(Long userId, Long goalId);

    GoalVO aiSuggestGoal(Long userId, Integer goalType);

    StudyStatsVO getDailyStats(Long userId, String date);

    StudyStatsVO getWeeklyStats(Long userId, String week);

    StudyStatsVO getMonthlyStats(Long userId, String month);

    MonthlyStatsVO getMonthlyCalendarStats(Long userId, String month);
}
