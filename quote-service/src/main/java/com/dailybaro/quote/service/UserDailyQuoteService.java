package com.dailybaro.quote.service;

import com.dailybaro.quote.model.UserDailyQuote;
import com.dailybaro.common.util.Result;
 
public interface UserDailyQuoteService {
    Result<UserDailyQuote> getUserQuote(Long userId);
    Result<UserDailyQuote> saveOrUpdateUserQuote(Long userId, String content);
} 