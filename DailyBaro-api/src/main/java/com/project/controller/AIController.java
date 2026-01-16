package com.project.controller;

import com.project.service.AIService;
import com.project.util.Result;
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
        // 只使用真实的DeepSeek AI服务
        Result<String> result = deepSeekAIService.getGeneralResponse(query.getQuestion());
        if (result.getCode() != 200) {
            // DeepSeek失败，返回错误信息
            return Result.fail("AI服务暂时不可用，请稍后重试");
        }
        return result;
    }

    @PostMapping("/diary-feedback")
    public Result<String> getDiaryFeedback(@RequestBody AIDiaryFeedbackDTO feedbackRequest) {
        // 只使用真实的DeepSeek AI服务
        Result<String> result = deepSeekAIService.getResponseForDiary(feedbackRequest.getDiaryContent());
        if (result.getCode() != 200) {
            // DeepSeek失败，返回错误信息
            return Result.fail("AI服务暂时不可用，请稍后重试");
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