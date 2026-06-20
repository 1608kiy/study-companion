package com.studycompanion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MissRecordRequest {

    @NotBlank(message = "断签原因不能为空")
    private String reason;
}
