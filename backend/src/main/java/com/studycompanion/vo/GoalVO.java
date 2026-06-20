package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GoalVO {

    private Long id;
    private Integer goalType;
    private String goalTypeName;
    private Integer goalValue;
    private Integer currentValue;
    private LocalDate goalDate;
    private Boolean isCompleted;
    private Boolean aiSuggested;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
