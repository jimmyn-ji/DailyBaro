package com.project.service.Impl;

import com.project.service.AIService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service("deepSeekAIService")
public class DeepSeekAIServiceImpl implements AIService {

    @Value("${deepseek.api-url}")
    private String apiUrl;

    @Value("${deepseek.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Result<String> getGeneralResponse(String question) {
        return callDeepSeek(question);
    }

    @Override
    public Result<String> getResponseForDiary(String diaryContent) {
        return callDeepSeek("请根据以下日记内容，给出情绪分析和建议：" + diaryContent);
    }

    private Result<String> callDeepSeek(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "deepseek-chat");
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        body.put("messages", Collections.singletonList(userMessage));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            // 解析返回内容（请根据DeepSeek实际返回结构调整）
            List choices = (List) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map choice = (Map) choices.get(0);
                Map msg = (Map) choice.get("message");
                String content = (String) msg.get("content");
                return Result.success(content);
            }
            return Result.fail("AI无有效回复");
        } catch (Exception e) {
            return Result.fail("AI服务调用失败: " + e.getMessage());
        }
    }
} 