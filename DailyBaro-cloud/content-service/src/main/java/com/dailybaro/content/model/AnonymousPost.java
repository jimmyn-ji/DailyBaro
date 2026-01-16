package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("anonymous_posts")
public class AnonymousPost {

    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    private Long userId;

    private String content;

    private String visibility;

    private Date createTime;
}
