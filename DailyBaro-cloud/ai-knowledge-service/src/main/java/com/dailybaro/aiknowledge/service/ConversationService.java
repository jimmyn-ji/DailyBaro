package com.dailybaro.aiknowledge.service;

import com.dailybaro.aiknowledge.model.vo.ConversationMessage;
import java.util.List;

/**
 * 对话历史服务接口
 */
public interface ConversationService {
    
    /**
     * 保存对话消息
     */
    void saveMessage(Long userId, ConversationMessage message);
    
    /**
     * 获取用户对话历史
     */
    List<ConversationMessage> getConversationHistory(Long userId, int limit);
    
    /**
     * 清除用户对话历史
     */
    void clearConversationHistory(Long userId);
    
    /**
     * 获取最近的对话上下文（用于RAG）
     */
    String getRecentContext(Long userId, int maxMessages);
}
