package com.studycompanion.service;

import com.studycompanion.dto.MaterialRequest;
import com.studycompanion.vo.MaterialVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialService {
    List<MaterialVO> getMaterials(Long userId, Long subjectId, String keyword);
    MaterialVO getMaterialById(Long userId, Long materialId);
    MaterialVO uploadMaterial(Long userId, MaterialRequest request, MultipartFile file);
    MaterialVO updateMaterial(Long userId, Long materialId, MaterialRequest request);
    void deleteMaterial(Long userId, Long materialId);
    MaterialVO toggleFavorite(Long userId, Long materialId);
}
