package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.StartTimerRequest;
import com.studycompanion.dto.UpdateStudyRecordRequest;
import com.studycompanion.service.StudyRecordService;
import com.studycompanion.vo.PageResponse;
import com.studycompanion.vo.StudyRecordVO;
import com.studycompanion.vo.StudyStatsVO;
import com.studycompanion.vo.TimerStateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "学习记录", description = "番茄钟计时、学习记录CRUD、学习统计")
@RestController
@RequestMapping("/api/v1/study-records")
@RequiredArgsConstructor
public class StudyRecordController {

    private final StudyRecordService studyRecordService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "开始计时")
    @PostMapping("/timer/start")
    public Result<TimerStateVO> startTimer(HttpServletRequest request,
                                           @Valid @RequestBody StartTimerRequest body) {
        Long userId = getUserIdFromRequest(request);
        TimerStateVO state = studyRecordService.startTimer(userId, body);
        return Result.success(state);
    }

    @Operation(summary = "暂停计时")
    @PostMapping("/timer/pause")
    public Result<TimerStateVO> pauseTimer(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TimerStateVO state = studyRecordService.pauseTimer(userId);
        return Result.success(state);
    }

    @Operation(summary = "恢复计时")
    @PostMapping("/timer/resume")
    public Result<TimerStateVO> resumeTimer(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TimerStateVO state = studyRecordService.resumeTimer(userId);
        return Result.success(state);
    }

    @Operation(summary = "停止计时")
    @PostMapping("/timer/stop")
    public Result<StudyRecordVO> stopTimer(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        StudyRecordVO record = studyRecordService.stopTimer(userId);
        return Result.success("计时结束", record);
    }

    @Operation(summary = "获取计时状态")
    @GetMapping("/timer/state")
    public Result<TimerStateVO> getTimerState(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TimerStateVO state = studyRecordService.getTimerState(userId);
        return Result.success(state);
    }

    @Operation(summary = "获取学习记录列表")
    @GetMapping
    public Result<List<StudyRecordVO>> getStudyRecords(HttpServletRequest request,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        Long userId = getUserIdFromRequest(request);
        List<StudyRecordVO> records = studyRecordService.getStudyRecords(userId, startDate, endDate);
        return Result.success(records);
    }

    @Operation(summary = "获取学习记录列表（分页）")
    @GetMapping("/paged")
    public Result<PageResponse<StudyRecordVO>> getStudyRecordsPaged(HttpServletRequest request,
                                                                     @RequestParam(required = false) String startDate,
                                                                     @RequestParam(required = false) String endDate,
                                                                     @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromRequest(request);
        PageResponse<StudyRecordVO> records = studyRecordService.getStudyRecordsPaged(userId, startDate, endDate, page, size);
        return Result.success(records);
    }

    @Operation(summary = "获取学习记录详情")
    @GetMapping("/{recordId}")
    public Result<StudyRecordVO> getStudyRecordById(HttpServletRequest request,
                                                    @PathVariable Long recordId) {
        Long userId = getUserIdFromRequest(request);
        StudyRecordVO record = studyRecordService.getStudyRecordById(userId, recordId);
        return Result.success(record);
    }

    @Operation(summary = "AI判断是否允许修改")
    @PostMapping("/{recordId}/ai-judge")
    public Result<Map<String, Object>> aiJudgeModify(HttpServletRequest request,
                                                      @PathVariable Long recordId,
                                                      @RequestBody Map<String, String> body) {
        Long userId = getUserIdFromRequest(request);
        String reason = body.getOrDefault("reason", "");
        Map<String, Object> result = studyRecordService.aiJudgeModify(userId, recordId, reason);
        return Result.success(result);
    }

    @Operation(summary = "更新学习记录")
    @PutMapping("/{recordId}")
    public Result<StudyRecordVO> updateStudyRecord(HttpServletRequest request,
                                                   @PathVariable Long recordId,
                                                   @RequestBody UpdateStudyRecordRequest body) {
        Long userId = getUserIdFromRequest(request);
        StudyRecordVO record = studyRecordService.updateStudyRecord(userId, recordId, body);
        return Result.success("更新成功", record);
    }

    @Operation(summary = "删除学习记录")
    @DeleteMapping("/{recordId}")
    public Result<Void> deleteStudyRecord(HttpServletRequest request,
                                          @PathVariable Long recordId) {
        Long userId = getUserIdFromRequest(request);
        studyRecordService.deleteStudyRecord(userId, recordId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取学习统计")
    @GetMapping("/stats")
    public Result<StudyStatsVO> getStudyStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        StudyStatsVO stats = studyRecordService.getStudyStats(userId);
        return Result.success(stats);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
