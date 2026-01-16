package com.dailybaro.aiknowledge.service;

import com.dailybaro.aiknowledge.model.vo.SearchResultVO;

public interface RAGService {
    
    /**
     * RAG检索增强生成
     * @param query 用户查询
     * @return 包含检索结果和AI生成回答的结果
     */
    SearchResultVO ragSearch(String query, int topK);
}
