package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.Diary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记表Mapper接口
 */
@Mapper
public interface DiaryMapper extends BaseMapper<Diary> {
}
