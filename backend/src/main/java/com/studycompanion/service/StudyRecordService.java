package com.studycompanion.service;

import com.studycompanion.dto.StartTimerRequest;
import com.studycompanion.dto.UpdateStudyRecordRequest;
import com.studycompanion.vo.PageResponse;
import com.studycompanion.vo.StudyRecordVO;
import com.studycompanion.vo.StudyStatsVO;
import com.studycompanion.vo.TimerStateVO;

import java.util.List;

public interface StudyRecordService {

    TimerStateVO startTimer(Long userId, StartTimerRequest request);

    TimerStateVO pauseTimer(Long userId);

    TimerStateVO resumeTimer(Long userId);

    StudyRecordVO stopTimer(Long userId);

    TimerStateVO getTimerState(Long userId);

    List<StudyRecordVO> getStudyRecords(Long userId, String startDate, String endDate);

    PageResponse<StudyRecordVO> getStudyRecordsPaged(Long userId, String startDate, String endDate, int page, int size);

    StudyRecordVO getStudyRecordById(Long userId, Long recordId);

    StudyRecordVO updateStudyRecord(Long userId, Long recordId, UpdateStudyRecordRequest request);

    void deleteStudyRecord(Long userId, Long recordId);

    StudyStatsVO getStudyStats(Long userId);
}
