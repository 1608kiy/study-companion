package com.studycompanion.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.dto.CreateSubjectRequest;
import com.studycompanion.dto.UpdateSubjectRequest;
import com.studycompanion.service.impl.SubjectServiceImpl;
import com.studycompanion.vo.SubjectVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private Subject testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setUserId(1L);
        testSubject.setName("行测");
        testSubject.setColor("#409EFF");
        testSubject.setIcon("icon-xingce");
        testSubject.setSortOrder(1);
        testSubject.setIsPreset(0);
    }

    @Test
    void getSubjectList_Success() {
        when(subjectMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testSubject));

        List<SubjectVO> subjects = subjectService.getSubjectList(1L);

        assertNotNull(subjects);
        assertEquals(1, subjects.size());
        assertEquals("行测", subjects.get(0).getName());
    }

    @Test
    void createSubject_Success() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("申论");
        request.setColor("#67C23A");

        when(subjectMapper.selectCount(any())).thenReturn(0L);
        when(subjectMapper.insert(any(Subject.class))).thenReturn(1);

        SubjectVO result = subjectService.createSubject(1L, request);

        assertNotNull(result);
        assertEquals("申论", result.getName());
        verify(subjectMapper, times(1)).insert(any(Subject.class));
    }

    @Test
    void createSubject_NameAlreadyExists_ThrowsException() {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("行测");

        when(subjectMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessException.class, () -> {
            subjectService.createSubject(1L, request);
        });
    }

    @Test
    void updateSubject_Success() {
        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("行测-修改");

        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(subjectMapper.selectCount(any())).thenReturn(0L);
        when(subjectMapper.updateById(any())).thenReturn(1);

        SubjectVO result = subjectService.updateSubject(1L, 1L, request);

        assertNotNull(result);
        assertEquals("行测-修改", result.getName());
    }

    @Test
    void updateSubject_PresetSubject_CanUpdate() {
        testSubject.setIsPreset(1);
        UpdateSubjectRequest request = new UpdateSubjectRequest();
        request.setName("修改预设");

        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(subjectMapper.selectCount(any())).thenReturn(0L);
        when(subjectMapper.updateById(any())).thenReturn(1);

        // 预设科目也可以更新名称/颜色等
        SubjectVO result = subjectService.updateSubject(1L, 1L, request);

        assertNotNull(result);
        assertEquals("修改预设", result.getName());
    }

    @Test
    void deleteSubject_Success() {
        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(subjectMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            subjectService.deleteSubject(1L, 1L);
        });
        verify(subjectMapper, times(1)).deleteById(1L);
    }

    @Test
    void deleteSubject_PresetSubject_ThrowsException() {
        testSubject.setIsPreset(1);
        when(subjectMapper.selectById(1L)).thenReturn(testSubject);

        assertThrows(BusinessException.class, () -> {
            subjectService.deleteSubject(1L, 1L);
        });
    }

    @Test
    void deleteSubject_SubjectNotFound_ThrowsException() {
        when(subjectMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            subjectService.deleteSubject(1L, 999L);
        });
    }
}
