package com.studycompanion.service;

import com.studycompanion.dto.CreateDiaryRequest;
import com.studycompanion.dto.UpdateDiaryRequest;
import com.studycompanion.vo.DiaryVO;
import com.studycompanion.vo.PageResponse;

import java.util.List;

public interface DiaryService {

    List<DiaryVO> getDiaryList(Long userId, String month);

    PageResponse<DiaryVO> getDiaryListPaged(Long userId, String month, int page, int size);

    DiaryVO getDiaryByDate(Long userId, String date);

    DiaryVO createDiary(Long userId, CreateDiaryRequest request);

    DiaryVO updateDiary(Long userId, Long diaryId, UpdateDiaryRequest request);

    void deleteDiary(Long userId, Long diaryId);

    DiaryVO generateDiary(Long userId);

    DiaryVO regenerateDiary(Long userId, Long diaryId);
}
