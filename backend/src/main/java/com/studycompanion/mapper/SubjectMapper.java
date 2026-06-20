package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.Subject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科目表Mapper接口
 */
@Mapper
public interface SubjectMapper extends BaseMapper<Subject> {
}
