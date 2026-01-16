package com.dailybaro.user.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordDTO {
    
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String newPassword;
    
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
