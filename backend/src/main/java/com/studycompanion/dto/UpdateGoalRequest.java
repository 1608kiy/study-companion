package com.studycompanion.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateGoalRequest {

    private Integer goalType;

    @Min(value = 1, message = "目标值必须大于0")
    private Integer goalValue;

    private LocalDate goalDate;

    private Integer isCompleted;
}
