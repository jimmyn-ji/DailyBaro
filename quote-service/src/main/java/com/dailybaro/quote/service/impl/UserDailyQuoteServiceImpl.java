package com.dailybaro.quote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dailybaro.quote.mapper.UserDailyQuoteMapper;
import com.dailybaro.quote.model.UserDailyQuote;
import com.dailybaro.quote.service.UserDailyQuoteService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
public class UserDailyQuoteServiceImpl implements UserDailyQuoteService {
    @Autowired
    private UserDailyQuoteMapper userDailyQuoteMapper;
    
    @Override
    public Result<UserDailyQuote> getUserQuote(Long userId) {
        UserDailyQuote quote = userDailyQuoteMapper.selectOne(
                new QueryWrapper<UserDailyQuote>().eq("user_id", userId)
        );
        if (quote == null) {
            return Result.success(null); // 查不到时返回null，前端才能继续请求随机日签
        }
        return Result.success(quote);
    }
    
    @Override
    public Result<UserDailyQuote> saveOrUpdateUserQuote(Long userId, String content) {
        // 检查今天是否已经修改过
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        UserDailyQuote existingQuote = userDailyQuoteMapper.selectOne(
                new QueryWrapper<UserDailyQuote>().eq("user_id", userId)
        );
        
        if (existingQuote != null) {
            // 检查今天是否已经修改过
            if (existingQuote.getUpdateTime() != null && 
                existingQuote.getUpdateTime().after(startOfDay) && 
                existingQuote.getUpdateTime().before(endOfDay)) {
                return Result.fail("今日已修改过日签，明天再来吧");
            }
            
            // 更新内容
            existingQuote.setContent(content);
            existingQuote.setUpdateTime(new Date());
            userDailyQuoteMapper.updateById(existingQuote);
        } else {
            // 创建新的日签
            existingQuote = new UserDailyQuote();
            existingQuote.setUserId(userId);
            existingQuote.setContent(content);
            existingQuote.setCreateTime(new Date());
            existingQuote.setUpdateTime(new Date());
            userDailyQuoteMapper.insert(existingQuote);
        }
        
        return Result.success(existingQuote);
    }
} 