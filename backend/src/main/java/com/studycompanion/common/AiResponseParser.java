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
        
        // 安全的类型转换
        Object levelObj = json.get("level");
        if (levelObj instanceof Number) {
            level = ((Number) levelObj).intValue();
        } else if (levelObj instanceof String) {
            try { level = Integer.parseInt((String) levelObj); } catch (NumberFormatException ignored) {}
        }
        
        Object allowObj = json.get("allow");
        if (allowObj instanceof Boolean) {
            allow = (Boolean) allowObj;
        } else if (allowObj instanceof String) {
            allow = "true".equalsIgnoreCase((String) allowObj);
        } else if (allowObj instanceof Number) {
            allow = ((Number) allowObj).intValue() != 0;
        }
        
        Object reasonObj = json.get("reason");
        if (reasonObj instanceof String) {
            reason = (String) reasonObj;
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
