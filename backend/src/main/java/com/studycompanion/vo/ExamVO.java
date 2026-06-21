package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamVO {
    private Long id;
    private String name;
    private LocalDate examDate;
    private String location;
    private String description;
    private Boolean isTarget;
    private Integer sortOrder;
    private Long daysLeft; // 距离考试天数
}
