package com.studycompanion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studycompanion.entity.AiAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI分析记录表Mapper接口
 */
@Mapper
public interface AiAnalysisMapper extends BaseMapper<AiAnalysis> {
}
