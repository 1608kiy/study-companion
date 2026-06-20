package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.UpdateProfileRequest;
import com.studycompanion.service.UserService;
import com.studycompanion.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块", description = "获取/更新用户信息、注销")
@RestController
@RequestMapping("/api/v1/user")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(JwtUtil jwtUtil, UserService userService) {
        super(jwtUtil);
        this.userService = userService;
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserProfileVO profile = userService.getProfile(userId);
        return Result.success(profile);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(HttpServletRequest request,
                                      @RequestBody UpdateProfileRequest profileRequest) {
        Long userId = getUserIdFromRequest(request);
        userService.updateProfile(userId, profileRequest);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "注销账号")
    @DeleteMapping("/delete")
    public Result<Void> deleteAccount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userService.deleteAccount(userId);
        return Result.success("注销成功", null);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        userService.logout(token);
        return Result.success("注销成功", null);
    }
}
