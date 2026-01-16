package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {

    @TableId(value = "uid", type = IdType.AUTO)
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