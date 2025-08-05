package com.dailybaro.quote.controller;

import com.dailybaro.quote.util.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Quote service is running!");
    }

    @GetMapping("/random")
    public Result<String> getRandomQuote() {
        return Result.success("今日日签：生活就像一盒巧克力，你永远不知道下一颗是什么味道。");
    }
} 