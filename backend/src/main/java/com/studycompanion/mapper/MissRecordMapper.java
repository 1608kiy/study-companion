package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.MissRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 断签记录表Mapper接口
 */
@Mapper
public interface MissRecordMapper extends BaseMapper<MissRecord> {
}
