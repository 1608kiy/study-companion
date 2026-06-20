package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiaryVO {

    private Long id;
    private LocalDate diaryDate;
    private String content;
    private String summary;
    private String plan;
    private String reflection;
    private String problems;
    private Boolean aiGenerated;
    private Integer aiGenerateCount;
    private List<DiaryImageVO> images;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
