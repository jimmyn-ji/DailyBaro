package com.project.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class QueryPostDTO {
    private Long userId;
    private Date startTime;
    private Date endTime;
}