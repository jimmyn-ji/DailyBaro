package com.dailybaro.content.controller;

import com.dailybaro.content.model.DailyQuote;
import com.dailybaro.content.model.UserDailyQuote;
import com.dailybaro.content.service.DailyQuoteService;
import com.dailybaro.content.service.RefreshLimitService;
import com.dailybaro.content.service.UserDailyQuoteService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class DailyQuoteController {

    @Autowired
    private DailyQuoteService dailyQuoteService;

    @Autowired
    private UserDailyQuoteService userDailyQuoteService;

    @Autowired
    private RefreshLimitService refreshLimitService;

    @GetMapping("/random")
    public Result<DailyQuote> getRandomQuote() {
        return dailyQuoteService.getRandomQuote();
    }
    
    @GetMapping("/random/user")
    public Result<DailyQuote> getRandomQuoteForUser(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return dailyQuoteService.getRandomQuoteForUser(userId);
    }

    @GetMapping("/random/user/options")
    public Result<List<DailyQuote>> getRandomQuoteOptions(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return dailyQuoteService.getRandomQuoteOptions(userId, 5); // 返回5个选项
    }

    @GetMapping("/random/user/manual")
    public Result<DailyQuote> getManualRandomQuote(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        if (!refreshLimitService.allowAndIncrement(userId)) {
            return Result.error(429, "今日已达换一换上限(3次)");
        }
        return dailyQuoteService.getManualRandomQuoteForUser(userId);
    }

    @GetMapping("/test/random")
    public Result<DailyQuote> testRandomQuote(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        
        // 测试不同日期的随机种子
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);
        
        System.out.println("测试日签随机逻辑:");
        System.out.println("今天: " + today + ", 用户ID: " + userId);
        System.out.println("昨天: " + yesterday + ", 用户ID: " + userId);
        
        return dailyQuoteService.getRandomQuoteForUser(userId);
    }

    @GetMapping("/custom")
    public Result<UserDailyQuote> getCustomQuote(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return userDailyQuoteService.getUserQuote(userId);
    }

    @PostMapping("/custom")
    public Result<UserDailyQuote> saveCustomQuote(@RequestBody UserDailyQuote quote, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return userDailyQuoteService.saveOrUpdateUserQuote(userId, quote.getContent());
    }

    // 新增历史记录相关接口
    @GetMapping("/history")
    public Result<List<UserDailyQuote>> getQuoteHistory(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return userDailyQuoteService.getQuoteHistory(userId);
    }

    @PutMapping("/{quoteId}")
    public Result<UserDailyQuote> updateQuote(@PathVariable Long quoteId, @RequestBody UserDailyQuote quote, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return userDailyQuoteService.updateQuote(quoteId, userId, quote.getContent());
    }

    @DeleteMapping("/{quoteId}")
    public Result<String> deleteQuote(@PathVariable Long quoteId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
        return userDailyQuoteService.deleteQuote(quoteId, userId);
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String uidStr = request.getHeader("uid");
        if (uidStr == null) return null;
        try {
            return Long.parseLong(uidStr);
        } catch (Exception e) {
            return null;
        }
    }
} 