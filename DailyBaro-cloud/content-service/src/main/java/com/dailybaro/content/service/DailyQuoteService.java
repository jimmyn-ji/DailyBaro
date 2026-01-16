package com.dailybaro.content.service;

import com.dailybaro.content.model.DailyQuote;
import com.dailybaro.common.util.Result;
import java.util.List;

public interface DailyQuoteService {
    Result<DailyQuote> getRandomQuote();
    Result<DailyQuote> getRandomQuoteForUser(Long userId);
    Result<DailyQuote> getManualRandomQuoteForUser(Long userId);
    Result<List<DailyQuote>> getRandomQuoteOptions(Long userId, int count);
} 