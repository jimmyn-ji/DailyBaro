package com.dailybaro.aiknowledge.model.dto;

import lombok.Data;

@Data
public class SearchDTO {
    private String query;          // 搜索关键词
    private String category;        // 分类筛选
    private String subcategory;     // 子分类筛选
    private Integer page = 1;       // 页码
    private Integer size = 10;      // 每页数量
    private Boolean useRAG = true;  // 是否使用RAG检索
}
