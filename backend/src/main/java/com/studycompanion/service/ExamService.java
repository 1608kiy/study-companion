package com.studycompanion.service;

import com.studycompanion.dto.ExamRequest;
import com.studycompanion.vo.ExamVO;

import java.util.List;

public interface ExamService {
    List<ExamVO> getExams(Long userId);
    ExamVO createExam(Long userId, ExamRequest request);
    ExamVO updateExam(Long userId, Long examId, ExamRequest request);
    void deleteExam(Long userId, Long examId);
}
