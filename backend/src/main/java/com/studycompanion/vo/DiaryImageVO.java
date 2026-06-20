package com.studycompanion.vo;

import lombok.Data;

@Data
public class DiaryImageVO {

    private Long id;
    private String imageUrl;
    private Integer imageSize;
    private Integer sortOrder;
}
