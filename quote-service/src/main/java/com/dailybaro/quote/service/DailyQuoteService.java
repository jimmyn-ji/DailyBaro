package com.dailybaro.quote.service;

import com.dailybaro.quote.model.DailyQuote;
import com.dailybaro.common.util.Result;

public interface DailyQuoteService {
    Result<DailyQuote> getRandomQuote();
    Result<DailyQuote> getRandomQuoteForUser(Long userId);
    Result<DailyQuote> getManualRandomQuoteForUser(Long userId);
} 