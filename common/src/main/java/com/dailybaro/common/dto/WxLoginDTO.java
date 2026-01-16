package com.project.model.dto;

import lombok.Data;

@Data
public class WxLoginDTO {
    private String code;          // 微信登录code
    private String encryptedData; // 加密数据
    private String iv;           // 加密算法的初始向量
}