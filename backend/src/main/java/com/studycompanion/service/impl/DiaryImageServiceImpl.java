package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.entity.Diary;
import com.studycompanion.entity.DiaryImage;
import com.studycompanion.mapper.DiaryImageMapper;
import com.studycompanion.mapper.DiaryMapper;
import com.studycompanion.service.DiaryImageService;
import com.studycompanion.vo.DiaryImageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryImageServiceImpl implements DiaryImageService {

    private final DiaryImageMapper diaryImageMapper;
    private final DiaryMapper diaryMapper;

    @Value("${upload.path}")
    private String uploadPath;

    private static final int MAX_IMAGE_COUNT = 9;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    @Transactional
    public DiaryImageVO uploadImage(Long userId, Long diaryId, MultipartFile file) {
        // 验证日记是否存在
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_NOT_FOUND);
        }

        // 检查图片数量限制
        LambdaQueryWrapper<DiaryImage> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(DiaryImage::getDiaryId, diaryId);
        if (diaryImageMapper.selectCount(countWrapper) >= MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.DIARY_IMAGE_LIMIT_EXCEEDED);
        }

        // 检查文件大小
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.DIARY_IMAGE_SIZE_EXCEEDED);
        }

        // 保存文件
        String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        Path filePath = Paths.get(uploadPath, "diary", diaryId.toString(), fileName);

        try {
            Files.createDirectories(filePath.getParent());
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        // 保存图片记录
        DiaryImage image = new DiaryImage();
        image.setDiaryId(diaryId);
        image.setImageUrl("/uploads/diary/" + diaryId + "/" + fileName);
        image.setImageSize((int) (file.getSize() / 1024));
        image.setSortOrder(getNextSortOrder(diaryId));

        diaryImageMapper.insert(image);
        return convertToVO(image);
    }

    @Override
    @Transactional
    public void deleteImage(Long userId, Long imageId) {
        DiaryImage image = diaryImageMapper.selectById(imageId);
        if (image == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 验证日记所有权
        Diary diary = diaryMapper.selectById(image.getDiaryId());
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 删除文件
        Path filePath = Paths.get(uploadPath, "diary", image.getDiaryId().toString(), 
                Paths.get(image.getImageUrl()).getFileName().toString());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("文件删除失败", e);
        }

        diaryImageMapper.deleteById(imageId);
    }

    @Override
    public List<DiaryImageVO> getDiaryImages(Long userId, Long diaryId) {
        LambdaQueryWrapper<DiaryImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiaryImage::getDiaryId, diaryId)
               .orderByAsc(DiaryImage::getSortOrder);
        List<DiaryImage> images = diaryImageMapper.selectList(wrapper);
        return images.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private int getNextSortOrder(Long diaryId) {
        LambdaQueryWrapper<DiaryImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiaryImage::getDiaryId, diaryId)
               .orderByDesc(DiaryImage::getSortOrder)
               .last("LIMIT 1");
        DiaryImage lastImage = diaryImageMapper.selectOne(wrapper);
        return lastImage != null ? lastImage.getSortOrder() + 1 : 1;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return ".jpg";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private DiaryImageVO convertToVO(DiaryImage image) {
        DiaryImageVO vo = new DiaryImageVO();
        vo.setId(image.getId());
        vo.setImageUrl(image.getImageUrl());
        vo.setImageSize(image.getImageSize());
        vo.setSortOrder(image.getSortOrder());
        return vo;
    }
}
