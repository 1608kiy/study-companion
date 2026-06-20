package com.studycompanion.service;

import com.studycompanion.entity.Goal;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.GoalMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.dto.CreateGoalRequest;
import com.studycompanion.service.impl.GoalServiceImpl;
import com.studycompanion.vo.GoalVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private StudyRecordMapper studyRecordMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private GoalServiceImpl goalService;

    private Goal testGoal;

    @BeforeEach
    void setUp() {
        testGoal = new Goal();
        testGoal.setId(1L);
        testGoal.setUserId(1L);
        testGoal.setGoalType(1);
        testGoal.setGoalValue(120);
        testGoal.setCurrentValue(0);
        testGoal.setIsCompleted(0);
        testGoal.setAiSuggested(0);
    }

    @Test
    void createGoal_Success() {
        CreateGoalRequest request = new CreateGoalRequest();
        request.setGoalType(1);
        request.setGoalValue(120);

        when(goalMapper.insert(any(Goal.class))).thenReturn(1);

        GoalVO result = goalService.createGoal(1L, request);

        assertNotNull(result);
        assertEquals(1, result.getGoalType());
        assertEquals(120, result.getGoalValue());
        assertFalse(result.getIsCompleted());
        verify(goalMapper, times(1)).insert(any(Goal.class));
    }

    @Test
    void getGoalList_Success() {
        when(goalMapper.selectList(any())).thenReturn(Arrays.asList(testGoal));

        List<GoalVO> goals = goalService.getGoalList(1L, null);

        assertNotNull(goals);
        assertEquals(1, goals.size());
        assertEquals("每日目标", goals.get(0).getGoalTypeName());
    }

    @Test
    void updateGoal_Success() {
        when(goalMapper.selectById(1L)).thenReturn(testGoal);
        when(goalMapper.updateById(any())).thenReturn(1);

        var request = new com.studycompanion.dto.UpdateGoalRequest();
        request.setGoalValue(180);

        GoalVO result = goalService.updateGoal(1L, 1L, request);

        assertNotNull(result);
        assertEquals(180, result.getGoalValue());
    }

    @Test
    void updateGoal_NotFound_ThrowsException() {
        when(goalMapper.selectById(999L)).thenReturn(null);

        var request = new com.studycompanion.dto.UpdateGoalRequest();
        request.setGoalValue(180);

        assertThrows(BusinessException.class, () -> {
            goalService.updateGoal(1L, 999L, request);
        });
    }

    @Test
    void deleteGoal_Success() {
        when(goalMapper.selectById(1L)).thenReturn(testGoal);
        when(goalMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> {
            goalService.deleteGoal(1L, 1L);
        });
        verify(goalMapper, times(1)).deleteById(1L);
    }

    @Test
    void deleteGoal_NotFound_ThrowsException() {
        when(goalMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> {
            goalService.deleteGoal(1L, 999L);
        });
    }

    @Test
    void aiSuggestGoal_DailyGoal() {
        when(goalMapper.insert(any(Goal.class))).thenReturn(1);

        GoalVO result = goalService.aiSuggestGoal(1L, 1);

        assertNotNull(result);
        assertEquals(1, result.getGoalType());
        assertEquals(120, result.getGoalValue());
        assertTrue(result.getAiSuggested());
    }

    @Test
    void aiSuggestGoal_WeeklyGoal() {
        when(goalMapper.insert(any(Goal.class))).thenReturn(1);

        GoalVO result = goalService.aiSuggestGoal(1L, 2);

        assertNotNull(result);
        assertEquals(2, result.getGoalType());
        assertEquals(840, result.getGoalValue());
    }
}
