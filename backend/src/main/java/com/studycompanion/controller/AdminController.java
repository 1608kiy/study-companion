package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员", description = "管理员专属功能")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController extends BaseController {

    private final AdminService adminService;

    public AdminController(JwtUtil jwtUtil, AdminService adminService) {
        super(jwtUtil);
        this.adminService = adminService;
    }

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = adminService.getUserList(page, size, keyword);
        return Result.success(result);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/users/{userId}")
    public Result<Void> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> updates) {
        adminService.updateUser(userId, updates);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取AI配置")
    @GetMapping("/ai/config")
    public Result<Map<String, String>> getAiConfig() {
        Map<String, String> config = adminService.getAiConfig();
        return Result.success(config);
    }

    @Operation(summary = "更新AI配置")
    @PutMapping("/ai/config")
    public Result<Void> updateAiConfig(@RequestBody Map<String, String> config) {
        adminService.updateAiConfig(config);
        return Result.success("更新成功", null);
    }
}
