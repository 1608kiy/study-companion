package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 打卡表Mapper接口
 */
@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {
}
