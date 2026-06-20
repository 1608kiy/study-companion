package com.studycompanion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.entity.CheckIn;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.UserStatistics;
import com.studycompanion.mapper.CheckInMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.UserStatisticsMapper;
import com.studycompanion.service.impl.CheckInServiceImpl;
import com.studycompanion.vo.CheckInStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInMapper checkInMapper;
    
    @Mock
    private StudyRecordMapper studyRecordMapper;
    
    @Mock
    private UserStatisticsMapper userStatisticsMapper;
    
    @InjectMocks
    private CheckInServiceImpl checkInService;

    private Long userId = 1L;
    private LocalDate today = LocalDate.now();

    @Test
    void getTodayCheckInStatus_NotCheckedIn() {
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(studyRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        
        CheckInStatusVO result = checkInService.getTodayCheckInStatus(userId);
        
        assertNotNull(result);
        assertFalse(result.getIsCompleted());
        assertTrue(result.getCanCheckIn());
        assertEquals(0, result.getStreak());
    }

    @Test
    void getTodayCheckInStatus_AlreadyCheckedIn() {
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        checkIn.setIsCompleted(1);
        checkIn.setTotalDuration(60);
        checkIn.setStreak(3);
        
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(checkIn);
        
        CheckInStatusVO result = checkInService.getTodayCheckInStatus(userId);
        
        assertNotNull(result);
        assertTrue(result.getIsCompleted());
        assertFalse(result.getCanCheckIn());
        assertEquals(60, result.getTotalDuration());
        assertEquals(3, result.getStreak());
    }

    @Test
    void getTodayCheckInStatus_NoStudyRecord() {
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(studyRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        
        CheckInStatusVO result = checkInService.getTodayCheckInStatus(userId);
        
        assertNotNull(result);
        assertFalse(result.getIsCompleted());
        assertFalse(result.getCanCheckIn());
    }

    @Test
    void checkIn_Success() {
        // Stub all selectOne calls to return null (no previous check-ins for streak)
        // Then return the created check-in for the final status check
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class)))
            .thenReturn(null)   // getCheckInStatus - no existing check-in
            .thenReturn(null)   // calculateStreak day-1 - no check-in
            .thenReturn(null)   // calculateStreak day-2 - no check-in (loop breaks)
            .thenReturn(createCheckIn()); // Final getCheckInStatus
        when(studyRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(studyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(createStudyRecord()));
        when(userStatisticsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        CheckInStatusVO result = checkInService.checkIn(userId);
        
        assertNotNull(result);
        verify(checkInMapper).insert(any(CheckIn.class));
        verify(userStatisticsMapper).insert(any(UserStatistics.class));
    }

    @Test
    void checkIn_AlreadyCheckedIn() {
        CheckIn existingCheckIn = new CheckIn();
        existingCheckIn.setId(1L);
        existingCheckIn.setUserId(userId);
        existingCheckIn.setCheckDate(today);
        existingCheckIn.setIsCompleted(1);
        existingCheckIn.setTotalDuration(60);
        existingCheckIn.setStreak(1);
        
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingCheckIn);
        
        CheckInStatusVO result = checkInService.checkIn(userId);
        
        assertNotNull(result);
        verify(checkInMapper, never()).insert(any(CheckIn.class));
    }

    @Test
    void getCheckInHistory_WithMonth() {
        CheckIn checkIn = createCheckIn();
        when(checkInMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(checkIn));
        
        String month = today.getYear() + "-" + String.format("%02d", today.getMonthValue());
        List<CheckInStatusVO> result = checkInService.getCheckInHistory(userId, month);
        
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCheckInHistory_Empty() {
        when(checkInMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());
        
        List<CheckInStatusVO> result = checkInService.getCheckInHistory(userId, null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private CheckIn createCheckIn() {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setUserId(userId);
        checkIn.setCheckDate(today);
        checkIn.setIsCompleted(1);
        checkIn.setTotalDuration(60);
        checkIn.setStreak(1);
        return checkIn;
    }

    private StudyRecord createStudyRecord() {
        StudyRecord record = new StudyRecord();
        record.setId(1L);
        record.setUserId(userId);
        record.setSubjectId(1L);
        record.setStudyDate(today);
        record.setDuration(60);
        return record;
    }
}
