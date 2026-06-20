package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectVO {

    private Long id;
    private String name;
    private String color;
    private String icon;
    private Integer sortOrder;
    private Boolean isPreset;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
