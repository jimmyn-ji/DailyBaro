package com.dailybaro.user.auth.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EmailLoginDTO {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码长度必须为6位")
    private String verificationCode;
}
