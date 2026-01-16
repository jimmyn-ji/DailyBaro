package com.project.model.dto;

import lombok.Data;

@Data
public class ReplyDTO {
    private Long commentId;  // 被回复的评论ID
    private Long postId;     // 所属帖子ID
    private Long userId;     // 回复用户ID
    private String content;  // 回复内容
    private Long replyToUserId; // 被回复用户ID
}