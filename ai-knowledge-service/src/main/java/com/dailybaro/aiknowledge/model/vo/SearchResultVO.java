package com.dailybaro.aiknowledge.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class SearchResultVO {
    private List<KnowledgeVO> knowledgeList;  // 知识列表
    private String aiAnswer;                    // AI生成的回答（RAG）
    private List<String> sources;               // 参考来源
    private Integer total;                      // 总数
    private Integer page;                       // 当前页
    private Integer size;                       // 每页数量
}
