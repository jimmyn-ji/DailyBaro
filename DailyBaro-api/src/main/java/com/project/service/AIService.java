package com.project.service;

import com.project.util.Result;

public interface AIService {

    /**
     * Get a response for a general question.
     * @param question The user's question.
     * @return The AI's response.
     */
    Result<String> getGeneralResponse(String question);

    /**
     * Get a response based on diary content.
     * @param diaryContent The content of the diary.
     * @return A context-aware response from the AI.
     */
    Result<String> getResponseForDiary(String diaryContent);
} 