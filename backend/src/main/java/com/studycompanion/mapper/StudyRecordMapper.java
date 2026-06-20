package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录表Mapper接口
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {
}
