package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_material")
public class LearningMaterial extends BaseEntity {
    private Long userId;
    private Long subjectId;
    private String title;
    private String description;
    private String fileUrl;
    private String fileName;
    private Long fileSize; // bytes
    private String fileType; // pdf, doc, image, etc.
    private String tags;
    private Integer isFavorite;
}
