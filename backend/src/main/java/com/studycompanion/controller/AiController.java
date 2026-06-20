package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.dto.FocusJudgeRequest;
import com.studycompanion.entity.AiChatHistory;
import com.studycompanion.mapper.AiChatHistoryMapper;
import com.studycompanion.service.AiService;
import com.studycompanion.vo.AiAnalysisVO;
import com.studycompanion.vo.AiChatResponse;
import com.studycompanion.vo.ShareImageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "AI与分享", description = "AI建议、周报/月报、AI问答、分享图片")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiChatHistoryMapper chatHistoryMapper;
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
        
        // 保存用户消息到历史
        saveChatMessage(userId, "user", body.getQuestion());
        
        AiChatResponse response = aiService.chat(userId, body);
        
        // 保存AI回复到历史
        saveChatMessage(userId, "assistant", response.getAnswer());
        
        return Result.success(response);
    }

    @Operation(summary = "AI判断专注度")
    @PostMapping("/focus-judge")
    public Result<AiAnalysisVO> judgeFocusLevel(HttpServletRequest request,
                                                @RequestBody(required = false) FocusJudgeRequest body) {
        Long userId = getUserIdFromRequest(request);
        String studyContext = body != null ? body.getStudyContext() : null;
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

    @Operation(summary = "获取聊天历史")
    @GetMapping("/chat/history")
    public Result<List<Map<String, String>>> getChatHistory(HttpServletRequest request,
                                                            @RequestParam(defaultValue = "20") int limit) {
        Long userId = getUserIdFromRequest(request);
        
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getUserId, userId)
               .orderByDesc(AiChatHistory::getCreateTime)
               .last("LIMIT " + limit);
        
        List<AiChatHistory> history = chatHistoryMapper.selectList(wrapper);
        
        // 反转为时间正序
        List<Map<String, String>> result = history.stream()
                .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .map(h -> Map.of("role", h.getRole(), "content", h.getContent()))
                .collect(Collectors.toList());
        
        return Result.success(result);
    }

    @Operation(summary = "清空聊天历史")
    @DeleteMapping("/chat/history")
    public Result<Void> clearChatHistory(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        
        LambdaQueryWrapper<AiChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatHistory::getUserId, userId);
        chatHistoryMapper.delete(wrapper);
        
        return Result.success("清空成功", null);
    }

    private void saveChatMessage(Long userId, String role, String content) {
        try {
            AiChatHistory history = new AiChatHistory();
            history.setUserId(userId);
            history.setRole(role);
            history.setContent(content);
            chatHistoryMapper.insert(history);
            
            // 只保留最近100条消息
            LambdaQueryWrapper<AiChatHistory> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(AiChatHistory::getUserId, userId);
            long count = chatHistoryMapper.selectCount(countWrapper);
            
            if (count > 100) {
                LambdaQueryWrapper<AiChatHistory> oldWrapper = new LambdaQueryWrapper<>();
                oldWrapper.eq(AiChatHistory::getUserId, userId)
                          .orderByAsc(AiChatHistory::getCreateTime)
                          .last("LIMIT " + (count - 100));
                
                List<AiChatHistory> oldMessages = chatHistoryMapper.selectList(oldWrapper);
                for (AiChatHistory old : oldMessages) {
                    chatHistoryMapper.deleteById(old.getId());
                }
            }
        } catch (Exception e) {
            // 保存历史失败不影响主流程
            e.printStackTrace();
        }
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
