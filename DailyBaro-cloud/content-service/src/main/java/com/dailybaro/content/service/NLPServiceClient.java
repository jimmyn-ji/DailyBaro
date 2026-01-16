package com.dailybaro.content.service;

import com.dailybaro.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * NLP服务客户端
 * 用于调用情绪分析服务
 */
@FeignClient(name = "nlp-service", url = "http://localhost:5001")
public interface NLPServiceClient {

    /**
     * 分析日记情绪
     * @param request 包含日记内容的请求
     * @return 情绪分析结果
     */
    @PostMapping("/api/emotion/analyze-diary")
    Result<Map<String, Object>> analyzeDiaryEmotion(@RequestBody Map<String, Object> request);

    /**
     * 建议情绪标签
     * @param request 包含文本内容的请求
     * @return 情绪标签建议
     */
    @PostMapping("/api/emotion/suggest-tags")
    Result<Map<String, Object>> suggestEmotionTags(@RequestBody Map<String, Object> request);

    /**
     * 通用情绪分析
     * @param request 包含文本内容的请求
     * @return 情绪分析结果
     */
    @PostMapping("/api/emotion/analyze")
    Result<Map<String, Object>> analyzeEmotion(@RequestBody Map<String, Object> request);
}
