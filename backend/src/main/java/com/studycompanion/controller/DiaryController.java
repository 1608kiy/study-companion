package com.studycompanion.controller;

import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.CreateDiaryRequest;
import com.studycompanion.dto.UpdateDiaryRequest;
import com.studycompanion.service.DiaryService;
import com.studycompanion.service.DiaryImageService;
import com.studycompanion.vo.DiaryImageVO;
import com.studycompanion.vo.DiaryVO;
import com.studycompanion.vo.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "日记", description = "日记CRUD、图片上传、AI生成日记")
@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryImageService diaryImageService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "获取日记列表")
    @GetMapping
    public Result<List<DiaryVO>> getDiaryList(HttpServletRequest request,
                                              @RequestParam(required = false) String month) {
        Long userId = getUserIdFromRequest(request);
        List<DiaryVO> diaries = diaryService.getDiaryList(userId, month);
        return Result.success(diaries);
    }

    @Operation(summary = "获取日记列表（分页）")
    @GetMapping("/paged")
    public Result<PageResponse<DiaryVO>> getDiaryListPaged(HttpServletRequest request,
                                                            @RequestParam(required = false) String month,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserIdFromRequest(request);
        PageResponse<DiaryVO> diaries = diaryService.getDiaryListPaged(userId, month, page, size);
        return Result.success(diaries);
    }

    @Operation(summary = "获取指定日期日记")
    @GetMapping("/date/{date}")
    public Result<DiaryVO> getDiaryByDate(HttpServletRequest request,
                                          @PathVariable String date) {
        Long userId = getUserIdFromRequest(request);
        DiaryVO diary = diaryService.getDiaryByDate(userId, date);
        return Result.success(diary);
    }

    @Operation(summary = "创建日记")
    @PostMapping
    public Result<DiaryVO> createDiary(HttpServletRequest request,
                                       @Valid @RequestBody CreateDiaryRequest body) {
        Long userId = getUserIdFromRequest(request);
        DiaryVO diary = diaryService.createDiary(userId, body);
        return Result.success("创建成功", diary);
    }

    @Operation(summary = "更新日记（已禁用 - 日记不可编辑）")
    @PutMapping("/{diaryId}")
    public Result<DiaryVO> updateDiary(HttpServletRequest request,
                                       @PathVariable Long diaryId,
                                       @RequestBody UpdateDiaryRequest body) {
        throw new BusinessException(ErrorCode.DIARY_IMMUTABLE, "日记不可编辑，保证数据真实性");
    }

    @Operation(summary = "删除日记（已禁用 - 日记不可删除）")
    @DeleteMapping("/{diaryId}")
    public Result<Void> deleteDiary(HttpServletRequest request,
                                    @PathVariable Long diaryId) {
        throw new BusinessException(ErrorCode.DIARY_IMMUTABLE, "日记不可删除，保证数据真实性");
    }

    @Operation(summary = "AI生成日记")
    @PostMapping("/generate")
    public Result<DiaryVO> generateDiary(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        DiaryVO diary = diaryService.generateDiary(userId);
        return Result.success("生成成功", diary);
    }

    @Operation(summary = "重新生成日记")
    @PostMapping("/{diaryId}/regenerate")
    public Result<DiaryVO> regenerateDiary(HttpServletRequest request,
                                           @PathVariable Long diaryId) {
        Long userId = getUserIdFromRequest(request);
        DiaryVO diary = diaryService.regenerateDiary(userId, diaryId);
        return Result.success("重新生成成功", diary);
    }

    @Operation(summary = "上传日记图片")
    @PostMapping("/{diaryId}/images")
    public Result<DiaryImageVO> uploadImage(HttpServletRequest request,
                                            @PathVariable Long diaryId,
                                            @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromRequest(request);
        DiaryImageVO image = diaryImageService.uploadImage(userId, diaryId, file);
        return Result.success("上传成功", image);
    }

    @Operation(summary = "删除日记图片")
    @DeleteMapping("/images/{imageId}")
    public Result<Void> deleteImage(HttpServletRequest request,
                                    @PathVariable Long imageId) {
        Long userId = getUserIdFromRequest(request);
        diaryImageService.deleteImage(userId, imageId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取日记图片列表")
    @GetMapping("/{diaryId}/images")
    public Result<List<DiaryImageVO>> getDiaryImages(HttpServletRequest request,
                                                     @PathVariable Long diaryId) {
        Long userId = getUserIdFromRequest(request);
        List<DiaryImageVO> images = diaryImageService.getDiaryImages(userId, diaryId);
        return Result.success(images);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
