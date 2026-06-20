package com.studycompanion.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    EMAIL_INVALID(1003, "邮箱格式不正确"),
    PASSWORD_ERROR(1004, "密码错误"),
    PASSWORD_TOO_SHORT(1005, "密码长度不能少于6位"),

    // 科目模块 2xxx
    SUBJECT_NOT_FOUND(2001, "科目不存在"),
    SUBJECT_ALREADY_EXISTS(2002, "科目名称已存在"),
    SUBJECT_LIMIT_EXCEEDED(2003, "科目数量超限"),

    // 学习记录模块 3xxx
    RECORD_NOT_FOUND(3001, "学习记录不存在"),
    RECORD_MODIFY_NOT_ALLOWED(3002, "AI判断不允许修改"),
    RECORD_DELETE_NOT_ALLOWED(3003, "AI判断不允许删除"),
    TIMER_ALREADY_RUNNING(3004, "计时器已在运行中"),
    TIMER_NOT_RUNNING(3005, "计时器未运行"),
    TIMER_NOT_PAUSED(3006, "计时器未暂停"),

    // 打卡模块 4xxx
    CHECKIN_ALREADY_COMPLETED(4001, "今日已打卡"),
    CHECKIN_NOT_COMPLETED(4002, "打卡条件未满足"),
    DIARY_NOT_WRITTEN(4003, "请先写日记"),

    // 日记模块 5xxx
    DIARY_NOT_FOUND(5001, "日记不存在"),
    DIARY_ALREADY_EXISTS(5002, "今日日记已存在"),
    DIARY_MODIFY_NOT_ALLOWED(5003, "日记提交后不可修改"),
    DIARY_IMMUTABLE(5004, "日记不可编辑删除，保证数据真实性"),
    DIARY_AI_COUNT_EXCEEDED(5005, "AI生成次数已达上限"),
    DIARY_IMAGE_LIMIT_EXCEEDED(5006, "图片数量超限"),
    DIARY_IMAGE_SIZE_EXCEEDED(5007, "图片大小超限"),

    // 目标模块 6xxx
    GOAL_NOT_FOUND(6001, "目标不存在"),

    // 断签模块 7xxx
    MISS_RECORD_NOT_FOUND(7001, "断签记录不存在"),
    REPLENISH_NOT_ALLOWED(7002, "AI判断不允许补签"),
    REPLENISH_ALREADY_COMPLETED(7003, "已补签"),

    // AI模块 8xxx
    AI_SERVICE_ERROR(8001, "AI服务异常"),
    AI_REQUEST_FAILED(8002, "AI请求失败");

    private final Integer code;
    private final String message;
}
