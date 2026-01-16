package com.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user_daily_quote")
public class UserDailyQuote {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private Date createTime;
    private Date updateTime;
} 