package com.dailybaro.ai.service;

import com.dailybaro.common.util.Result;

public interface AIService {
    Result<String> getGeneralResponse(String question);
    Result<String> getResponseForDiary(String diaryContent);
}