package com.studycompanion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDiaryRequest {

    @NotBlank(message = "日记内容不能为空")
    private String content;

    private String summary;

    private String plan;

    private String reflection;

    private String problems;
}
