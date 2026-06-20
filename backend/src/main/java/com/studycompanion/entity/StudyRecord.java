package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习记录表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("study_record")
public class StudyRecord extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 科目ID
     */
    @TableField("subject_id")
    private Long subjectId;

    /**
     * 学习日期
     */
    @TableField("study_date")
    private LocalDate studyDate;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 学习时长(分钟)
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 备注(AI生成)
     */
    @TableField("remark")
    private String remark;

    /**
     * 心情(用户自定义)
     */
    @TableField("mood")
    private String mood;

    /**
     * 专注度(1-5 用户自评)
     */
    @TableField("focus_level")
    private Integer focusLevel;

    /**
     * AI判断的专注度(1-5)
     */
    @TableField("ai_focus_level")
    private Integer aiFocusLevel;

    /**
     * 中断次数
     */
    @TableField("interruption_count")
    private Integer interruptionCount;

    /**
     * 中断原因
     */
    @TableField("interruption_reason")
    private String interruptionReason;

    /**
     * AI是否允许修改 (0=不允许, 1=允许, null=未判断)
     */
    @TableField("ai_allow_modify")
    private Integer aiAllowModify;

    /**
     * AI修改判断原因
     */
    @TableField("ai_modify_reason")
    private String aiModifyReason;
}
