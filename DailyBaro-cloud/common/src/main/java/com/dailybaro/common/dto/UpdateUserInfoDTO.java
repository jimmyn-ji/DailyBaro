package com.project.model.dto;

import lombok.Data;

@Data
public class UpdateUserInfoDTO {
    private Long uid;
    private String phone;
    private String email;
}
