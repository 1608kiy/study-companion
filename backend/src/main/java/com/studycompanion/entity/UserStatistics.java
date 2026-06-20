package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户统计表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_statistics")
public class UserStatistics extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 累计学习天数
     */
    @TableField("total_days")
    private Integer totalDays;

    /**
     * 累计学习时长(分钟)
     */
    @TableField("total_duration")
    private Integer totalDuration;

    /**
     * 当前连续打卡天数
     */
    @TableField("current_streak")
    private Integer currentStreak;

    /**
     * 最长连续打卡天数
     */
    @TableField("max_streak")
    private Integer maxStreak;

    /**
     * 最后打卡日期
     */
    @TableField("last_check_date")
    private LocalDate lastCheckDate;
}
