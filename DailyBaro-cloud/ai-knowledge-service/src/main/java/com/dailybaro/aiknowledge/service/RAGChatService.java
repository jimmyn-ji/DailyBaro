package com.dailybaro.aiknowledge.service;

import com.dailybaro.aiknowledge.model.vo.ConversationMessage;
import com.dailybaro.aiknowledge.service.ConversationService;
import com.dailybaro.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.*;

/**
 * RAG增强的AI对话服务
 * 集成知识库检索和对话历史
 */
@Slf4j
@Service
public class RAGChatService {
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private AIService aiService;
    
    @Value("${rag.enabled:true}")
    private boolean ragEnabled;
    
    @Value("${rag.knowledge-service-url:http://localhost:8013}")
    private String knowledgeServiceUrl;  // 现在在同一个服务内，但保留配置以便未来拆分
    
    @Value("${rag.top-k:5}")
    private int topK;
    
    private final WebClient webClient;
    
    @Value("${rag.timeout-seconds:10}")
    private int ragTimeoutSeconds;
    
    public RAGChatService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }
    
    /**
     * RAG增强的AI对话
     */
    public Result<String> chatWithRAG(Long userId, String question) {
        try {
            // 1. 如果启用RAG，先从知识库检索相关内容
            String context = "";
            if (ragEnabled) {
                context = retrieveKnowledgeContext(question);
            }
            
            // 2. 获取用户最近的对话历史（用于上下文理解）
            String conversationContext = conversationService.getRecentContext(userId, 5);
            
            // 3. 构建增强的提示词
            String enhancedPrompt = buildEnhancedPrompt(question, context, conversationContext);
            
            // 4. 调用AI服务
            Result<String> aiResult = aiService.getGeneralResponse(enhancedPrompt);
            
            // 5. 保存对话历史到Redis（只保存用户问题和AI回答，不保存RAG检索的内容）
            if (aiResult.getCode() == 200 && aiResult.getData() != null) {
                conversationService.saveMessage(userId, new ConversationMessage("user", question));
                conversationService.saveMessage(userId, new ConversationMessage("ai", aiResult.getData()));
            }
            
            return aiResult;
            
        } catch (Exception e) {
            log.error("RAG对话失败 userId={}, question={}", userId, question, e);
            return Result.fail("AI服务暂时不可用，请稍后重试");
        }
    }
    
    /**
     * 从知识库检索相关内容
     */
    private String retrieveKnowledgeContext(String question) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> searchDTO = new HashMap<>();
            searchDTO.put("query", question);
            searchDTO.put("useRAG", true);
            searchDTO.put("size", topK);
            requestBody.putAll(searchDTO);
            
            Map<String, Object> response = webClient.post()
                    .uri(knowledgeServiceUrl + "/api/knowledge/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(ragTimeoutSeconds))
                    .onErrorResume(e -> {
                        log.warn("知识库检索超时或失败，将使用通用AI回答", e);
                        return Mono.empty();
                    })
                    .block();
            
            if (response != null) {
                Object code = response.get("code");
                if (code != null && (code instanceof Integer && (Integer)code == 200 || "200".equals(String.valueOf(code)))) {
                    Object data = response.get("data");
                    if (data instanceof Map) {
                        Map<String, Object> resultData = (Map<String, Object>) data;
                        Object aiAnswer = resultData.get("aiAnswer");
                        Object knowledgeList = resultData.get("knowledgeList");
                        
                        // 构建知识库上下文
                        StringBuilder context = new StringBuilder();
                        if (knowledgeList instanceof List) {
                            List<Map<String, Object>> knowledgeItems = (List<Map<String, Object>>) knowledgeList;
                            for (Map<String, Object> item : knowledgeItems) {
                                String title = (String) item.get("title");
                                String content = (String) item.get("content");
                                if (title != null && content != null) {
                                    context.append("【").append(title).append("】\n").append(content).append("\n\n");
                                }
                            }
                        }
                        
                        return context.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("知识库检索失败，将使用通用AI回答", e);
        }
        
        return "";
    }
    
    /**
     * 构建增强的提示词
     */
    private String buildEnhancedPrompt(String question, String knowledgeContext, String conversationContext) {
        StringBuilder prompt = new StringBuilder();
        
        // 系统提示
        prompt.append("你是一个专业的心理健康助手，名为'墨语传心'。你的任务是帮助用户解决情绪问题，提供温暖、专业的建议。\n\n");
        
        // 如果有知识库上下文，添加知识库内容
        if (knowledgeContext != null && !knowledgeContext.trim().isEmpty()) {
            prompt.append("以下是相关的知识库内容，请优先基于这些内容回答：\n");
            prompt.append(knowledgeContext);
            prompt.append("\n");
        }
        
        // 如果有对话历史，添加上下文
        if (conversationContext != null && !conversationContext.trim().isEmpty()) {
            prompt.append("以下是用户最近的对话历史，帮助你理解上下文：\n");
            prompt.append(conversationContext);
            prompt.append("\n");
        }
        
        // 用户当前问题
        prompt.append("用户问题：").append(question);
        prompt.append("\n\n请基于以上信息，给出专业、温暖、有帮助的回答。");
        
        return prompt.toString();
    }
}
