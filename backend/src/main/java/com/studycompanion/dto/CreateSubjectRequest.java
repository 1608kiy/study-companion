package com.studycompanion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSubjectRequest {

    @NotBlank(message = "科目名称不能为空")
    @Size(max = 50, message = "科目名称不能超过50个字符")
    private String name;

    @Size(max = 20, message = "颜色值不能超过20个字符")
    private String color;

    @Size(max = 50, message = "图标名称不能超过50个字符")
    private String icon;

    private Integer sortOrder;
}
