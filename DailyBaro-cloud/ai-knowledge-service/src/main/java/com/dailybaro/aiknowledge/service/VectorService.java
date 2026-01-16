package com.dailybaro.aiknowledge.service;

import java.util.List;

public interface VectorService {
    
    /**
     * 将文本转换为向量
     */
    List<Float> textToVector(String text);
    
    /**
     * 将知识内容存储到Milvus
     */
    String storeVector(Long knowledgeId, String text);
    
    /**
     * 向量相似度搜索
     */
    List<Long> searchSimilar(String queryText, int topK);
    
    /**
     * 删除向量
     */
    boolean deleteVector(String vectorId);
}
