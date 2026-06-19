package com.studycompanion;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.studycompanion.mapper")
public class StudyCompanionApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyCompanionApplication.class, args);
    }
}
