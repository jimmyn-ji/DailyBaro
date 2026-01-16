package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("post_comments")
public class PostComment {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    private Long postId;
    private Long userId;
    private String content;
    private Date createTime;
    private Date updateTime;

    private Integer isReply;      // 是否为回复(0:否,1:是)
    private Long replyToCommentId; // 被回复评论ID
    private Long replyToUserId;    // 被回复用户ID
    private Integer replyCount;    // 回复数
}
