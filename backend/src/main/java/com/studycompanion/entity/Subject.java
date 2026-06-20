package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 科目表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subject")
public class Subject extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 科目名称
     */
    @TableField("name")
    private String name;

    /**
     * 科目颜色
     */
    @TableField("color")
    private String color;

    /**
     * 科目图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否预设科目(0否 1是)
     */
    @TableField("is_preset")
    private Integer isPreset;
}
