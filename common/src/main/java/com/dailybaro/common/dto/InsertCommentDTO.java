package com.project.model.dto;

import lombok.Data;

@Data
public class InsertCommentDTO {
    private Long postId;
    private Long userId;
    private String content;
}