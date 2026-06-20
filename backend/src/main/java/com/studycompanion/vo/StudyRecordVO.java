package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyRecordVO {

    private Long id;
    private Long subjectId;
    private String subjectName;
    private LocalDate studyDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private String remark;
    private String mood;
    private Integer focusLevel;
    private Integer aiFocusLevel;
    private Integer interruptionCount;
    private String interruptionReason;
    private LocalDateTime createTime;
    private Boolean aiAllowModify;
    private String aiModifyReason;
}
