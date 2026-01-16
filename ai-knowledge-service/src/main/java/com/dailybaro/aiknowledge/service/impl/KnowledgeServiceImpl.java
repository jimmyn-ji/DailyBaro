package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.mapper.KnowledgeMapper;
import com.dailybaro.aiknowledge.model.Knowledge;
import com.dailybaro.aiknowledge.model.dto.KnowledgeDTO;
import com.dailybaro.aiknowledge.model.dto.SearchDTO;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.model.vo.SearchResultVO;
import com.dailybaro.aiknowledge.service.KnowledgeService;
import com.dailybaro.aiknowledge.service.RAGService;
import com.dailybaro.aiknowledge.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Autowired
    private VectorService vectorService;
    
    @Autowired
    private RAGService ragService;
    
    @Override
    public Long createKnowledge(KnowledgeDTO dto, Long createBy) {
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(dto, knowledge);
        knowledge.setCreateBy(createBy);
        knowledge.setStatus(dto.getStatus() != null ? dto.getStatus() : 0); // 默认草稿
        
        knowledgeMapper.insert(knowledge);
        
        // 向量化并存储
        if (knowledge.getId() != null) {
            String vectorId = vectorService.storeVector(knowledge.getId(), 
                    knowledge.getTitle() + " " + knowledge.getContent());
            knowledge.setVectorId(vectorId);
            knowledgeMapper.update(knowledge);
        }
        
        return knowledge.getId();
    }
    
    @Override
    public boolean updateKnowledge(Long id, KnowledgeDTO dto) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            return false;
        }
        
        BeanUtils.copyProperties(dto, knowledge, "id", "createBy", "createTime");
        
        // 更新向量
        if (dto.getContent() != null || dto.getTitle() != null) {
            String vectorId = vectorService.storeVector(id, 
                    knowledge.getTitle() + " " + knowledge.getContent());
            knowledge.setVectorId(vectorId);
        }
        
        return knowledgeMapper.update(knowledge) > 0;
    }
    
    @Override
    public boolean deleteKnowledge(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge != null && knowledge.getVectorId() != null) {
            vectorService.deleteVector(knowledge.getVectorId());
        }
        return knowledgeMapper.delete(id) > 0;
    }
    
    @Override
    public KnowledgeVO getKnowledgeById(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            return null;
        }
        
        KnowledgeVO vo = new KnowledgeVO();
        BeanUtils.copyProperties(knowledge, vo);
        
        // 增加浏览次数
        incrementViewCount(id);
        
        return vo;
    }
    
    @Override
    public List<KnowledgeVO> getKnowledgeByCategory(String category) {
        List<Knowledge> knowledgeList = knowledgeMapper.selectByCategory(category);
        return convertToVOList(knowledgeList);
    }
    
    @Override
    public List<KnowledgeVO> getKnowledgeBySubcategory(String category, String subcategory) {
        List<Knowledge> knowledgeList = knowledgeMapper.selectBySubcategory(category, subcategory);
        return convertToVOList(knowledgeList);
    }
    
    @Override
    public SearchResultVO searchKnowledge(SearchDTO searchDTO) {
        SearchResultVO result = new SearchResultVO();
        
        if (searchDTO.getUseRAG() != null && searchDTO.getUseRAG()) {
            // 使用RAG检索
            return ragService.ragSearch(searchDTO.getQuery(), searchDTO.getSize());
        } else {
            // 传统关键词搜索
            List<Knowledge> knowledgeList = knowledgeMapper.searchByKeyword(searchDTO.getQuery());
            List<KnowledgeVO> voList = convertToVOList(knowledgeList);
            
            result.setKnowledgeList(voList);
            result.setTotal(voList.size());
            result.setPage(searchDTO.getPage());
            result.setSize(searchDTO.getSize());
        }
        
        return result;
    }
    
    @Override
    public void incrementViewCount(Long id) {
        knowledgeMapper.incrementViewCount(id);
    }
    
    private List<KnowledgeVO> convertToVOList(List<Knowledge> knowledgeList) {
        return knowledgeList.stream().map(knowledge -> {
            KnowledgeVO vo = new KnowledgeVO();
            BeanUtils.copyProperties(knowledge, vo);
            
            // 生成摘要
            String content = knowledge.getContent();
            if (content != null && content.length() > 200) {
                vo.setSummary(content.substring(0, 200) + "...");
            } else {
                vo.setSummary(content);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
}
