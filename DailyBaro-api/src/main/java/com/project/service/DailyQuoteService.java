package com.project.service;

import com.project.model.DailyQuote;
import com.project.util.Result;
import java.util.List;

public interface DailyQuoteService {
    Result<DailyQuote> getRandomQuote();
    Result<DailyQuote> getRandomQuoteForUser(Long userId);
    Result<DailyQuote> getManualRandomQuoteForUser(Long userId);
    Result<List<DailyQuote>> getRandomQuoteOptions(Long userId, int count);
} 