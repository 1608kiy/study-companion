package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.CreateGoalRequest;
import com.studycompanion.dto.UpdateGoalRequest;
import com.studycompanion.service.GoalService;
import com.studycompanion.vo.GoalVO;
import com.studycompanion.vo.MonthlyStatsVO;
import com.studycompanion.vo.StudyStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "目标模块", description = "目标CRUD、统计")
@RestController
@RequestMapping("/api/v1/goals")
public class GoalController extends BaseController {

    private final GoalService goalService;

    public GoalController(JwtUtil jwtUtil, GoalService goalService) {
        super(jwtUtil);
        this.goalService = goalService;
    }

    @Operation(summary = "获取目标列表")
    @GetMapping
    public Result<List<GoalVO>> getGoalList(HttpServletRequest request,
                                            @RequestParam(required = false) Integer goalType) {
        Long userId = getUserIdFromRequest(request);
        List<GoalVO> goals = goalService.getGoalList(userId, goalType);
        return Result.success(goals);
    }

    @Operation(summary = "创建目标")
    @PostMapping
    public Result<GoalVO> createGoal(HttpServletRequest request,
                                     @Valid @RequestBody CreateGoalRequest body) {
        Long userId = getUserIdFromRequest(request);
        GoalVO goal = goalService.createGoal(userId, body);
        return Result.success("创建成功", goal);
    }

    @Operation(summary = "更新目标")
    @PutMapping("/{goalId}")
    public Result<GoalVO> updateGoal(HttpServletRequest request,
                                     @PathVariable Long goalId,
                                     @Valid @RequestBody UpdateGoalRequest body) {
        Long userId = getUserIdFromRequest(request);
        GoalVO goal = goalService.updateGoal(userId, goalId, body);
        return Result.success("更新成功", goal);
    }

    @Operation(summary = "删除目标")
    @DeleteMapping("/{goalId}")
    public Result<Void> deleteGoal(HttpServletRequest request,
                                   @PathVariable Long goalId) {
        Long userId = getUserIdFromRequest(request);
        goalService.deleteGoal(userId, goalId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "AI推荐目标")
    @PostMapping("/ai-suggest")
    public Result<GoalVO> aiSuggestGoal(HttpServletRequest request,
                                        @RequestParam Integer goalType) {
        Long userId = getUserIdFromRequest(request);
        GoalVO goal = goalService.aiSuggestGoal(userId, goalType);
        return Result.success(goal);
    }

    @Operation(summary = "每日统计")
    @GetMapping("/stats/daily")
    public Result<StudyStatsVO> getDailyStats(HttpServletRequest request,
                                              @RequestParam(required = false) String date) {
        Long userId = getUserIdFromRequest(request);
        StudyStatsVO stats = goalService.getDailyStats(userId, date);
        return Result.success(stats);
    }

    @Operation(summary = "每周统计")
    @GetMapping("/stats/weekly")
    public Result<StudyStatsVO> getWeeklyStats(HttpServletRequest request,
                                               @RequestParam(required = false) String week) {
        Long userId = getUserIdFromRequest(request);
        StudyStatsVO stats = goalService.getWeeklyStats(userId, week);
        return Result.success(stats);
    }

    @Operation(summary = "每月统计")
    @GetMapping("/stats/monthly")
    public Result<StudyStatsVO> getMonthlyStats(HttpServletRequest request,
                                                @RequestParam(required = false) String month) {
        Long userId = getUserIdFromRequest(request);
        StudyStatsVO stats = goalService.getMonthlyStats(userId, month);
        return Result.success(stats);
    }

    @Operation(summary = "月历统计")
    @GetMapping("/stats/calendar")
    public Result<MonthlyStatsVO> getMonthlyCalendarStats(HttpServletRequest request,
                                                          @RequestParam(required = false) String month) {
        Long userId = getUserIdFromRequest(request);
        MonthlyStatsVO stats = goalService.getMonthlyCalendarStats(userId, month);
        return Result.success(stats);
    }
}
