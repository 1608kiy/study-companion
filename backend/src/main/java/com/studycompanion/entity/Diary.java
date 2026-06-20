package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 日记表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("diary")
public class Diary extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 日记日期
     */
    @TableField("diary_date")
    private LocalDate diaryDate;

    /**
     * 日记内容(Markdown)
     */
    @TableField("content")
    private String content;

    /**
     * 今日总结
     */
    @TableField("summary")
    private String summary;

    /**
     * 明日计划
     */
    @TableField("plan")
    private String plan;

    /**
     * 心得体会
     */
    @TableField("reflection")
    private String reflection;

    /**
     * 问题记录
     */
    @TableField("problems")
    private String problems;

    /**
     * 是否AI生成(0否 1是)
     */
    @TableField("ai_generated")
    private Integer aiGenerated;

    /**
     * AI生成次数
     */
    @TableField("ai_generate_count")
    private Integer aiGenerateCount;
}
