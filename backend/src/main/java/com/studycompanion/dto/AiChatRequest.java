package com.studycompanion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiChatRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    private List<Map<String, String>> history;
}
