package com.studycompanion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest {

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

    @Schema(description = "深色模式(0关闭 1开启)")
    private Integer darkMode;

    @Schema(description = "通知开关(0关闭 1开启)")
    private Integer notificationEnabled;
}
