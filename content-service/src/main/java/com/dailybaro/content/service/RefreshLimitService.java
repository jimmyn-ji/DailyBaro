package com.dailybaro.content.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshLimitService {
    private final Map<String, Integer> userDateToCount = new ConcurrentHashMap<>();
    private static final int DAILY_LIMIT = 3;

    public synchronized boolean allowAndIncrement(Long userId) {
        String key = userId + ":" + LocalDate.now();
        int cnt = userDateToCount.getOrDefault(key, 0);
        if (cnt >= DAILY_LIMIT) {
            return false;
        }
        userDateToCount.put(key, cnt + 1);
        return true;
    }
}


