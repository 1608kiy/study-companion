package com.studycompanion.controller;

import com.studycompanion.common.JwtUtil;
import com.studycompanion.common.Result;
import com.studycompanion.dto.MaterialRequest;
import com.studycompanion.service.MaterialService;
import com.studycompanion.vo.MaterialVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "学习资料", description = "学习资料管理")
@RestController
@RequestMapping("/api/v1/materials")
public class MaterialController extends BaseController {

    private final MaterialService materialService;

    public MaterialController(JwtUtil jwtUtil, MaterialService materialService) {
        super(jwtUtil);
        this.materialService = materialService;
    }

    @Operation(summary = "获取资料列表")
    @GetMapping
    public Result<List<MaterialVO>> getMaterials(HttpServletRequest request,
                                                  @RequestParam(required = false) Long subjectId,
                                                  @RequestParam(required = false) String keyword) {
        Long userId = getUserIdFromRequest(request);
        List<MaterialVO> materials = materialService.getMaterials(userId, subjectId, keyword);
        return Result.success(materials);
    }

    @Operation(summary = "获取资料详情")
    @GetMapping("/{materialId}")
    public Result<MaterialVO> getMaterialById(HttpServletRequest request,
                                               @PathVariable Long materialId) {
        Long userId = getUserIdFromRequest(request);
        MaterialVO material = materialService.getMaterialById(userId, materialId);
        return Result.success(material);
    }

    @Operation(summary = "上传资料")
    @PostMapping
    public Result<MaterialVO> uploadMaterial(HttpServletRequest request,
                                              @RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "subjectId", required = false) Long subjectId,
                                              @RequestParam(value = "title", required = false) String title,
                                              @RequestParam(value = "description", required = false) String description,
                                              @RequestParam(value = "tags", required = false) String tags) {
        Long userId = getUserIdFromRequest(request);
        MaterialRequest materialRequest = new MaterialRequest();
        materialRequest.setSubjectId(subjectId);
        materialRequest.setTitle(title);
        materialRequest.setDescription(description);
        materialRequest.setTags(tags);
        MaterialVO material = materialService.uploadMaterial(userId, materialRequest, file);
        return Result.success("上传成功", material);
    }

    @Operation(summary = "更新资料信息")
    @PutMapping("/{materialId}")
    public Result<MaterialVO> updateMaterial(HttpServletRequest request,
                                              @PathVariable Long materialId,
                                              @RequestBody MaterialRequest body) {
        Long userId = getUserIdFromRequest(request);
        MaterialVO material = materialService.updateMaterial(userId, materialId, body);
        return Result.success("更新成功", material);
    }

    @Operation(summary = "删除资料")
    @DeleteMapping("/{materialId}")
    public Result<Void> deleteMaterial(HttpServletRequest request,
                                        @PathVariable Long materialId) {
        Long userId = getUserIdFromRequest(request);
        materialService.deleteMaterial(userId, materialId);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "切换收藏状态")
    @PostMapping("/{materialId}/favorite")
    public Result<MaterialVO> toggleFavorite(HttpServletRequest request,
                                              @PathVariable Long materialId) {
        Long userId = getUserIdFromRequest(request);
        MaterialVO material = materialService.toggleFavorite(userId, materialId);
        return Result.success(material);
    }
}
