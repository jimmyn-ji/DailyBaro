package com.project.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateCommentDTO {
    private Long postId;
    private Long commentId;
    private String content;
    private Date updateTime;
}