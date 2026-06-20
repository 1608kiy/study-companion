package com.studycompanion.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息响应")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "每日目标时长(分钟)")
    private Integer dailyGoal;

    @Schema(description = "每周目标时长(分钟)")
    private Integer weeklyGoal;

    @Schema(description = "每月目标时长(分钟)")
    private Integer monthlyGoal;

    @Schema(description = "深色模式(false关闭 true开启)")
    private Boolean darkMode;

    @Schema(description = "通知开关(false关闭 true开启)")
    private Boolean notificationEnabled;
}
