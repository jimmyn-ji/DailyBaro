package com.dailybaro.common.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ReplyVO {
    private Long postId;
    private Long userId;
    private String content;
    private Date createTime;
}