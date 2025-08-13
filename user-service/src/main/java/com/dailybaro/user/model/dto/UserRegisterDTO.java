package com.dailybaro.user.model.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String account;
    private String password;
    private String confirmPassword;
    private String phone;
    private String email;
} 