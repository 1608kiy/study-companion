package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.CreateSubjectRequest;
import com.studycompanion.dto.UpdateSubjectRequest;
import com.studycompanion.service.SubjectService;
import com.studycompanion.vo.SubjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "科目模块", description = "科目CRUD")
@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController extends BaseController {

    private final SubjectService subjectService;

    public SubjectController(JwtUtil jwtUtil, SubjectService subjectService) {
        super(jwtUtil);
        this.subjectService = subjectService;
    }

    @Operation(summary = "获取科目列表")
    @GetMapping
    public Result<List<SubjectVO>> getSubjectList(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<SubjectVO> subjects = subjectService.getSubjectList(userId);
        return Result.success(subjects);
    }

    @Operation(summary = "获取预设科目")
    @GetMapping("/preset")
    public Result<List<SubjectVO>> getPresetSubjects(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<SubjectVO> subjects = subjectService.getPresetSubjects(userId);
        return Result.success(subjects);
    }

    @Operation(summary = "创建科目")
    @PostMapping
    public Result<SubjectVO> createSubject(HttpServletRequest request,
                                           @Valid @RequestBody CreateSubjectRequest body) {
        Long userId = getUserIdFromRequest(request);
        SubjectVO subject = subjectService.createSubject(userId, body);
        return Result.success("创建成功", subject);
    }

    @Operation(summary = "更新科目")
    @PutMapping("/{subjectId}")
    public Result<SubjectVO> updateSubject(HttpServletRequest request,
                                           @PathVariable Long subjectId,
                                           @Valid @RequestBody UpdateSubjectRequest body) {
        Long userId = getUserIdFromRequest(request);
        SubjectVO subject = subjectService.updateSubject(userId, subjectId, body);
        return Result.success("更新成功", subject);
    }

    @Operation(summary = "删除科目")
    @DeleteMapping("/{subjectId}")
    public Result<Void> deleteSubject(HttpServletRequest request,
                                      @PathVariable Long subjectId) {
        Long userId = getUserIdFromRequest(request);
        subjectService.deleteSubject(userId, subjectId);
        return Result.success("删除成功", null);
    }
}
