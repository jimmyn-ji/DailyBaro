package com.dailybaro.nlp.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 情绪分析响应DTO
 * 
 * @author DailyBaro Team
 * @version 1.0.0
 */
@Data
public class EmotionAnalysisResponse {

    /**
     * 识别的主要情绪
     */
    private String emotion;

    /**
     * 情绪强度（1-10）
     */
    private Double intensity;

    /**
     * 置信度（0-1）
     */
    private Double confidence;

    /**
     * 情绪极性（positive/negative/neutral）
     */
    private String polarity;

    /**
     * 所有情绪的概率分布
     */
    private Map<String, Double> allEmotions;

    /**
     * 原始文本
     */
    private String text;

    /**
     * 分析时间
     */
    private LocalDateTime timestamp;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 错误信息（如果有）
     */
    private String error;

    /**
     * 是否成功
     */
    private Boolean success = true;
}
