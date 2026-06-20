package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.MissRecordRequest;
import com.studycompanion.service.CheckInService;
import com.studycompanion.service.MissRecordService;
import com.studycompanion.vo.CheckInStatusVO;
import com.studycompanion.vo.MissRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "打卡与断签", description = "打卡、连续天数、断签记录、补签")
@RestController
@RequestMapping("/api/v1/check-in")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final MissRecordService missRecordService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "获取今日打卡状态")
    @GetMapping("/today")
    public Result<CheckInStatusVO> getTodayCheckInStatus(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        CheckInStatusVO status = checkInService.getTodayCheckInStatus(userId);
        return Result.success(status);
    }

    @Operation(summary = "打卡")
    @PostMapping
    public Result<CheckInStatusVO> checkIn(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        CheckInStatusVO status = checkInService.checkIn(userId);
        return Result.success("打卡成功", status);
    }

    @Operation(summary = "获取打卡历史")
    @GetMapping("/history")
    public Result<List<CheckInStatusVO>> getCheckInHistory(HttpServletRequest request,
                                                           @RequestParam(required = false) String month) {
        Long userId = getUserIdFromRequest(request);
        List<CheckInStatusVO> history = checkInService.getCheckInHistory(userId, month);
        return Result.success(history);
    }

    @Operation(summary = "记录断签")
    @PostMapping("/miss")
    public Result<MissRecordVO> recordMiss(HttpServletRequest request,
                                           @Valid @RequestBody MissRecordRequest body) {
        Long userId = getUserIdFromRequest(request);
        MissRecordVO record = missRecordService.recordMiss(userId, body);
        return Result.success(record);
    }

    @Operation(summary = "AI判断补签")
    @PostMapping("/miss/{missRecordId}/ai-judge")
    public Result<MissRecordVO> aiJudgeReplenish(HttpServletRequest request,
                                                 @PathVariable Long missRecordId) {
        Long userId = getUserIdFromRequest(request);
        MissRecordVO record = missRecordService.aiJudgeReplenish(userId, missRecordId);
        return Result.success(record);
    }

    @Operation(summary = "补签")
    @PostMapping("/miss/{missRecordId}/replenish")
    public Result<MissRecordVO> replenish(HttpServletRequest request,
                                          @PathVariable Long missRecordId) {
        Long userId = getUserIdFromRequest(request);
        MissRecordVO record = missRecordService.replenish(userId, missRecordId);
        return Result.success("补签成功", record);
    }

    @Operation(summary = "获取断签记录")
    @GetMapping("/miss")
    public Result<List<MissRecordVO>> getMissRecords(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<MissRecordVO> records = missRecordService.getMissRecords(userId);
        return Result.success(records);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
