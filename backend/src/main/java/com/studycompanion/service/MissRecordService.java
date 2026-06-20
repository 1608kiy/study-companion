package com.studycompanion.service;

import com.studycompanion.dto.MissRecordRequest;
import com.studycompanion.vo.MissRecordVO;

import java.util.List;

public interface MissRecordService {

    MissRecordVO recordMiss(Long userId, MissRecordRequest request);

    MissRecordVO aiJudgeReplenish(Long userId, Long missRecordId);

    MissRecordVO replenish(Long userId, Long missRecordId);

    List<MissRecordVO> getMissRecords(Long userId);
}
