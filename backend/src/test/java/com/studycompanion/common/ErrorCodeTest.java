package com.studycompanion.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorCodeTest {

    @Test
    void errorCode_CodesAreUnique() {
        ErrorCode[] codes = ErrorCode.values();
        long uniqueCount = java.util.Arrays.stream(codes)
                .map(ErrorCode::getCode)
                .distinct()
                .count();
        assertEquals(codes.length, uniqueCount, "所有错误码必须唯一");
    }

    @Test
    void errorCode_UserModule_CodesInRange() {
        assertTrue(ErrorCode.USER_NOT_FOUND.getCode() >= 1000);
        assertTrue(ErrorCode.USER_NOT_FOUND.getCode() < 2000);
        assertTrue(ErrorCode.USER_ALREADY_EXISTS.getCode() >= 1000);
        assertTrue(ErrorCode.USER_ALREADY_EXISTS.getCode() < 2000);
    }

    @Test
    void errorCode_SubjectModule_CodesInRange() {
        assertTrue(ErrorCode.SUBJECT_NOT_FOUND.getCode() >= 2000);
        assertTrue(ErrorCode.SUBJECT_NOT_FOUND.getCode() < 3000);
        assertTrue(ErrorCode.SUBJECT_ALREADY_EXISTS.getCode() >= 2000);
        assertTrue(ErrorCode.SUBJECT_ALREADY_EXISTS.getCode() < 3000);
    }

    @Test
    void errorCode_RecordModule_CodesInRange() {
        assertTrue(ErrorCode.RECORD_NOT_FOUND.getCode() >= 3000);
        assertTrue(ErrorCode.RECORD_NOT_FOUND.getCode() < 4000);
    }

    @Test
    void errorCode_CheckInModule_CodesInRange() {
        assertTrue(ErrorCode.CHECKIN_ALREADY_COMPLETED.getCode() >= 4000);
        assertTrue(ErrorCode.CHECKIN_ALREADY_COMPLETED.getCode() < 5000);
    }

    @Test
    void errorCode_DiaryModule_CodesInRange() {
        assertTrue(ErrorCode.DIARY_NOT_FOUND.getCode() >= 5000);
        assertTrue(ErrorCode.DIARY_NOT_FOUND.getCode() < 6000);
    }

    @Test
    void errorCode_GoalModule_CodesInRange() {
        assertTrue(ErrorCode.GOAL_NOT_FOUND.getCode() >= 6000);
        assertTrue(ErrorCode.GOAL_NOT_FOUND.getCode() < 7000);
    }

    @Test
    void errorCode_MissRecordModule_CodesInRange() {
        assertTrue(ErrorCode.MISS_RECORD_NOT_FOUND.getCode() >= 7000);
        assertTrue(ErrorCode.MISS_RECORD_NOT_FOUND.getCode() < 8000);
    }

    @Test
    void errorCode_AiModule_CodesInRange() {
        assertTrue(ErrorCode.AI_SERVICE_ERROR.getCode() >= 8000);
        assertTrue(ErrorCode.AI_SERVICE_ERROR.getCode() < 9000);
    }

    @Test
    void errorCode_MessagesAreNotNull() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertNotNull(errorCode.getMessage(), "错误码 " + errorCode.name() + " 的消息不能为空");
            assertFalse(errorCode.getMessage().isEmpty(), "错误码 " + errorCode.name() + " 的消息不能为空字符串");
        }
    }
}
