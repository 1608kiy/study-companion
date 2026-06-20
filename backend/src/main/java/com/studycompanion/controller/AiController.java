package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.service.AiService;
import com.studycompanion.vo.AiAnalysisVO;
import com.studycompanion.vo.AiChatResponse;
import com.studycompanion.vo.ShareImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI与分享", description = "AI建议、周报/月报、AI问答、分享图片")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "生成周报")
    @PostMapping("/weekly-report")
    public Result<AiAnalysisVO> generateWeeklyReport(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        AiAnalysisVO report = aiService.generateWeeklyReport(userId);
        return Result.success("周报生成成功", report);
    }

    @Operation(summary = "生成月报")
    @PostMapping("/monthly-report")
    public Result<AiAnalysisVO> generateMonthlyReport(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        AiAnalysisVO report = aiService.generateMonthlyReport(userId);
        return Result.success("月报生成成功", report);
    }

    @Operation(summary = "AI问答")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(HttpServletRequest request,
                                       @Valid @RequestBody AiChatRequest body) {
        Long userId = getUserIdFromRequest(request);
        AiChatResponse response = aiService.chat(userId, body);
        return Result.success(response);
    }

    @Operation(summary = "AI判断专注度")
    @PostMapping("/focus-judge")
    public Result<AiAnalysisVO> judgeFocusLevel(HttpServletRequest request,
                                                @RequestParam(required = false) String studyContext) {
        Long userId = getUserIdFromRequest(request);
        AiAnalysisVO result = aiService.judgeFocusLevel(userId, studyContext);
        return Result.success(result);
    }

    @Operation(summary = "生成分享图片")
    @GetMapping("/share")
    public Result<ShareImageVO> generateShareImage(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        ShareImageVO shareImage = aiService.generateShareImage(userId);
        return Result.success(shareImage);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
