package com.dailybaro.ai.controller;

import com.dailybaro.ai.service.AIService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    @Qualifier("deepSeekAIService") // 使用DeepSeek AI
    private AIService deepSeekAIService;
    
    @Autowired
    @Qualifier("mockAIService") // 备用Mock AI
    private AIService mockAIService;

    @PostMapping("/query")
    public Result<String> askGeneralQuestion(@RequestBody AIQueryDTO query) {
        // 首先尝试DeepSeek，如果失败则使用Mock AI
        Result<String> result = deepSeekAIService.getGeneralResponse(query.getQuestion());
        if (result.getCode() != 200) {
            // DeepSeek失败，使用Mock AI
            return mockAIService.getGeneralResponse(query.getQuestion());
        }
        return result;
    }

    @PostMapping("/diary-feedback")
    public Result<String> getDiaryFeedback(@RequestBody AIDiaryFeedbackDTO feedbackRequest) {
        // 首先尝试DeepSeek，如果失败则使用Mock AI
        Result<String> result = deepSeekAIService.getResponseForDiary(feedbackRequest.getDiaryContent());
        if (result.getCode() != 200) {
            // DeepSeek失败，使用Mock AI
            return mockAIService.getResponseForDiary(feedbackRequest.getDiaryContent());
        }
        return result;
    }

    // Simple DTOs for request bodies
    static class AIQueryDTO {
        private String question;
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }

    static class AIDiaryFeedbackDTO {
        private String diaryContent;
        public String getDiaryContent() { return diaryContent; }
        public void setDiaryContent(String diaryContent) { this.diaryContent = diaryContent; }
    }
} 