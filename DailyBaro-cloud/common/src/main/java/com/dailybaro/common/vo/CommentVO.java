package com.dailybaro.common.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Date createTime;
    private Date updateTime;
    private Integer replyCount;
}