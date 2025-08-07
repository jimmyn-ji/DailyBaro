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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DailyQuoteServiceImpl implements DailyQuoteService {

    private static final Logger log = LoggerFactory.getLogger(DailyQuoteServiceImpl.class);

    @Autowired
    private DailyQuoteMapper dailyQuoteMapper;

    @Override
    public Result<DailyQuote> getRandomQuote() {
        // A more efficient way for large tables would be to get a random ID within the range
        // or use a native SQL query with RAND() or a similar function.
        // For a small number of quotes, this is acceptable.
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
        
        // 使用用户ID和当前日期作为随机种子，确保同一天同一用户看到相同的随机日签
        LocalDate today = LocalDate.now();
        // 修复随机种子计算，确保不同用户每天都有不同的日签
        // 使用更复杂的种子计算，确保不同用户和不同日期的组合产生不同的结果
        long seed = userId * 1000000L + today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth();
        // 添加额外的随机因子，确保不同用户看到不同的日签
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
        
        // 使用用户ID、当前日期和当前时间作为随机种子，确保每次手动刷新都不同
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
} 