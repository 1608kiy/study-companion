package com.studycompanion.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialVO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private String title;
    private String description;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String tags;
    private Boolean isFavorite;
    private LocalDateTime createTime;
}
