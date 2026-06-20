package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.UserStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户统计表Mapper接口
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
}
