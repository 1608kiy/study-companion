package com.studycompanion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartTimerRequest {

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;
}
