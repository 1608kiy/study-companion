package com.studycompanion.service;

import com.studycompanion.vo.DiaryImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiaryImageService {

    DiaryImageVO uploadImage(Long userId, Long diaryId, MultipartFile file);

    void deleteImage(Long userId, Long imageId);

    List<DiaryImageVO> getDiaryImages(Long userId, Long diaryId);
}
