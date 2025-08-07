package com.dailybaro.quote.controller;

import com.dailybaro.quote.model.DailyQuote;
import com.dailybaro.quote.model.UserDailyQuote;
import com.dailybaro.quote.service.DailyQuoteService;
import com.dailybaro.quote.service.UserDailyQuoteService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/quotes")
public class DailyQuoteController {

    @Autowired
    private DailyQuoteService dailyQuoteService;

    @Autowired
    private UserDailyQuoteService userDailyQuoteService;

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

    @GetMapping("/random/user/manual")
    public Result<DailyQuote> getManualRandomQuote(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) return Result.fail("用户未登录");
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