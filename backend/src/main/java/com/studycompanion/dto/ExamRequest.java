package com.studycompanion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamRequest {
    @NotBlank(message = "考试名称不能为空")
    private String name;
    
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;
    
    private String location;
    private String description;
    private Boolean isTarget;
    private Integer sortOrder;
}
