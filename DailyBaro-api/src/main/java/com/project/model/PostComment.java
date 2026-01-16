package com.project.model;

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
} 