package com.dailybaro.nlp.service;

import com.dailybaro.nlp.dto.EmotionAnalysisResponse;
import java.util.List;
import java.util.Map;

/**
 * NLP服务接口
 * 
 * @author DailyBaro Team
 * @version 1.0.0
 */
public interface NlpService {

    /**
     * 分析文本情绪
     * 
     * @param text 待分析文本
     * @return 情绪分析结果
     */
    EmotionAnalysisResponse analyzeEmotion(String text);

    /**
     * 批量分析文本情绪
     * 
     * @param texts 待分析文本列表
     * @return 情绪分析结果列表
     */
    List<EmotionAnalysisResponse> batchAnalyzeEmotion(List<String> texts);

    /**
     * 情绪分类
     * 
     * @param text 待分类文本
     * @param topK 返回前K个情绪
     * @return 情绪分类结果
     */
    EmotionAnalysisResponse classifyEmotion(String text, Integer topK);

    /**
     * 获取服务信息
     * 
     * @return 服务信息
     */
    Map<String, Object> getServiceInfo();

    /**
     * 检查Python NLP服务状态
     * 
     * @return 是否可用
     */
    Boolean checkPythonServiceHealth();
}
