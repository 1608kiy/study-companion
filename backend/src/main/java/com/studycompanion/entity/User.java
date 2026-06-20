package com.studycompanion.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码(BCrypt加密)
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 每日目标时长(分钟)
     */
    @TableField("daily_goal")
    private Integer dailyGoal;

    /**
     * 每周目标时长(分钟)
     */
    @TableField("weekly_goal")
    private Integer weeklyGoal;

    /**
     * 每月目标时长(分钟)
     */
    @TableField("monthly_goal")
    private Integer monthlyGoal;

    /**
     * 深色模式(0关闭 1开启)
     */
    @TableField("dark_mode")
    private Integer darkMode;

    /**
     * 通知开关(0关闭 1开启)
     */
    @TableField("notification_enabled")
    private Integer notificationEnabled;
}
