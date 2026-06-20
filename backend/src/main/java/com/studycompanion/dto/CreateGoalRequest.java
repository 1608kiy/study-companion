package com.studycompanion.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateGoalRequest {

    @NotNull(message = "目标类型不能为空")
    private Integer goalType;

    @NotNull(message = "目标值不能为空")
    @Min(value = 1, message = "目标值必须大于0")
    private Integer goalValue;

    private LocalDate goalDate;
}
