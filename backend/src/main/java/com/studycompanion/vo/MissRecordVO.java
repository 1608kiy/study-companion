package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MissRecordVO {

    private Long id;
    private LocalDate missDate;
    private String reason;
    private Boolean aiAllowReplenish;
    private Boolean isReplenished;
}
