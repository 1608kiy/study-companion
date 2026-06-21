package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.AiClient;
import com.studycompanion.common.AiResponseParser;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.MissRecordRequest;
import com.studycompanion.entity.CheckIn;
import com.studycompanion.entity.MissRecord;
import com.studycompanion.mapper.CheckInMapper;
import com.studycompanion.mapper.MissRecordMapper;
import com.studycompanion.service.MissRecordService;
import com.studycompanion.vo.MissRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissRecordServiceImpl implements MissRecordService {

    private final MissRecordMapper missRecordMapper;
    private final CheckInMapper checkInMapper;
    private final AiClient aiClient;

    @Override
    public MissRecordVO recordMiss(Long userId, MissRecordRequest request) {
        MissRecord missRecord = new MissRecord();
        missRecord.setUserId(userId);
        missRecord.setMissDate(request.getMissDate() != null ? request.getMissDate() : LocalDate.now());
        missRecord.setReason(request.getReason());
        missRecord.setAiAllowReplenish(null);
        missRecord.setIsReplenished(0);

        missRecordMapper.insert(missRecord);
        return convertToVO(missRecord);
    }

    @Override
    public MissRecordVO aiJudgeReplenish(Long userId, Long missRecordId) {
        MissRecord missRecord = getMissRecordById(userId, missRecordId);

        // 调用 AI 判断是否允许补签
        String systemPrompt = "你是一个考公学习陪伴助手。根据用户描述的断签原因，判断是否允许补签。\n\n" +
                "判断规则：\n" +
                "- 生病、住院、身体不适 → 允许补签\n" +
                "- 考试、面试、出差、紧急事务 → 允许补签\n" +
                "- 家庭紧急情况、丧事、婚礼 → 允许补签\n" +
                "- 偷懒、忘记、不想学、玩手机 → 不允许补签\n" +
                "- 天气不好、心情不好、压力大 → 不允许补签\n\n" +
                "请只返回JSON格式：{\"allow\":true/false,\"reason\":\"简短原因\"}";

        String userMessage = "断签原因：" + missRecord.getReason() + "\n断签日期：" + missRecord.getMissDate();

        try {
            String result = aiClient.chat(systemPrompt, userMessage);
            
            // 解析 AI 返回的 JSON
            AiResponseParser.AiJudgmentResult judgment = AiResponseParser.parseJudgment(result);
            boolean allow = judgment.isAllow();
            
            missRecord.setAiAllowReplenish(allow ? 1 : 0);
            
            // 保存 AI 判断原因
            String reason = judgment.getReason();
            if (reason != null && !reason.isEmpty()) {
                missRecord.setReason(missRecord.getReason() + "\n[AI判断] " + reason);
            }
            
            log.info("AI补签判断: userId={}, missDate={}, allow={}, reason={}", 
                    userId, missRecord.getMissDate(), allow, reason);
        } catch (Exception e) {
            log.error("AI补签判断失败，使用默认规则: {}", e.getMessage());
            // 降级处理：根据关键词判断
            String reason = missRecord.getReason().toLowerCase();
            boolean allow = reason.contains("生病") || reason.contains("考试") || 
                           reason.contains("出差") || reason.contains("紧急");
            missRecord.setAiAllowReplenish(allow ? 1 : 0);
        }

        missRecordMapper.updateById(missRecord);
        return convertToVO(missRecord);
    }

    @Override
    @Transactional
    public MissRecordVO replenish(Long userId, Long missRecordId) {
        MissRecord missRecord = getMissRecordById(userId, missRecordId);

        if (missRecord.getAiAllowReplenish() == null || missRecord.getAiAllowReplenish() == 0) {
            throw new BusinessException(ErrorCode.REPLENISH_NOT_ALLOWED);
        }

        if (missRecord.getIsReplenished() == 1) {
            throw new BusinessException(ErrorCode.REPLENISH_ALREADY_COMPLETED);
        }

        // 标记已补签
        missRecord.setIsReplenished(1);
        missRecordMapper.updateById(missRecord);

        // 创建打卡记录
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckDate(missRecord.getMissDate());
        checkIn.setIsCompleted(1);
        checkIn.setTotalDuration(0);
        checkIn.setStreak(0); // streak 会在下次打卡时重新计算
        checkInMapper.insert(checkIn);

        return convertToVO(missRecord);
    }

    @Override
    public List<MissRecordVO> getMissRecords(Long userId) {
        LambdaQueryWrapper<MissRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MissRecord::getUserId, userId)
               .orderByDesc(MissRecord::getMissDate);
        List<MissRecord> records = missRecordMapper.selectList(wrapper);
        return records.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private MissRecord getMissRecordById(Long userId, Long missRecordId) {
        MissRecord missRecord = missRecordMapper.selectById(missRecordId);
        if (missRecord == null || !missRecord.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.MISS_RECORD_NOT_FOUND);
        }
        return missRecord;
    }

    private MissRecordVO convertToVO(MissRecord missRecord) {
        MissRecordVO vo = new MissRecordVO();
        vo.setId(missRecord.getId());
        vo.setMissDate(missRecord.getMissDate());
        vo.setReason(missRecord.getReason());
        vo.setAiAllowReplenish(missRecord.getAiAllowReplenish() != null && missRecord.getAiAllowReplenish() == 1);
        vo.setIsReplenished(missRecord.getIsReplenished() == 1);
        return vo;
    }
}
