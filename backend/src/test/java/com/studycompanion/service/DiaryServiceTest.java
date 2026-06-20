package com.studycompanion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.entity.Diary;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.DiaryMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.dto.CreateDiaryRequest;
import com.studycompanion.service.impl.DiaryServiceImpl;
import com.studycompanion.vo.DiaryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryMapper diaryMapper;

    @Mock
    private StudyRecordMapper studyRecordMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private DiaryImageService diaryImageService;

    @InjectMocks
    private DiaryServiceImpl diaryService;

    private Diary testDiary;

    @BeforeEach
    void setUp() {
        testDiary = new Diary();
        testDiary.setId(1L);
        testDiary.setUserId(1L);
        testDiary.setDiaryDate(LocalDate.now());
        testDiary.setContent("# 今日学习日记\n今天学习了Java...");
        testDiary.setAiGenerated(0);
        testDiary.setAiGenerateCount(0);
    }

    @Test
    void createDiary_Success() {
        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setContent("今天学习了Spring Boot框架...");

        when(diaryMapper.selectCount(any())).thenReturn(0L);
        when(diaryMapper.insert(any(Diary.class))).thenReturn(1);
        when(diaryImageService.getDiaryImages(any(), any())).thenReturn(Collections.emptyList());

        DiaryVO result = diaryService.createDiary(1L, request);

        assertNotNull(result);
        assertEquals("今天学习了Spring Boot框架...", result.getContent());
        verify(diaryMapper, times(1)).insert(any(Diary.class));
    }

    @Test
    void createDiary_AlreadyExists_ThrowsException() {
        CreateDiaryRequest request = new CreateDiaryRequest();
        request.setContent("今天学习了...");

        when(diaryMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> {
            diaryService.createDiary(1L, request);
        });
    }

    @Test
    void getDiaryByDate_Success() {
        when(diaryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testDiary);
        when(diaryImageService.getDiaryImages(any(), any())).thenReturn(Collections.emptyList());

        DiaryVO result = diaryService.getDiaryByDate(1L, LocalDate.now().toString());

        assertNotNull(result);
        assertEquals(testDiary.getContent(), result.getContent());
    }

    @Test
    void getDiaryByDate_NotFound_ReturnsNull() {
        when(diaryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        DiaryVO result = diaryService.getDiaryByDate(1L, LocalDate.now().toString());

        assertNull(result);
    }

    @Test
    void generateDiary_Success() {
        StudyRecord record = new StudyRecord();
        record.setSubjectId(1L);
        record.setDuration(60);

        Subject subject = new Subject();
        subject.setId(1L);
        subject.setName("行测");

        when(diaryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(subjectMapper.selectById(1L)).thenReturn(subject);
        when(studyRecordMapper.selectList(any())).thenReturn(Arrays.asList(record));
        when(diaryMapper.insert(any(Diary.class))).thenReturn(1);

        DiaryVO result = diaryService.generateDiary(1L);

        assertNotNull(result);
        assertTrue(result.getAiGenerated());
        assertEquals(1, result.getAiGenerateCount());
    }

    @Test
    void generateDiary_MaxCountExceeded_ThrowsException() {
        testDiary.setAiGenerateCount(3);
        when(diaryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testDiary);

        assertThrows(BusinessException.class, () -> {
            diaryService.generateDiary(1L);
        });
    }

    @Test
    void deleteDiary_Success() {
        when(diaryMapper.selectById(1L)).thenReturn(testDiary);
        when(diaryMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            diaryService.deleteDiary(1L, 1L);
        });
        verify(diaryMapper, times(1)).deleteById(1L);
    }

    @Test
    void deleteDiary_NotFound_ThrowsException() {
        when(diaryMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            diaryService.deleteDiary(1L, 999L);
        });
    }
}
