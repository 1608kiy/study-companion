package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记图片表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary_image")
public class DiaryImage extends BaseEntity {

    /**
     * 日记ID
     */
    @TableField("diary_id")
    private Long diaryId;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 图片大小(KB)
     */
    @TableField("image_size")
    private Integer imageSize;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
