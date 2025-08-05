package com.dailybaro.user.controller;

import com.dailybaro.user.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("user-service is running!");
    }

    @GetMapping("/info")
    public Result<String> info() {
        return Result.success("User Service - DailyBaro Microservice");
    }
} 