package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckInStatusVO {

    private LocalDate checkDate;
    private Boolean isCompleted;
    private Integer totalDuration;
    private Integer streak;
    private Boolean canCheckIn;
}
