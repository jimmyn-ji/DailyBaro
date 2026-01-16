package com.dailybaro.aiknowledge.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dailybaro.aiknowledge.model.vo.ConversationMessage;
import com.dailybaro.aiknowledge.service.AIService;
import com.dailybaro.aiknowledge.service.ConversationService;
import com.dailybaro.aiknowledge.service.RAGChatService;
import com.dailybaro.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AIService aiService;
    
    @Autowired
    private RAGChatService ragChatService;
    
    @Autowired
    private ConversationService conversationService;

    // 优先使用真正接入 DeepSeek 的实现
    public AiController(@Qualifier("deepSeekAIService") AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * 兼容 Web 端和小程序端的 AI 文本问答接口（支持RAG）
     * 请求体示例：{ "question": "今天心情有点低落，怎么办？" } 或 { "message": "今天心情有点低落，怎么办？" }
     * 如果请求头包含uid，将使用RAG增强并保存对话历史
     */
    @PostMapping("/query")
    public Map<String, Object> query(
            @RequestHeader(value = "uid", required = false) String uid,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            String question = "";
            if (body != null) {
                // 支持 "question" 和 "message" 两种字段名（兼容 Web 端和 App 端）
                Object q = body.get("question");
                if (q == null || q.toString().trim().isEmpty()) {
                    q = body.get("message");
                }
                if (q != null) {
                    question = q.toString();
                }
            }
            if (question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                return result;
            }

            // 如果提供了uid，使用RAG增强的对话服务
            if (uid != null && !uid.trim().isEmpty()) {
                try {
                    Long userId = Long.valueOf(uid);
                    Result<String> aiResult = ragChatService.chatWithRAG(userId, question);
                    result.put("code", aiResult.getCode());
                    result.put("data", aiResult.getData());
                    result.put("message", aiResult.getMessage());
                    return result;
                } catch (NumberFormatException e) {
                    // uid格式错误，降级到普通AI服务
                }
            }

            // 降级到普通AI服务
            Result<String> aiResult = aiService.getGeneralResponse(question);
            result.put("code", aiResult.getCode());
            result.put("data", aiResult.getData());
            result.put("message", aiResult.getMessage());
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "AI服务内部错误: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * RAG增强的AI对话接口
     * 请求体示例：{ "question": "今天心情有点低落，怎么办？" } 或 { "message": "今天心情有点低落，怎么办？" }
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(
            @RequestHeader(value = "uid", required = false) String uid,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (uid == null || uid.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "用户ID不能为空");
                return result;
            }
            
            String question = "";
            if (body != null) {
                // 支持 "question" 和 "message" 两种字段名（兼容 Web 端和 App 端）
                Object q = body.get("question");
                if (q == null || q.toString().trim().isEmpty()) {
                    q = body.get("message");
                }
                if (q != null) {
                    question = q.toString();
                }
            }
            if (question.trim().isEmpty()) {
                result.put("code", 400);
                result.put("message", "问题不能为空");
                return result;
            }

            Long userId = Long.valueOf(uid);
            Result<String> aiResult = ragChatService.chatWithRAG(userId, question);
            result.put("code", aiResult.getCode());
            result.put("data", aiResult.getData());
            result.put("message", aiResult.getMessage());
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "AI服务内部错误: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 获取对话历史
     */
    @GetMapping("/conversation/{uid}")
    public Map<String, Object> getConversation(
            @PathVariable String uid,
            @RequestParam(defaultValue = "20") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = Long.valueOf(uid);
            List<ConversationMessage> history = conversationService.getConversationHistory(userId, limit);
            result.put("code", 200);
            result.put("data", history);
            result.put("message", "success");
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取对话历史失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 清除对话历史
     */
    @DeleteMapping("/conversation/{uid}")
    public Map<String, Object> clearConversation(@PathVariable String uid) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = Long.valueOf(uid);
            conversationService.clearConversationHistory(userId);
            result.put("code", 200);
            result.put("message", "对话历史已清除");
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "清除对话历史失败: " + e.getMessage());
            return result;
        }
    }
}