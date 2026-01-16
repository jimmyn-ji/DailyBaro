package com.dailybaro.user.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class WeiboLoginDTO {
    
    @NotBlank(message = "微博授权码不能为空")
    private String code;
    
    private String state;
}
