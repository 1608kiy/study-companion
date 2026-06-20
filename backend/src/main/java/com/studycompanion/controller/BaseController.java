package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 控制器基类，提供通用方法
 */
@RequiredArgsConstructor
public abstract class BaseController {

    protected final JwtUtil jwtUtil;

    /**
     * 从请求中提取用户ID
     */
    protected Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
