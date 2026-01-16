package com.dailybaro.content.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.dailybaro.common.util.Result;

@FeignClient(name = "user-service", url = "http://localhost:8001", contextId = "userClient")
public interface UserClient {
    @PostMapping("/users/increaseEnergy")
    Result<String> increaseEnergy(@RequestParam("uid") Long uid, @RequestParam("energy") int energy);
}
