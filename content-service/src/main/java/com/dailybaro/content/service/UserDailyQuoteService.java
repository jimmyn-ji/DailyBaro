package com.dailybaro.content.service;

import com.dailybaro.content.model.UserDailyQuote;
import com.dailybaro.common.util.Result;
import java.util.List;
 
public interface UserDailyQuoteService {
    Result<UserDailyQuote> getUserQuote(Long userId);
    Result<UserDailyQuote> saveOrUpdateUserQuote(Long userId, String content);
    Result<List<UserDailyQuote>> getQuoteHistory(Long userId);
    Result<UserDailyQuote> updateQuote(Long quoteId, Long userId, String content);
    Result<String> deleteQuote(Long quoteId, Long userId);
} 