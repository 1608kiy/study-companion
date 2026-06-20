package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.Goal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 目标表Mapper接口
 */
@Mapper
public interface GoalMapper extends BaseMapper<Goal> {
}
