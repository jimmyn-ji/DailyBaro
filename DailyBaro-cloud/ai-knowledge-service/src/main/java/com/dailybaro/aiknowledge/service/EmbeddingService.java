package com.dailybaro.aiknowledge.service;

import java.util.List;

/**
 * 文本向量化服务接口
 * 需要实现真正的 embedding API 调用
 */
public interface EmbeddingService {
    
    /**
     * 将文本转换为向量
     * @param text 输入文本
     * @return 向量列表（维度由配置决定）
     */
    List<Float> embed(String text);
    
    /**
     * 批量向量化
     * @param texts 文本列表
     * @return 向量列表
     */
    List<List<Float>> embedBatch(List<String> texts);
}
