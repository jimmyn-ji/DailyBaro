package com.dailybaro.quote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.quote.model.DailyQuote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyQuoteMapper extends BaseMapper<DailyQuote> {
} 