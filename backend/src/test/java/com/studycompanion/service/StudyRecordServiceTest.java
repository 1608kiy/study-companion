package com.studycompanion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.dto.StartTimerRequest;
import com.studycompanion.dto.UpdateStudyRecordRequest;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.CheckInMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.impl.StudyRecordServiceImpl;
import com.studycompanion.vo.StudyRecordVO;
import com.studycompanion.vo.StudyStatsVO;
import com.studycompanion.vo.TimerStateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyRecordServiceTest {

    @Mock
    private StudyRecordMapper studyRecordMapper;
    
    @Mock
    private SubjectMapper subjectMapper;
    
    @Mock
    private CheckInMapper checkInMapper;
    
    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @InjectMocks
    private StudyRecordServiceImpl studyRecordService;

    private Long userId = 1L;
    private Long subjectId = 1L;
    private Long recordId = 1L;
    private Subject testSubject;
    private StudyRecord testRecord;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setId(subjectId);
        testSubject.setUserId(userId);
        testSubject.setName("行测");
        
        testRecord = new StudyRecord();
        testRecord.setId(recordId);
        testRecord.setUserId(userId);
        testRecord.setSubjectId(subjectId);
        testRecord.setStudyDate(LocalDate.now());
        testRecord.setStartTime(LocalDateTime.now().minusHours(1));
        testRecord.setEndTime(LocalDateTime.now());
        testRecord.setDuration(60);
    }

    @Test
    void getStudyRecordById_Success() {
        when(studyRecordMapper.selectById(recordId)).thenReturn(testRecord);
        when(subjectMapper.selectById(subjectId)).thenReturn(testSubject);
        
        StudyRecordVO result = studyRecordService.getStudyRecordById(userId, recordId);
        
        assertNotNull(result);
        assertEquals(recordId, result.getId());
        assertEquals(subjectId, result.getSubjectId());
        assertEquals("行测", result.getSubjectName());
    }

    @Test
    void getStudyRecordById_NotFound_ThrowsException() {
        when(studyRecordMapper.selectById(recordId)).thenReturn(null);
        
        assertThrows(BusinessException.class, 
            () -> studyRecordService.getStudyRecordById(userId, recordId));
    }

    @Test
    void getStudyRecordById_WrongUser_ThrowsException() {
        testRecord.setUserId(999L);
        when(studyRecordMapper.selectById(recordId)).thenReturn(testRecord);
        
        assertThrows(BusinessException.class, 
            () -> studyRecordService.getStudyRecordById(userId, recordId));
    }

    @Test
    void updateStudyRecord_Success() {
        when(studyRecordMapper.selectById(recordId)).thenReturn(testRecord);
        when(subjectMapper.selectById(subjectId)).thenReturn(testSubject);
        
        UpdateStudyRecordRequest request = new UpdateStudyRecordRequest();
        request.setMood("开心");
        request.setFocusLevel(4);
        request.setRemark("今天状态不错");
        
        StudyRecordVO result = studyRecordService.updateStudyRecord(userId, recordId, request);
        
        assertNotNull(result);
        assertEquals("开心", result.getMood());
        assertEquals(4, result.getFocusLevel());
        assertEquals("今天状态不错", result.getRemark());
        verify(studyRecordMapper).updateById(any(StudyRecord.class));
    }

    @Test
    void deleteStudyRecord_Success() {
        when(studyRecordMapper.selectById(recordId)).thenReturn(testRecord);
        
        studyRecordService.deleteStudyRecord(userId, recordId);
        
        verify(studyRecordMapper).deleteById(recordId);
    }

    @Test
    void deleteStudyRecord_NotFound_ThrowsException() {
        when(studyRecordMapper.selectById(recordId)).thenReturn(null);
        
        assertThrows(BusinessException.class, 
            () -> studyRecordService.deleteStudyRecord(userId, recordId));
    }

    @Test
    void getStudyRecords_WithDateRange() {
        when(studyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(testRecord));
        when(subjectMapper.selectBatchIds(anyCollection()))
            .thenReturn(Arrays.asList(testSubject));
        
        String startDate = LocalDate.now().toString();
        String endDate = LocalDate.now().toString();
        
        List<StudyRecordVO> result = studyRecordService.getStudyRecords(userId, startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("行测", result.get(0).getSubjectName());
    }

    @Test
    void getStudyStats_Success() {
        // Mock SQL aggregation methods
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("totalDays", 1L);
        aggregated.put("totalDuration", 60);
        when(studyRecordMapper.getStudyStatsAggregated(userId)).thenReturn(aggregated);
        when(studyRecordMapper.getDurationBetween(eq(userId), any(), any())).thenReturn(60);
        when(studyRecordMapper.getSubjectStats(userId)).thenReturn(Arrays.asList(
            Map.of("subjectName", "行测", "totalDuration", 60)
        ));
        when(studyRecordMapper.getDailyDurationsBetween(eq(userId), any(), any())).thenReturn(Collections.emptyList());
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        StudyStatsVO result = studyRecordService.getStudyStats(userId);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalDays());
        assertEquals(60, result.getTotalDuration());
        assertNotNull(result.getSubjectStats());
        assertEquals(60, result.getSubjectStats().get("行测"));
        assertNotNull(result.getWeeklyDurations());
        assertEquals(7, result.getWeeklyDurations().size());
    }

    @Test
    void getTimerState_NotRunning() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("timer:" + userId + ":running")).thenReturn("false");
        when(valueOperations.get("timer:" + userId + ":paused")).thenReturn("false");
        
        TimerStateVO result = studyRecordService.getTimerState(userId);
        
        assertNotNull(result);
        assertFalse(result.getIsRunning());
    }

    @Test
    void getStudyRecords_EmptyResult() {
        when(studyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.emptyList());
        
        List<StudyRecordVO> result = studyRecordService.getStudyRecords(userId, null, null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStudyStats_EmptyRecords() {
        // Mock SQL aggregation methods for empty result
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("totalDays", 0L);
        aggregated.put("totalDuration", 0);
        when(studyRecordMapper.getStudyStatsAggregated(userId)).thenReturn(aggregated);
        when(studyRecordMapper.getDurationBetween(eq(userId), any(), any())).thenReturn(0);
        when(studyRecordMapper.getSubjectStats(userId)).thenReturn(Collections.emptyList());
        when(studyRecordMapper.getDailyDurationsBetween(eq(userId), any(), any())).thenReturn(Collections.emptyList());
        when(checkInMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        
        StudyStatsVO result = studyRecordService.getStudyStats(userId);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalDays());
        assertEquals(0, result.getTotalDuration());
        assertEquals(0, result.getTodayDuration());
    }
}
