package com.studycompanion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/v1/user/**", "/api/v1/subjects/**",
                        "/api/v1/study-records/**", "/api/v1/check-in/**",
                        "/api/v1/goals/**", "/api/v1/diaries/**",
                        "/api/v1/ai/**")
                .excludePathPatterns("/api/v1/auth/**");
    }
}
