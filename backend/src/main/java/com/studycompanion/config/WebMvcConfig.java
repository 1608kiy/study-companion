package com.studycompanion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 普通用户接口
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/v1/user/**", "/api/v1/subjects/**",
                        "/api/v1/study-records/**", "/api/v1/check-in/**",
                        "/api/v1/goals/**", "/api/v1/diaries/**",
                        "/api/v1/ai/**", "/api/v1/exams/**",
                        "/api/v1/materials/**")
                .excludePathPatterns("/api/v1/auth/**");

        // 管理员接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/v1/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件的静态资源服务
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
