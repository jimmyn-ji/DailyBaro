package com.dailybaro.ai.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody Map<String, String> body) {
        String question = body.getOrDefault("question", "");
        String apiKey = "sk-92d68b33753d4ab88721f7abbe4713fa";
        String url = "https://api.deepseek.com/v1/chat/completions";

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "deepseek-chat");
        payload.put("max_tokens", 64);
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question);
        messages.add(userMsg);
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(300);
        factory.setReadTimeout(700);
        RestTemplate restTemplate = new RestTemplate(factory);

        Map<String, Object> result = new HashMap<>();
        ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
        Object bodyObj = response.getBody();
        String aiReply = "";
        if (bodyObj instanceof Map) {
            Map<?, ?> respBody = (Map<?, ?>) bodyObj;
            Object choicesObj = respBody.get("choices");
            if (choicesObj instanceof List) {
                List<?> choices = (List<?>) choicesObj;
                if (!choices.isEmpty() && choices.get(0) instanceof Map) {
                    Map<?, ?> choice0 = (Map<?, ?>) choices.get(0);
                    Object messageObj = choice0.get("message");
                    if (messageObj instanceof Map) {
                        Object content = ((Map<?, ?>) messageObj).get("content");
                        aiReply = content == null ? "" : content.toString();
                    }
                }
            }
        }
        if (aiReply.isEmpty()) {
            result.put("code", 502);
            result.put("message", "AI网关无内容返回");
            return result;
        }
        result.put("code", 200);
        result.put("data", aiReply);
        return result;
    }
} 