package com.studycompanion.controller;

import com.studycompanion.common.Result;
import com.studycompanion.dto.ForgotPasswordRequest;
import com.studycompanion.dto.LoginRequest;
import com.studycompanion.dto.RegisterRequest;
import com.studycompanion.dto.ResetPasswordRequest;
import com.studycompanion.service.UserService;
import com.studycompanion.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证模块", description = "用户注册、登录、密码重置")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "忘记密码 - 获取验证码")
    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String code = userService.forgotPassword(request);
        // TODO: 生产环境应通过邮件发送验证码，而非直接返回
        return Result.success("验证码已发送（开发环境直接返回）", code);
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success("密码重置成功", null);
    }
}
