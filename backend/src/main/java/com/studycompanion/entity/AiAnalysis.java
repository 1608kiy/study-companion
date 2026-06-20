package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI分析记录表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_analysis")
public class AiAnalysis extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 分析类型(weekly_report/monthly_report/advice/diary)
     */
    @TableField("analysis_type")
    private String analysisType;

    /**
     * 分析内容
     */
    @TableField("content")
    private String content;
}
