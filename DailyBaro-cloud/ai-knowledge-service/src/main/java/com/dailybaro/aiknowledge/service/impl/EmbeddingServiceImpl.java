package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本向量化服务实现
 * 
 * 注意：当前需要配置真实的 embedding API
 * 
 * 支持的方案：
 * 1. OpenAI Embedding API
 * 2. DeepSeek Embedding API（如果支持）
 * 3. 百度/阿里云 Embedding API
 * 4. Hugging Face API
 * 5. 本地 Python embedding 服务
 */
@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {
    
    @Value("${embedding.api-url:}")
    private String apiUrl;
    
    @Value("${embedding.api-key:}")
    private String apiKey;
    
    @Value("${embedding.model:text-embedding-ada-002}")
    private String model;
    
    @Value("${embedding.dimension:1536}")
    private Integer dimension;
    
    @Value("${embedding.provider:openai}")
    private String provider; // openai, deepseek, baidu, aliyun, huggingface, local
    
    private WebClient webClient;
    
    public EmbeddingServiceImpl() {
        this.webClient = WebClient.builder().build();
    }
    
    @Override
    public List<Float> embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("输入文本为空，返回零向量");
            return generateZeroVector();
        }
        
        // 如果未配置 API，使用临时方案（需要用户提供配置）
        if (apiUrl == null || apiUrl.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            log.error("Embedding API 未配置！请配置 embedding.api-url 和 embedding.api-key");
            log.error("当前使用临时向量生成，检索效果不佳。请尽快配置真实的 embedding API。");
            return generateTemporaryVector(text);
        }
        
        try {
            return callEmbeddingAPI(text);
        } catch (Exception e) {
            log.error("调用 Embedding API 失败: " + e.getMessage(), e);
            log.warn("使用临时向量生成作为降级方案");
            return generateTemporaryVector(text);
        }
    }
    
    @Override
    public List<List<Float>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        // 优化：预分配容量
        List<List<Float>> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            results.add(embed(text));
        }
        return results;
    }
    
    /**
     * 调用真实的 Embedding API
     */
    private List<Float> callEmbeddingAPI(String text) {
        switch (provider.toLowerCase()) {
            case "openai":
            case "deepseek":
                return callOpenAIStyleAPI(text);
            case "baidu":
                return callBaiduAPI(text);
            case "aliyun":
                return callAliyunAPI(text);
            case "huggingface":
                return callHuggingFaceAPI(text);
            case "local":
                return callLocalEmbeddingService(text);
            default:
                log.warn("未知的 provider: " + provider + "，使用 OpenAI 风格 API");
                return callOpenAIStyleAPI(text);
        }
    }
    
    /**
     * 调用 OpenAI/DeepSeek 风格的 Embedding API
     */
    private List<Float> callOpenAIStyleAPI(String text) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", text);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        
        try {
            Map response = webClient.post()
                    .uri(apiUrl)
                    .headers(h -> headers.forEach(h::set))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (data != null && !data.isEmpty()) {
                    List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                    if (embedding != null) {
                        List<Float> result = new ArrayList<>();
                        for (Double d : embedding) {
                            result.add(d.floatValue());
                        }
                        return result;
                    }
                }
            }
            
            log.error("Embedding API 返回格式异常: " + response);
            return generateTemporaryVector(text);
        } catch (Exception e) {
            log.error("调用 OpenAI 风格 Embedding API 失败", e);
            throw e;
        }
    }
    
    /**
     * 调用百度 Embedding API（需要根据实际 API 文档调整）
     */
    private List<Float> callBaiduAPI(String text) {
        // TODO: 实现百度 Embedding API 调用
        log.warn("百度 Embedding API 未实现，使用临时向量");
        return generateTemporaryVector(text);
    }
    
    /**
     * 调用阿里云 Embedding API（需要根据实际 API 文档调整）
     */
    private List<Float> callAliyunAPI(String text) {
        // TODO: 实现阿里云 Embedding API 调用
        log.warn("阿里云 Embedding API 未实现，使用临时向量");
        return generateTemporaryVector(text);
    }
    
    /**
     * 调用 Hugging Face API
     */
    private List<Float> callHuggingFaceAPI(String text) {
        // TODO: 实现 Hugging Face Embedding API 调用
        log.warn("Hugging Face Embedding API 未实现，使用临时向量");
        return generateTemporaryVector(text);
    }
    
    /**
     * 调用本地 Python Embedding 服务
     */
    private List<Float> callLocalEmbeddingService(String text) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);
            
            Map response = webClient.post()
                    .uri(apiUrl) // 本地服务地址，如 http://localhost:5002/embed
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("embedding")) {
                List<Double> embedding = (List<Double>) response.get("embedding");
                List<Float> result = new ArrayList<>();
                for (Double d : embedding) {
                    result.add(d.floatValue());
                }
                return result;
            }
            
            return generateTemporaryVector(text);
        } catch (Exception e) {
            log.error("调用本地 Embedding 服务失败", e);
            return generateTemporaryVector(text);
        }
    }
    
    /**
     * 生成临时向量（降级方案，效果不佳）
     * 仅在没有配置 API 时使用
     */
    private List<Float> generateTemporaryVector(String text) {
        // 使用文本哈希生成伪向量（效果很差，仅用于测试）
        List<Float> vector = new ArrayList<>();
        java.util.Random random = new java.util.Random(text.hashCode());
        for (int i = 0; i < dimension; i++) {
            vector.add((float) (random.nextGaussian() * 0.1));
        }
        // 归一化
        double norm = Math.sqrt(vector.stream().mapToDouble(v -> v * v).sum());
        if (norm > 0) {
            return vector.stream().map(v -> (float)(v / norm)).collect(java.util.stream.Collectors.toList());
        }
        return vector;
    }
    
    /**
     * 生成零向量
     */
    private List<Float> generateZeroVector() {
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < dimension; i++) {
            vector.add(0.0f);
        }
        return vector;
    }
}
