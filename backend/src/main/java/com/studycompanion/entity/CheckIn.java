package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 打卡表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_in")
public class CheckIn extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 打卡日期
     */
    @TableField("check_date")
    private LocalDate checkDate;

    /**
     * 当天总学习时长(分钟)
     */
    @TableField("total_duration")
    private Integer totalDuration;

    /**
     * 是否完成打卡(0未完成 1完成)
     */
    @TableField("is_completed")
    private Integer isCompleted;

    /**
     * 当前连续打卡天数
     */
    @TableField("streak")
    private Integer streak;
}
