package com.project.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
@TableName("user")
public class User {

    @Id
    private Long uid;
    private String account;
    private String password;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    private Integer isdelete;
    private String wxOpenid;
    private Integer energy; // 情绪能量值

}



