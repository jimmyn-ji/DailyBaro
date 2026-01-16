package com.dailybaro.common.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PostVO {
    private Long postId;
    private Long userId;
    private String content;
    private Date createTime;
    private List<ReplyVO> replies;
}