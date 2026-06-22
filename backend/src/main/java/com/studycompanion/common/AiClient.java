package com.studycompanion.common;

import com.studycompanion.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiClient {

    @Value("${ai.base-url}")
    private String defaultBaseUrl;

    @Value("${ai.api-key}")
    private String defaultApiKey;

    @Value("${ai.model}")
    private String defaultModel;

    @Value("${ai.enabled}")
    private boolean defaultEnabled;

    private final SystemConfigService systemConfigService;

    private final RestTemplate restTemplate;

    public AiClient(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
        // 配置超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);  // 连接超时 5 秒
        factory.setReadTimeout(60000);    // 读取超时 60 秒（AI 响应较慢）
        this.restTemplate = new RestTemplate(factory);
    }

    private String getBaseUrl() {
        return systemConfigService.getConfig("ai.base-url", defaultBaseUrl);
    }

    private String getApiKey() {
        String key = systemConfigService.getConfig("ai.api-key", "");
        return key != null && !key.isEmpty() ? key : defaultApiKey;
    }

    private String getModel() {
        return systemConfigService.getConfig("ai.model", defaultModel);
    }

    private boolean isEnabled() {
        String enabled = systemConfigService.getConfig("ai.enabled", String.valueOf(defaultEnabled));
        return "true".equalsIgnoreCase(enabled);
    }

    public boolean checkEnabled() {
        return isEnabled();
    }

    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of(Map.of("role", "user", "content", userMessage)));
    }

    public String chat(String systemPrompt, List<Map<String, String>> messages) {
        if (!isEnabled()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务未启用");
        }

        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty() || "your-api-key".equals(apiKey)) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI API Key未配置");
        }

        String url = getBaseUrl();
        url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> allMessages = new ArrayList<>();
        allMessages.add(Map.of("role", "system", "content", systemPrompt));
        allMessages.addAll(messages);

        Map<String, Object> body = Map.of(
                "model", getModel(),
                "messages", allMessages,
                "temperature", 0.7,
                "max_tokens", 2048
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("choices")) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI响应格式异常");
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI响应为空");
            }

            Map<String, Object> firstChoice = choices.get(0);
            if (firstChoice == null) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI响应选项为空");
            }
            
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            if (message == null) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI响应消息为空");
            }
            
            String content = (String) message.get("content");
            if (content == null) {
                throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI响应内容为空");
            }
            
            return content;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_REQUEST_FAILED, "AI服务请求失败: " + e.getMessage());
        }
    }
}
