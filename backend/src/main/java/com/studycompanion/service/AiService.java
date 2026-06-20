package com.studycompanion.service;

import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.vo.AiAnalysisVO;
import com.studycompanion.vo.AiChatResponse;
import com.studycompanion.vo.ShareImageVO;

public interface AiService {

    AiAnalysisVO generateWeeklyReport(Long userId);

    AiAnalysisVO generateMonthlyReport(Long userId);

    AiChatResponse chat(Long userId, AiChatRequest request);

    AiAnalysisVO judgeFocusLevel(Long userId, String studyContext);

    ShareImageVO generateShareImage(Long userId);
}
