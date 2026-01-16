package com.dailybaro.aiknowledge.service;

import com.dailybaro.aiknowledge.model.Knowledge;
import com.dailybaro.aiknowledge.model.dto.KnowledgeDTO;
import com.dailybaro.aiknowledge.model.dto.SearchDTO;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.model.vo.SearchResultVO;

import java.util.List;

public interface KnowledgeService {
    
    /**
     * 创建知识
     */
    Long createKnowledge(KnowledgeDTO dto, Long createBy);
    
    /**
     * 更新知识
     */
    boolean updateKnowledge(Long id, KnowledgeDTO dto);
    
    /**
     * 删除知识
     */
    boolean deleteKnowledge(Long id);
    
    /**
     * 获取知识详情
     */
    KnowledgeVO getKnowledgeById(Long id);
    
    /**
     * 按分类获取知识列表
     */
    List<KnowledgeVO> getKnowledgeByCategory(String category);
    
    /**
     * 按子分类获取知识列表
     */
    List<KnowledgeVO> getKnowledgeBySubcategory(String category, String subcategory);
    
    /**
     * 搜索知识
     */
    SearchResultVO searchKnowledge(SearchDTO searchDTO);
    
    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);
}
