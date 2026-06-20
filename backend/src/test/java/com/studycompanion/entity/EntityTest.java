package com.studycompanion.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void userEntity_SettersAndGetters() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setNickname("测试用户");
        user.setAvatar("avatar.jpg");
        user.setDailyGoal(120);
        user.setWeeklyGoal(840);
        user.setMonthlyGoal(3600);
        user.setDarkMode(0);
        user.setNotificationEnabled(1);

        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("测试用户", user.getNickname());
        assertEquals("avatar.jpg", user.getAvatar());
        assertEquals(120, user.getDailyGoal());
        assertEquals(840, user.getWeeklyGoal());
        assertEquals(3600, user.getMonthlyGoal());
        assertEquals(0, user.getDarkMode());
        assertEquals(1, user.getNotificationEnabled());
    }

    @Test
    void subjectEntity_SettersAndGetters() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setUserId(1L);
        subject.setName("行测");
        subject.setColor("#409EFF");
        subject.setIcon("icon-xingce");
        subject.setSortOrder(1);
        subject.setIsPreset(0);

        assertEquals(1L, subject.getId());
        assertEquals(1L, subject.getUserId());
        assertEquals("行测", subject.getName());
        assertEquals("#409EFF", subject.getColor());
        assertEquals("icon-xingce", subject.getIcon());
        assertEquals(1, subject.getSortOrder());
        assertEquals(0, subject.getIsPreset());
    }

    @Test
    void studyRecordEntity_SettersAndGetters() {
        StudyRecord record = new StudyRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setSubjectId(1L);
        record.setStudyDate(LocalDate.now());
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(LocalDateTime.now());
        record.setDuration(60);
        record.setMood("开心");
        record.setFocusLevel(4);

        assertEquals(1L, record.getId());
        assertEquals(1L, record.getUserId());
        assertEquals(1L, record.getSubjectId());
        assertEquals(LocalDate.now(), record.getStudyDate());
        assertEquals(60, record.getDuration());
        assertEquals("开心", record.getMood());
        assertEquals(4, record.getFocusLevel());
    }

    @Test
    void checkInEntity_SettersAndGetters() {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setUserId(1L);
        checkIn.setCheckDate(LocalDate.now());
        checkIn.setTotalDuration(120);
        checkIn.setIsCompleted(1);
        checkIn.setStreak(5);

        assertEquals(1L, checkIn.getId());
        assertEquals(1L, checkIn.getUserId());
        assertEquals(LocalDate.now(), checkIn.getCheckDate());
        assertEquals(120, checkIn.getTotalDuration());
        assertEquals(1, checkIn.getIsCompleted());
        assertEquals(5, checkIn.getStreak());
    }

    @Test
    void diaryEntity_SettersAndGetters() {
        Diary diary = new Diary();
        diary.setId(1L);
        diary.setUserId(1L);
        diary.setDiaryDate(LocalDate.now());
        diary.setContent("今天学习了...");
        diary.setSummary("学习了Java");
        diary.setAiGenerated(0);
        diary.setAiGenerateCount(0);

        assertEquals(1L, diary.getId());
        assertEquals(1L, diary.getUserId());
        assertEquals(LocalDate.now(), diary.getDiaryDate());
        assertEquals("今天学习了...", diary.getContent());
        assertEquals("学习了Java", diary.getSummary());
        assertEquals(0, diary.getAiGenerated());
        assertEquals(0, diary.getAiGenerateCount());
    }

    @Test
    void goalEntity_SettersAndGetters() {
        Goal goal = new Goal();
        goal.setId(1L);
        goal.setUserId(1L);
        goal.setGoalType(1);
        goal.setGoalValue(120);
        goal.setCurrentValue(60);
        goal.setGoalDate(LocalDate.now());
        goal.setIsCompleted(0);
        goal.setAiSuggested(0);

        assertEquals(1L, goal.getId());
        assertEquals(1L, goal.getUserId());
        assertEquals(1, goal.getGoalType());
        assertEquals(120, goal.getGoalValue());
        assertEquals(60, goal.getCurrentValue());
        assertEquals(LocalDate.now(), goal.getGoalDate());
        assertEquals(0, goal.getIsCompleted());
        assertEquals(0, goal.getAiSuggested());
    }

    @Test
    void missRecordEntity_SettersAndGetters() {
        MissRecord record = new MissRecord();
        record.setId(1L);
        record.setUserId(1L);
        record.setMissDate(LocalDate.now());
        record.setReason("生病");
        record.setAiAllowReplenish(1);
        record.setIsReplenished(0);

        assertEquals(1L, record.getId());
        assertEquals(1L, record.getUserId());
        assertEquals(LocalDate.now(), record.getMissDate());
        assertEquals("生病", record.getReason());
        assertEquals(1, record.getAiAllowReplenish());
        assertEquals(0, record.getIsReplenished());
    }

    @Test
    void userStatisticsEntity_SettersAndGetters() {
        UserStatistics stats = new UserStatistics();
        stats.setId(1L);
        stats.setUserId(1L);
        stats.setTotalDays(30);
        stats.setTotalDuration(3600);
        stats.setCurrentStreak(5);
        stats.setMaxStreak(10);
        stats.setLastCheckDate(LocalDate.now());

        assertEquals(1L, stats.getId());
        assertEquals(1L, stats.getUserId());
        assertEquals(30, stats.getTotalDays());
        assertEquals(3600, stats.getTotalDuration());
        assertEquals(5, stats.getCurrentStreak());
        assertEquals(10, stats.getMaxStreak());
        assertEquals(LocalDate.now(), stats.getLastCheckDate());
    }

    @Test
    void aiAnalysisEntity_SettersAndGetters() {
        AiAnalysis analysis = new AiAnalysis();
        analysis.setId(1L);
        analysis.setUserId(1L);
        analysis.setAnalysisType("weekly_report");
        analysis.setContent("本周学习报告...");

        assertEquals(1L, analysis.getId());
        assertEquals(1L, analysis.getUserId());
        assertEquals("weekly_report", analysis.getAnalysisType());
        assertEquals("本周学习报告...", analysis.getContent());
    }

    @Test
    void diaryImageEntity_SettersAndGetters() {
        DiaryImage image = new DiaryImage();
        image.setId(1L);
        image.setDiaryId(1L);
        image.setImageUrl("/uploads/diary/1/image.jpg");
        image.setImageSize(1024);
        image.setSortOrder(1);

        assertEquals(1L, image.getId());
        assertEquals(1L, image.getDiaryId());
        assertEquals("/uploads/diary/1/image.jpg", image.getImageUrl());
        assertEquals(1024, image.getImageSize());
        assertEquals(1, image.getSortOrder());
    }
}
