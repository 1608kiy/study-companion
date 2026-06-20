package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.CreateSubjectRequest;
import com.studycompanion.dto.UpdateSubjectRequest;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.SubjectService;
import com.studycompanion.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;

    @Override
    public List<SubjectVO> getSubjectList(Long userId) {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subject::getUserId, userId)
               .orderByAsc(Subject::getSortOrder);
        List<Subject> subjects = subjectMapper.selectList(wrapper);
        return subjects.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubjectVO> getPresetSubjects(Long userId) {
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subject::getUserId, userId)
               .eq(Subject::getIsPreset, 1)
               .orderByAsc(Subject::getSortOrder);
        List<Subject> subjects = subjectMapper.selectList(wrapper);
        return subjects.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectVO createSubject(Long userId, CreateSubjectRequest request) {
        // 检查科目名称是否已存在
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subject::getUserId, userId)
               .eq(Subject::getName, request.getName());
        if (subjectMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.SUBJECT_ALREADY_EXISTS);
        }

        Subject subject = new Subject();
        subject.setUserId(userId);
        subject.setName(request.getName());
        subject.setColor(request.getColor() != null ? request.getColor() : "#409EFF");
        subject.setIcon(request.getIcon());
        subject.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        subject.setIsPreset(0);

        subjectMapper.insert(subject);
        return convertToVO(subject);
    }

    @Override
    public SubjectVO updateSubject(Long userId, Long subjectId, UpdateSubjectRequest request) {
        Subject subject = getSubjectById(userId, subjectId);

        if (request.getName() != null) {
            // 检查新名称是否已存在
            LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Subject::getUserId, userId)
                   .eq(Subject::getName, request.getName())
                   .ne(Subject::getId, subjectId);
            if (subjectMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.SUBJECT_ALREADY_EXISTS);
            }
            subject.setName(request.getName());
        }
        if (request.getColor() != null) {
            subject.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            subject.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            subject.setSortOrder(request.getSortOrder());
        }

        subjectMapper.updateById(subject);
        return convertToVO(subject);
    }

    @Override
    public void deleteSubject(Long userId, Long subjectId) {
        Subject subject = getSubjectById(userId, subjectId);

        // 预设科目不可删除
        if (subject.getIsPreset() == 1) {
            throw new BusinessException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        subjectMapper.deleteById(subjectId);
    }

    private Subject getSubjectById(Long userId, Long subjectId) {
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || !subject.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SUBJECT_NOT_FOUND);
        }
        return subject;
    }

    private SubjectVO convertToVO(Subject subject) {
        SubjectVO vo = new SubjectVO();
        vo.setId(subject.getId());
        vo.setName(subject.getName());
        vo.setColor(subject.getColor());
        vo.setIcon(subject.getIcon());
        vo.setSortOrder(subject.getSortOrder());
        vo.setIsPreset(subject.getIsPreset() == 1);
        vo.setCreateTime(subject.getCreateTime());
        vo.setUpdateTime(subject.getUpdateTime());
        return vo;
    }
}
