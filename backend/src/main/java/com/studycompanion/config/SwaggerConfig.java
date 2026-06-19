package com.studycompanion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智学伴 - AI学习陪伴平台 API")
                        .description("基于Spring Boot 3 + MyBatis-Plus的后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StudyCompanion")
                                .email("admin@studycompanion.com")))
                .schemaRequirement("Authorization",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"));
    }
}
