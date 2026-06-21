package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.ExamRequest;
import com.studycompanion.service.ExamService;
import com.studycompanion.vo.ExamVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "考试管理", description = "考试倒计时管理")
@RestController
@RequestMapping("/api/v1/exams")
public class ExamController extends BaseController {

    private final ExamService examService;

    public ExamController(JwtUtil jwtUtil, ExamService examService) {
        super(jwtUtil);
        this.examService = examService;
    }

    @Operation(summary = "获取考试列表")
    @GetMapping
    public Result<List<ExamVO>> getExams(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<ExamVO> exams = examService.getExams(userId);
        return Result.success(exams);
    }

    @Operation(summary = "创建考试")
    @PostMapping
    public Result<ExamVO> createExam(HttpServletRequest request,
                                     @Valid @RequestBody ExamRequest body) {
        Long userId = getUserIdFromRequest(request);
        ExamVO exam = examService.createExam(userId, body);
        return Result.success("创建成功", exam);
    }

    @Operation(summary = "更新考试")
    @PutMapping("/{examId}")
    public Result<ExamVO> updateExam(HttpServletRequest request,
                                     @PathVariable Long examId,
                                     @Valid @RequestBody ExamRequest body) {
        Long userId = getUserIdFromRequest(request);
        ExamVO exam = examService.updateExam(userId, examId, body);
        return Result.success("更新成功", exam);
    }

    @Operation(summary = "删除考试")
    @DeleteMapping("/{examId}")
    public Result<Void> deleteExam(HttpServletRequest request,
                                   @PathVariable Long examId) {
        Long userId = getUserIdFromRequest(request);
        examService.deleteExam(userId, examId);
        return Result.success("删除成功", null);
    }
}
