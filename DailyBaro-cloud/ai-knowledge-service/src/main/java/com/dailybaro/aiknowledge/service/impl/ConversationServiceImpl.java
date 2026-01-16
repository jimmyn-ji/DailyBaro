package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.model.vo.ConversationMessage;
import com.dailybaro.aiknowledge.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${conversation.history.ttl-days:30}")
    private int ttlDays;
    
    private static final String CONVERSATION_KEY_PREFIX = "ai:conversation:";
    private static final int MAX_HISTORY_SIZE = 100; // 最多保存100条消息
    
    @Override
    public void saveMessage(Long userId, ConversationMessage message) {
        try {
            String key = CONVERSATION_KEY_PREFIX + userId;
            
            // 获取现有历史
            List<ConversationMessage> history = getConversationHistory(userId, MAX_HISTORY_SIZE);
            
            // 添加新消息
            history.add(message);
            
            // 如果超过最大数量，只保留最近的
            if (history.size() > MAX_HISTORY_SIZE) {
                history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
            }
            
            // 保存到Redis，设置过期时间
            redisTemplate.opsForValue().set(key, history, Duration.ofDays(ttlDays));
            
        } catch (Exception e) {
            log.error("保存对话消息失败 userId={}", userId, e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<ConversationMessage> getConversationHistory(Long userId, int limit) {
        try {
            String key = CONVERSATION_KEY_PREFIX + userId;
            Object data = redisTemplate.opsForValue().get(key);
            
            if (data == null) {
                return new ArrayList<>();
            }
            
            List<ConversationMessage> history;
            if (data instanceof List) {
                history = (List<ConversationMessage>) data;
            } else {
                return new ArrayList<>();
            }
            
            // 限制返回数量
            if (limit > 0 && history.size() > limit) {
                return history.subList(history.size() - limit, history.size());
            }
            
            return history;
            
        } catch (Exception e) {
            log.error("获取对话历史失败 userId={}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void clearConversationHistory(Long userId) {
        try {
            String key = CONVERSATION_KEY_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("清除对话历史失败 userId={}", userId, e);
        }
    }
    
    @Override
    public String getRecentContext(Long userId, int maxMessages) {
        List<ConversationMessage> history = getConversationHistory(userId, maxMessages);
        
        if (history.isEmpty()) {
            return "";
        }
        
        // 构建上下文字符串
        return history.stream()
                .map(msg -> msg.getRole() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));
    }
}
