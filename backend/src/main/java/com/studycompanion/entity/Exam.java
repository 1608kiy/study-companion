package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {
    private Long userId;
    private String name;
    private LocalDate examDate;
    private String location;
    private String description;
    private Integer isTarget; // 0=普通考试, 1=目标考试
    private Integer sortOrder;
}
