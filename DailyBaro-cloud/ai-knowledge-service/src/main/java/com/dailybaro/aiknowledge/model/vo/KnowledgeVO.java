package com.dailybaro.aiknowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeVO {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String subcategory;
    private String tags;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String summary;         // 内容摘要（用于列表展示）
}
