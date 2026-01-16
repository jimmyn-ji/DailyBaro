package com.project.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class QueryCommentDTO {
    private Long postId;
    private Long userId;
    private Date startTime;
    private Date endTime;
}