package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.MaterialRequest;
import com.studycompanion.entity.LearningMaterial;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.LearningMaterialMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.MaterialService;
import com.studycompanion.vo.MaterialVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final LearningMaterialMapper materialMapper;
    private final SubjectMapper subjectMapper;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "jpg", "jpeg", "png", "gif"
    );

    @Override
    public List<MaterialVO> getMaterials(Long userId, Long subjectId, String keyword) {
        LambdaQueryWrapper<LearningMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningMaterial::getUserId, userId);
        
        if (subjectId != null) {
            wrapper.eq(LearningMaterial::getSubjectId, subjectId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(LearningMaterial::getTitle, keyword)
                    .or().like(LearningMaterial::getTags, keyword));
        }
        wrapper.orderByDesc(LearningMaterial::getCreateTime);
        
        List<LearningMaterial> materials = materialMapper.selectList(wrapper);
        
        // 批量加载科目，避免 N+1 查询
        Set<Long> subjectIds = materials.stream()
                .map(LearningMaterial::getSubjectId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = new HashMap<>();
        if (!subjectIds.isEmpty()) {
            List<Subject> subjects = subjectMapper.selectBatchIds(subjectIds);
            subjectMap = subjects.stream().collect(Collectors.toMap(Subject::getId, s -> s));
        }
        
        Map<Long, Subject> finalSubjectMap = subjectMap;
        return materials.stream()
                .map(m -> convertToVO(m, finalSubjectMap))
                .toList();
    }

    @Override
    public MaterialVO getMaterialById(Long userId, Long materialId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return convertToVO(material);
    }

    @Override
    public MaterialVO uploadMaterial(Long userId, MaterialRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 检查文件类型
        if (!ALLOWED_TYPES.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 生成存储路径
        String fileName = UUID.randomUUID() + "." + extension;
        String relativePath = "materials/" + userId + "/" + fileName;
        String fullPath = uploadPath + relativePath;

        // 保存文件
        try {
            File dest = new File(fullPath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        // 保存记录
        LearningMaterial material = new LearningMaterial();
        material.setUserId(userId);
        material.setSubjectId(request.getSubjectId());
        material.setTitle(request.getTitle() != null ? request.getTitle() : originalFilename);
        material.setDescription(request.getDescription());
        material.setFileUrl("/uploads/" + relativePath);
        material.setFileName(originalFilename);
        material.setFileSize(file.getSize());
        material.setFileType(extension);
        material.setTags(request.getTags());
        material.setIsFavorite(Boolean.TRUE.equals(request.getIsFavorite()) ? 1 : 0);
        materialMapper.insert(material);

        return convertToVO(material);
    }

    @Override
    public MaterialVO updateMaterial(Long userId, Long materialId, MaterialRequest request) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (request.getSubjectId() != null) {
            material.setSubjectId(request.getSubjectId());
        }
        if (request.getTitle() != null) {
            material.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            material.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            material.setTags(request.getTags());
        }
        if (request.getIsFavorite() != null) {
            material.setIsFavorite(Boolean.TRUE.equals(request.getIsFavorite()) ? 1 : 0);
        }
        materialMapper.updateById(material);
        return convertToVO(material);
    }

    @Override
    public void deleteMaterial(Long userId, Long materialId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        // 删除文件
        String filePath = uploadPath + material.getFileUrl().replace("/uploads/", "");
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        
        materialMapper.deleteById(materialId);
    }

    @Override
    public MaterialVO toggleFavorite(Long userId, Long materialId) {
        LearningMaterial material = materialMapper.selectById(materialId);
        if (material == null || !material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        material.setIsFavorite(material.getIsFavorite() == 1 ? 0 : 1);
        materialMapper.updateById(material);
        return convertToVO(material);
    }

    private MaterialVO convertToVO(LearningMaterial material) {
        return convertToVO(material, new HashMap<>());
    }
    
    private MaterialVO convertToVO(LearningMaterial material, Map<Long, Subject> subjectMap) {
        MaterialVO vo = new MaterialVO();
        vo.setId(material.getId());
        vo.setSubjectId(material.getSubjectId());
        vo.setTitle(material.getTitle());
        vo.setDescription(material.getDescription());
        vo.setFileUrl(material.getFileUrl());
        vo.setFileName(material.getFileName());
        vo.setFileSize(material.getFileSize());
        vo.setFileType(material.getFileType());
        vo.setTags(material.getTags());
        vo.setIsFavorite(material.getIsFavorite() != null && material.getIsFavorite() == 1);
        vo.setCreateTime(material.getCreateTime());
        
        // 获取科目名称
        if (material.getSubjectId() != null) {
            Subject subject = subjectMap.get(material.getSubjectId());
            if (subject == null) {
                subject = subjectMapper.selectById(material.getSubjectId());
            }
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }
        }
        
        return vo;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
