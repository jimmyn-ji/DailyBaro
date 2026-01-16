package com.dailybaro.aiknowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话消息实体
 */
@Data
public class ConversationMessage {
    private String role;  // "user" 或 "ai"
    private String content;
    private LocalDateTime timestamp;
    
    public ConversationMessage() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
}
