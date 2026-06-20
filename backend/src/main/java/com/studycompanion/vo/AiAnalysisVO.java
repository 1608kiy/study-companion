package com.studycompanion.vo;

import lombok.Data;

@Data
public class AiAnalysisVO {

    private Long id;
    private String analysisType;
    private String content;
    private java.time.LocalDateTime createTime;
}
