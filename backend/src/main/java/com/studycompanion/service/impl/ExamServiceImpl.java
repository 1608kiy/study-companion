package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.ExamRequest;
import com.studycompanion.entity.Exam;
import com.studycompanion.mapper.ExamMapper;
import com.studycompanion.service.ExamService;
import com.studycompanion.vo.ExamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;

    @Override
    public List<ExamVO> getExams(Long userId) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exam::getUserId, userId)
               .orderByAsc(Exam::getExamDate);
        List<Exam> exams = examMapper.selectList(wrapper);
        
        return exams.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public ExamVO createExam(Long userId, ExamRequest request) {
        Exam exam = new Exam();
        exam.setUserId(userId);
        exam.setName(request.getName());
        exam.setExamDate(request.getExamDate());
        exam.setLocation(request.getLocation());
        exam.setDescription(request.getDescription());
        exam.setIsTarget(Boolean.TRUE.equals(request.getIsTarget()) ? 1 : 0);
        exam.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        examMapper.insert(exam);
        return convertToVO(exam);
    }

    @Override
    public ExamVO updateExam(Long userId, Long examId, ExamRequest request) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null || !exam.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        exam.setName(request.getName());
        exam.setExamDate(request.getExamDate());
        exam.setLocation(request.getLocation());
        exam.setDescription(request.getDescription());
        if (request.getIsTarget() != null) {
            exam.setIsTarget(Boolean.TRUE.equals(request.getIsTarget()) ? 1 : 0);
        }
        if (request.getSortOrder() != null) {
            exam.setSortOrder(request.getSortOrder());
        }
        examMapper.updateById(exam);
        return convertToVO(exam);
    }

    @Override
    public void deleteExam(Long userId, Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null || !exam.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        examMapper.deleteById(examId);
    }

    private ExamVO convertToVO(Exam exam) {
        ExamVO vo = new ExamVO();
        vo.setId(exam.getId());
        vo.setName(exam.getName());
        vo.setExamDate(exam.getExamDate());
        vo.setLocation(exam.getLocation());
        vo.setDescription(exam.getDescription());
        vo.setIsTarget(exam.getIsTarget() != null && exam.getIsTarget() == 1);
        vo.setSortOrder(exam.getSortOrder());
        
        // 计算距离考试天数
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), exam.getExamDate());
        vo.setDaysLeft(Math.max(0, daysLeft));
        
        return vo;
    }
}
