package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.CreateDiaryRequest;
import com.studycompanion.dto.UpdateDiaryRequest;
import com.studycompanion.entity.Diary;
import com.studycompanion.entity.StudyRecord;
import com.studycompanion.entity.Subject;
import com.studycompanion.mapper.DiaryMapper;
import com.studycompanion.mapper.StudyRecordMapper;
import com.studycompanion.mapper.SubjectMapper;
import com.studycompanion.service.DiaryService;
import com.studycompanion.service.DiaryImageService;
import com.studycompanion.vo.DiaryImageVO;
import com.studycompanion.vo.DiaryVO;
import com.studycompanion.vo.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final SubjectMapper subjectMapper;
    private final DiaryImageService diaryImageService;
    private final AiClient aiClient;

    private static final int MAX_AI_GENERATE_COUNT = 3;

    @Override
    public List<DiaryVO> getDiaryList(Long userId, String month) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .ge(Diary::getDiaryDate, startDate)
               .le(Diary::getDiaryDate, endDate)
               .orderByDesc(Diary::getDiaryDate);

        List<Diary> diaries = diaryMapper.selectList(wrapper);
        return diaries.stream()
                .map(d -> convertToVO(d, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<DiaryVO> getDiaryListPaged(Long userId, String month, int page, int size) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .ge(Diary::getDiaryDate, startDate)
               .le(Diary::getDiaryDate, endDate)
               .orderByDesc(Diary::getDiaryDate);

        // 获取总数
        long total = diaryMapper.selectCount(wrapper);

        // 分页查询
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + size + " OFFSET " + offset);
        List<Diary> diaries = diaryMapper.selectList(wrapper);

        List<DiaryVO> voList = diaries.stream()
                .map(d -> convertToVO(d, userId))
                .collect(Collectors.toList());

        return new PageResponse<>(voList, total, page, size);
    }

    @Override
    public DiaryVO getDiaryByDate(Long userId, String date) {
        LocalDate diaryDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .eq(Diary::getDiaryDate, diaryDate);
        Diary diary = diaryMapper.selectOne(wrapper);

        if (diary == null) {
            return null;
        }

        return convertToVO(diary, userId);
    }

    @Override
    @Transactional
    public DiaryVO createDiary(Long userId, CreateDiaryRequest request) {
        LocalDate today = LocalDate.now();

        // 检查今日日记是否已存在
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .eq(Diary::getDiaryDate, today);
        if (diaryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setDiaryDate(today);
        diary.setContent(request.getContent());
        diary.setSummary(request.getSummary());
        diary.setPlan(request.getPlan());
        diary.setReflection(request.getReflection());
        diary.setProblems(request.getProblems());
        diary.setAiGenerated(0);
        diary.setAiGenerateCount(0);

        diaryMapper.insert(diary);
        return convertToVO(diary, userId);
    }

    @Override
    @Transactional
    public DiaryVO updateDiary(Long userId, Long diaryId, UpdateDiaryRequest request) {
        Diary diary = getDiaryById(userId, diaryId);

        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }
        if (request.getSummary() != null) {
            diary.setSummary(request.getSummary());
        }
        if (request.getPlan() != null) {
            diary.setPlan(request.getPlan());
        }
        if (request.getReflection() != null) {
            diary.setReflection(request.getReflection());
        }
        if (request.getProblems() != null) {
            diary.setProblems(request.getProblems());
        }

        diaryMapper.updateById(diary);
        return convertToVO(diary, userId);
    }

    @Override
    @Transactional
    public void deleteDiary(Long userId, Long diaryId) {
        Diary diary = getDiaryById(userId, diaryId);
        diaryMapper.deleteById(diaryId);
    }

    @Override
    @Transactional
    public DiaryVO generateDiary(Long userId) {
        LocalDate today = LocalDate.now();

        // 检查今日日记是否已存在
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Diary::getUserId, userId)
               .eq(Diary::getDiaryDate, today);
        Diary existingDiary = diaryMapper.selectOne(wrapper);

        if (existingDiary != null) {
            if (existingDiary.getAiGenerateCount() >= MAX_AI_GENERATE_COUNT) {
                throw new BusinessException(ErrorCode.DIARY_AI_COUNT_EXCEEDED);
            }
            // 重新生成内容
            String newContent = generateDiaryContent(userId, today);
            existingDiary.setContent(newContent);
            existingDiary.setAiGenerateCount(existingDiary.getAiGenerateCount() + 1);
            diaryMapper.updateById(existingDiary);
            return convertToVO(existingDiary, userId);
        }

        // 获取今日学习记录并生成内容
        String content = generateDiaryContent(userId, today);

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setDiaryDate(today);
        diary.setContent(content);
        diary.setAiGenerated(1);
        diary.setAiGenerateCount(1);

        diaryMapper.insert(diary);
        return convertToVO(diary, userId);
    }

    private String generateDiaryContent(Long userId, LocalDate date) {
        // 获取学习记录
        LambdaQueryWrapper<StudyRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(StudyRecord::getUserId, userId)
                     .eq(StudyRecord::getStudyDate, date);
        List<StudyRecord> records = studyRecordMapper.selectList(recordWrapper);

        // 批量查询科目
        Set<Long> subjectIds = records.stream()
                .map(StudyRecord::getSubjectId)
                .collect(Collectors.toSet());
        Map<Long, Subject> subjectMap = subjectIds.isEmpty()
                ? Collections.emptyMap()
                : subjectMapper.selectBatchIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, s -> s));

        // 构建学习数据摘要
        StringBuilder dataSummary = new StringBuilder();
        dataSummary.append("日期：").append(date).append("\n");

        if (records.isEmpty()) {
            dataSummary.append("今天没有学习记录。\n");
        } else {
            dataSummary.append("学习科目：\n");
            int totalDuration = 0;
            for (StudyRecord record : records) {
                Subject subject = subjectMap.get(record.getSubjectId());
                String subjectName = subject != null ? subject.getName() : "未知科目";
                dataSummary.append("- ").append(subjectName).append("：").append(record.getDuration()).append("分钟\n");
                totalDuration += record.getDuration();
            }
            dataSummary.append("总学习时长：").append(totalDuration).append("分钟\n");
        }

        // 调用 AI 生成日记
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户今天的学习数据，生成一篇温暖、鼓励的学习日记。\n\n" +
                "要求：\n" +
                "1. 使用 Markdown 格式\n" +
                "2. 包含今日学习总结\n" +
                "3. 如果有学习记录，分析学习情况，给出建议\n" +
                "4. 如果没有学习记录，鼓励用户明天开始\n" +
                "5. 语气温暖积极，像朋友一样\n" +
                "6. 字数控制在 200-400 字";

        try {
            return aiClient.chat(systemPrompt, "请根据以下学习数据生成日记：\n\n" + dataSummary);
        } catch (Exception e) {
            log.warn("AI日记生成失败，使用模板: {}", e.getMessage());
            // 降级到模板
            if (records.isEmpty()) {
                return "# 今日学习日记\n\n今天没有学习记录。休息一下，明天继续加油！\n\n记住，坚持就是胜利。";
            }
            return "# 今日学习日记\n\n" + dataSummary + "\n\n继续努力，每天进步一点点！";
        }
    }

    @Override
    @Transactional
    public DiaryVO regenerateDiary(Long userId, Long diaryId) {
        Diary diary = getDiaryById(userId, diaryId);

        if (diary.getAiGenerateCount() >= MAX_AI_GENERATE_COUNT) {
            throw new BusinessException(ErrorCode.DIARY_AI_COUNT_EXCEEDED);
        }

        // 重新生成日记内容
        String content = generateDiaryContent(userId, diary.getDiaryDate());
        diary.setContent(content);
        diary.setAiGenerateCount(diary.getAiGenerateCount() + 1);
        diaryMapper.updateById(diary);

        return convertToVO(diary, userId);
    }

    private Diary getDiaryById(Long userId, Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_NOT_FOUND);
        }
        return diary;
    }

    private DiaryVO convertToVO(Diary diary, Long userId) {
        DiaryVO vo = new DiaryVO();
        vo.setId(diary.getId());
        vo.setDiaryDate(diary.getDiaryDate());
        vo.setContent(diary.getContent());
        vo.setSummary(diary.getSummary());
        vo.setPlan(diary.getPlan());
        vo.setReflection(diary.getReflection());
        vo.setProblems(diary.getProblems());
        vo.setAiGenerated(diary.getAiGenerated() == 1);
        vo.setAiGenerateCount(diary.getAiGenerateCount());
        vo.setCreateTime(diary.getCreateTime());
        vo.setUpdateTime(diary.getUpdateTime());

        // 获取图片列表
        List<DiaryImageVO> images = diaryImageService.getDiaryImages(userId, diary.getId());
        vo.setImages(images);

        return vo;
    }
}
