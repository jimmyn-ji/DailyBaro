package com.dailybaro.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.content.model.DailyQuote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyQuoteMapper extends BaseMapper<DailyQuote> {
} 