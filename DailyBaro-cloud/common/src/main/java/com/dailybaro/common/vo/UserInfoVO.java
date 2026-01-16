package com.project.model.vo;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long uid;      // 用户ID
    private String account; // 账号
    private Integer code;   // 返回码（1000-成功，其他-失败）
    private Integer energyReward; // 登录奖励的能量值
}