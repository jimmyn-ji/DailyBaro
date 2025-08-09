package com.dailybaro.quote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.quote.mapper.DailyQuoteMapper;
import com.dailybaro.quote.model.DailyQuote;
import com.dailybaro.quote.service.DailyQuoteService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DailyQuoteServiceImpl implements DailyQuoteService {

    private static final Logger log = LoggerFactory.getLogger(DailyQuoteServiceImpl.class);

    @Autowired
    private DailyQuoteMapper dailyQuoteMapper;

    @Override
    public Result<DailyQuote> getRandomQuote() {
        List<DailyQuote> allQuotes = dailyQuoteMapper.selectList(new QueryWrapper<>());
        if (allQuotes.isEmpty()) {
            return Result.fail("No quotes available.");
        }
        Random random = new Random();
        DailyQuote randomQuote = allQuotes.get(random.nextInt(allQuotes.size()));
        return Result.success(randomQuote);
    }
    
    @Override
    public Result<DailyQuote> getRandomQuoteForUser(Long userId) {
        List<DailyQuote> allQuotes = dailyQuoteMapper.selectList(new QueryWrapper<>());
        if (allQuotes.isEmpty()) {
            log.warn("没有可用的日签数据");
            return Result.fail("No quotes available.");
        }
        
        LocalDate today = LocalDate.now();
        long seed = userId * 1000000L + today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth();
        seed = seed ^ (userId * 31L) ^ (today.getDayOfYear() * 17L);
        Random random = new Random(seed);
        
        log.info("生成用户专属随机日签: userId={}, today={}, seed={}, totalQuotes={}", 
                userId, today, seed, allQuotes.size());
        
        int randomIndex = random.nextInt(allQuotes.size());
        DailyQuote randomQuote = allQuotes.get(randomIndex);
        
        log.info("选择的日签: index={}, content={}", randomIndex, randomQuote.getContent());
        
        return Result.success(randomQuote);
    }

    @Override
    public Result<DailyQuote> getManualRandomQuoteForUser(Long userId) {
        List<DailyQuote> allQuotes = dailyQuoteMapper.selectList(new QueryWrapper<>());
        if (allQuotes.isEmpty()) {
            log.warn("没有可用的日签数据");
            return Result.fail("No quotes available.");
        }
        
        LocalDate today = LocalDate.now();
        long currentTimeMillis = System.currentTimeMillis();
        long seed = userId * 1000000L + today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth() + currentTimeMillis;
        Random random = new Random(seed);
        
        log.info("手动生成用户专属随机日签: userId={}, today={}, seed={}, totalQuotes={}", 
                userId, today, seed, allQuotes.size());
        
        int randomIndex = random.nextInt(allQuotes.size());
        DailyQuote randomQuote = allQuotes.get(randomIndex);
        
        log.info("手动选择的日签: index={}, content={}", randomIndex, randomQuote.getContent());
        
        return Result.success(randomQuote);
    }

    @Override
    public Result<List<DailyQuote>> getRandomQuoteOptions(Long userId, int count) {
        List<DailyQuote> allQuotes = dailyQuoteMapper.selectList(new QueryWrapper<>());
        if (allQuotes.isEmpty()) {
            log.warn("没有可用的日签数据");
            return Result.fail("No quotes available.");
        }
        
        int actualCount = Math.min(count, allQuotes.size());
        
        long currentTimeMillis = System.currentTimeMillis();
        long seed = userId * 1000000L + currentTimeMillis;
        Random random = new Random(seed);
        
        log.info("生成用户日签选项: userId={}, count={}, totalQuotes={}", 
                userId, actualCount, allQuotes.size());
        
        HashSet<Integer> selectedIndices = new HashSet<>();
        List<DailyQuote> selectedQuotes = new ArrayList<>();
        
        while (selectedIndices.size() < actualCount) {
            int randomIndex = random.nextInt(allQuotes.size());
            if (selectedIndices.add(randomIndex)) {
                selectedQuotes.add(allQuotes.get(randomIndex));
            }
        }
        
        log.info("选择的日签选项数量: {}", selectedQuotes.size());
        
        return Result.success(selectedQuotes);
    }
}
