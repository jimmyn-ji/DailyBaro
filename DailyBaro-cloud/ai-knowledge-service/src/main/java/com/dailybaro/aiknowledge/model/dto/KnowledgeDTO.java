package com.dailybaro.aiknowledge.model.dto;

import lombok.Data;

@Data
public class KnowledgeDTO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String subcategory;
    private String tags;
    private Integer status;
}
