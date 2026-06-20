package com.studycompanion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studycompanion.common.BusinessException;
import com.studycompanion.common.ErrorCode;
import com.studycompanion.dto.MissRecordRequest;
import com.studycompanion.entity.MissRecord;
import com.studycompanion.mapper.MissRecordMapper;
import com.studycompanion.service.MissRecordService;
import com.studycompanion.vo.MissRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissRecordServiceImpl implements MissRecordService {

    private final MissRecordMapper missRecordMapper;

    @Override
    public MissRecordVO recordMiss(Long userId, MissRecordRequest request) {
        MissRecord missRecord = new MissRecord();
        missRecord.setUserId(userId);
        missRecord.setMissDate(LocalDate.now());
        missRecord.setReason(request.getReason());
        missRecord.setAiAllowReplenish(null);
        missRecord.setIsReplenished(0);

        missRecordMapper.insert(missRecord);
        return convertToVO(missRecord);
    }

    @Override
    public MissRecordVO aiJudgeReplenish(Long userId, Long missRecordId) {
        MissRecord missRecord = getMissRecordById(userId, missRecordId);

        // TODO: 接入AI服务判断是否允许补签
        // 目前简单实现：根据断签原因判断
        String reason = missRecord.getReason().toLowerCase();
        boolean allow = reason.contains("生病") || reason.contains("考试") || reason.contains("出差");

        missRecord.setAiAllowReplenish(allow ? 1 : 0);
        missRecordMapper.updateById(missRecord);

        return convertToVO(missRecord);
    }

    @Override
    public MissRecordVO replenish(Long userId, Long missRecordId) {
        MissRecord missRecord = getMissRecordById(userId, missRecordId);

        if (missRecord.getAiAllowReplenish() == null || missRecord.getAiAllowReplenish() == 0) {
            throw new BusinessException(ErrorCode.REPLENISH_NOT_ALLOWED);
        }

        if (missRecord.getIsReplenished() == 1) {
            throw new BusinessException(ErrorCode.REPLENISH_ALREADY_COMPLETED);
        }

        missRecord.setIsReplenished(1);
        missRecordMapper.updateById(missRecord);

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
