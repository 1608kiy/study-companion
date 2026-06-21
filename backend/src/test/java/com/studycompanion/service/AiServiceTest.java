package com.studycompanion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.BusinessException;
import com.studycompanion.dto.AiChatRequest;
import com.studycompanion.entity.AiAnalysis;
import com.studycompanion.entity.MissRecord;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.entity.User;
import com.studycompanion.mapper.AiAnalysisMapper;
import com.studycompanion.mapper.AiChatHistoryMapper;
import com.studycompanion.mapper.MissRecordMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.mapper.UserMapper;
import com.studycompanion.service.impl.AiServiceImpl;
import com.studycompanion.vo.AiAnalysisVO;
import com.studycompanion.vo.AiChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiAnalysisMapper aiAnalysisMapper;
    
    @Mock
    private StudyRecordMapper studyRecordMapper;
    
    @Mock
    private SubjectMapper subjectMapper;
    
    @Mock
    private MissRecordMapper missRecordMapper;
    
    @Mock
    private AiChatHistoryMapper aiChatHistoryMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private AiClient aiClient;
    
    @InjectMocks
    private AiServiceImpl aiService;

    private Long userId = 1L;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setNickname("测试用户");
    }

    @Test
    void chat_Success() {
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(aiClient.chat(anyString(), anyList())).thenReturn("AI回复内容");
        
        AiChatRequest request = new AiChatRequest();
        request.setQuestion("你好");
        
        AiChatResponse result = aiService.chat(userId, request);
        
        assertNotNull(result);
        assertEquals("AI回复内容", result.getAnswer());
    }

    @Test
    void chat_WithHistory() {
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(aiClient.chat(anyString(), anyList())).thenReturn("AI回复");
        
        AiChatRequest request = new AiChatRequest();
        request.setQuestion("继续对话");
        request.setHistory(List.of(
            Map.of("role", "user", "content", "之前的问题"),
            Map.of("role", "assistant", "content", "之前的回答")
        ));
        
        AiChatResponse result = aiService.chat(userId, request);
        
        assertNotNull(result);
        assertEquals("AI回复", result.getAnswer());
    }

    @Test
    void judgeFocusLevel_Success() {
        when(aiClient.chat(anyString(), anyString())).thenReturn("{\"level\":4,\"reason\":\"专注度较高\"}");
        
        var result = aiService.judgeFocusLevel(userId, "学习了2小时");
        
        assertNotNull(result);
        assertTrue(result.getContent().contains("4/5"));
    }

    @Test
    void judgeFocusLevel_InvalidJson_UsesRawResult() {
        when(aiClient.chat(anyString(), anyString())).thenReturn("这不是JSON格式");
        
        var result = aiService.judgeFocusLevel(userId, "学习了");
        
        assertNotNull(result);
        // AiResponseParser 返回空结果，格式化为默认文本
        assertTrue(result.getContent().contains("专注度评级"));
    }

    @Test
    void generateShareImage_Success() {
        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(studyRecordMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(createStudyRecord()));
        when(subjectMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Arrays.asList(createSubject()));
        
        var result = aiService.generateShareImage(userId);
        
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("测试用户"));
    }

    private StudyRecord createStudyRecord() {
        StudyRecord record = new StudyRecord();
        record.setId(1L);
        record.setUserId(userId);
        record.setSubjectId(1L);
        record.setStudyDate(LocalDate.now());
        record.setDuration(60);
        return record;
    }

    private Subject createSubject() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setUserId(userId);
        subject.setName("行测");
        return subject;
    }
}
