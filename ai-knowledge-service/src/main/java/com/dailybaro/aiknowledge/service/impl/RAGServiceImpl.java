package com.dailybaro.aiknowledge.service.impl;

import com.dailybaro.aiknowledge.mapper.KnowledgeMapper;
import com.dailybaro.aiknowledge.model.Knowledge;
import com.dailybaro.aiknowledge.model.vo.KnowledgeVO;
import com.dailybaro.aiknowledge.model.vo.SearchResultVO;
import com.dailybaro.aiknowledge.service.RAGService;
import com.dailybaro.aiknowledge.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGServiceImpl implements RAGService {
    
    @Autowired
    private VectorService vectorService;
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Value("${ai.service-url:http://localhost:8013/api/ai/query}")
    private String aiServiceUrl;  // 现在在同一个服务内
    
    private WebClient webClient;
    
    public RAGServiceImpl() {
        this.webClient = WebClient.builder().build();
    }
    
    @Override
    public SearchResultVO ragSearch(String query, int topK) {
        SearchResultVO result = new SearchResultVO();
        
        try {
            // 1. 向量检索相似知识
            List<Long> knowledgeIds = vectorService.searchSimilar(query, topK);
            
            // 2. 从数据库批量获取知识详情（优化：避免N+1查询）
            List<KnowledgeVO> knowledgeList = new ArrayList<>();
            List<String> sources = new ArrayList<>();
            
            if (!knowledgeIds.isEmpty()) {
                // 批量查询，避免循环查询数据库
                List<Knowledge> knowledgeListFromDb = knowledgeMapper.selectByIds(knowledgeIds);
                
                for (Knowledge knowledge : knowledgeListFromDb) {
                    if (knowledge != null && knowledge.getStatus() == 1) {
                        KnowledgeVO vo = new KnowledgeVO();
                        BeanUtils.copyProperties(knowledge, vo);
                        
                        // 生成摘要
                        String content = knowledge.getContent();
                        if (content != null && content.length() > 200) {
                            vo.setSummary(content.substring(0, 200) + "...");
                        } else {
                            vo.setSummary(content != null ? content : "");
                        }
                        
                        knowledgeList.add(vo);
                        sources.add(knowledge.getTitle());
                    }
                }
            }
            
            result.setKnowledgeList(knowledgeList);
            result.setSources(sources);
            result.setTotal(knowledgeList.size());
            
            // 3. 构建上下文并调用AI生成回答
            String context = buildContext(knowledgeList);
            String aiAnswer = generateAIAnswer(query, context);
            result.setAiAnswer(aiAnswer);
            
        } catch (Exception e) {
            log.error("RAG search failed", e);
            result.setKnowledgeList(new ArrayList<>());
            result.setAiAnswer("抱歉，检索过程中出现错误，请稍后重试。");
        }
        
        return result;
    }
    
    /**
     * 构建上下文（优化：使用StringBuilder预分配容量）
     */
    private String buildContext(List<KnowledgeVO> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            return "未找到相关内容。";
        }
        
        // 预估算容量，减少扩容次数
        int estimatedSize = knowledgeList.size() * 500; // 假设每条知识平均500字符
        StringBuilder context = new StringBuilder(estimatedSize);
        context.append("以下是从知识库中检索到的相关内容：\n\n");
        
        for (int i = 0; i < knowledgeList.size(); i++) {
            KnowledgeVO knowledge = knowledgeList.get(i);
            context.append("【").append(i + 1).append("】");
            if (knowledge.getTitle() != null) {
                context.append(knowledge.getTitle());
            }
            context.append("\n");
            if (knowledge.getContent() != null) {
                context.append(knowledge.getContent());
            }
            context.append("\n\n");
        }
        
        return context.toString();
    }
    
    /**
     * 调用AI服务生成回答
     */
    private String generateAIAnswer(String query, String context) {
        try {
            String prompt = String.format(
                    "基于以下知识库内容，回答用户的问题。\n\n" +
                    "用户问题：%s\n\n" +
                    "知识库内容：\n%s\n\n" +
                    "请基于以上知识库内容，给出专业、准确、有帮助的回答。如果知识库中没有相关信息，请说明。",
                    query, context
            );
            
            // 调用AI服务（通过 app-service 转发）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("question", prompt);
            
            Map<String, Object> response = webClient.post()
                    .uri(aiServiceUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            // 解析响应
            if (response != null) {
                Object code = response.get("code");
                if (code != null && (code instanceof Integer && (Integer)code == 200 || "200".equals(String.valueOf(code)))) {
                    Object data = response.get("data");
                    if (data != null) {
                        return data.toString();
                    }
                }
                // 如果格式不对，尝试直接返回 data 字段
                Object data = response.get("data");
                if (data != null) {
                    return data.toString();
                }
            }
            
            return "基于知识库内容，我为您整理了相关信息。请查看上方的知识条目获取详细内容。";
            
        } catch (Exception e) {
            log.error("Failed to generate AI answer", e);
            return "基于知识库内容，我为您整理了相关信息。请查看上方的知识条目获取详细内容。";
        }
    }
}
