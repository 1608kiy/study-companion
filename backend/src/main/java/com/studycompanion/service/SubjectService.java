package com.studycompanion.service;

import com.studycompanion.dto.CreateSubjectRequest;
import com.studycompanion.dto.UpdateSubjectRequest;
import com.studycompanion.vo.SubjectVO;

import java.util.List;

public interface SubjectService {

    List<SubjectVO> getSubjectList(Long userId);

    List<SubjectVO> getPresetSubjects(Long userId);

    SubjectVO createSubject(Long userId, CreateSubjectRequest request);

    SubjectVO updateSubject(Long userId, Long subjectId, UpdateSubjectRequest request);

    void deleteSubject(Long userId, Long subjectId);
}
