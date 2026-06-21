package com.studycompanion.dto;

import lombok.Data;

@Data
public class MaterialRequest {
    private Long subjectId;
    private String title;
    private String description;
    private String tags;
    private Boolean isFavorite;
}
