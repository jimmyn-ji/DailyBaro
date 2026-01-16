package com.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("daily_quotes")
public class DailyQuote {

    @TableId(value = "quote_id", type = IdType.AUTO)
    private Long quoteId;

    private String content;

    private String author;
} 