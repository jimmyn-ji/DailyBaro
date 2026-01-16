package com.dailybaro.user.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BindThirdPartyDTO {
    
    @NotBlank(message = "授权码不能为空")
    private String code;
    
    private String state;
}
