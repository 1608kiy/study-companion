package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TimerStateVO {

    private Boolean isRunning;
    private Long subjectId;
    private String subjectName;
    private LocalDateTime startTime;
    private Long elapsedSeconds;
}
