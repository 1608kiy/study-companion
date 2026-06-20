package com.studycompanion.dto;

import lombok.Data;

@Data
public class UpdateStudyRecordRequest {

    private String mood;

    private Integer focusLevel;

    private String remark;

    private Integer interruptionCount;

    private String interruptionReason;
}
