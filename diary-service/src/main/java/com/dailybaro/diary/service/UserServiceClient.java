package com.dailybaro.diary.service;

import com.dailybaro.diary.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/users/{userId}")
    Result<Object> getUserById(@PathVariable("userId") Long userId);

    @PutMapping("/users/{userId}/energy")
    Result<Void> increaseEnergy(@PathVariable("userId") Long userId, @RequestParam("amount") Integer amount);
} 