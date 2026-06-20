package com.studycompanion.dto;

import lombok.Data;

@Data
public class UpdateDiaryRequest {

    private String content;

    private String summary;

    private String plan;

    private String reflection;

    private String problems;
}
