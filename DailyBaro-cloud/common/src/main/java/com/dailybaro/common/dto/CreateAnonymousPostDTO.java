package com.dailybaro.common.dto;

import lombok.Data;

@Data
public class CreateAnonymousPostDTO {
    private String content;
    private String visibility; // "public" or "private"
} 