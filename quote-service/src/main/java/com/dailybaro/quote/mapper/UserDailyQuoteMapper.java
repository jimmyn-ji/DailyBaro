package com.dailybaro.quote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailybaro.quote.model.UserDailyQuote;
import org.apache.ibatis.annotations.Mapper;
 
@Mapper
public interface UserDailyQuoteMapper extends BaseMapper<UserDailyQuote> {
} 