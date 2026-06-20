package com.studycompanion.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * AI 响应解析工具类
 */
@Slf4j
public class AiResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 AI 返回的 JSON 字符串
     * 支持处理 markdown 代码块包裹的 JSON
     */
    public static Map<String, Object> parseJson(String response) {
        if (response == null || response.isBlank()) {
            return Map.of();
        }
        
        String trimmed = response.trim();
        
        // 处理 markdown 代码块
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        }
        
        try {
            return objectMapper.readValue(trimmed, Map.class);
        } catch (Exception e) {
            log.warn("解析AI JSON失败: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 从 AI 响应中提取 level 和 reason
     * 用于专注度判断、补签判断等场景
     */
    public static AiJudgmentResult parseJudgment(String response) {
        Map<String, Object> json = parseJson(response);
        
        int level = 0;
        boolean allow = false;
        String reason = "";
        
        if (json.containsKey("level")) {
            level = ((Number) json.get("level")).intValue();
        }
        if (json.containsKey("allow")) {
            allow = (Boolean) json.get("allow");
        }
        if (json.containsKey("reason")) {
            reason = (String) json.get("reason");
        }
        
        return new AiJudgmentResult(level, allow, reason);
    }

    /**
     * AI 判断结果
     */
    public static class AiJudgmentResult {
        private final int level;
        private final boolean allow;
        private final String reason;

        public AiJudgmentResult(int level, boolean allow, String reason) {
            this.level = level;
            this.allow = allow;
            this.reason = reason;
        }

        public int getLevel() { return level; }
        public boolean isAllow() { return allow; }
        public String getReason() { return reason; }
    }
}
