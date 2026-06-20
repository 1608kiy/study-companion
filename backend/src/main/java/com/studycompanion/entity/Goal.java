package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 目标表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goal")
public class Goal extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 目标类型(1每日 2每周 3每月)
     */
    @TableField("goal_type")
    private Integer goalType;

    /**
     * 目标值(分钟)
     */
    @TableField("goal_value")
    private Integer goalValue;

    /**
     * 当前值(分钟)
     */
    @TableField("current_value")
    private Integer currentValue;

    /**
     * 目标日期
     */
    @TableField("goal_date")
    private LocalDate goalDate;

    /**
     * 是否完成(0未完成 1完成)
     */
    @TableField("is_completed")
    private Integer isCompleted;

    /**
     * 是否AI推荐(0否 1是)
     */
    @TableField("ai_suggested")
    private Integer aiSuggested;
}
