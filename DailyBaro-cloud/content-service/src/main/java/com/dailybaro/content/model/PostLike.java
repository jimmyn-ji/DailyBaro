package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("post_likes")
public class PostLike {

    @TableId(value = "like_id", type = IdType.AUTO)
    private Long likeId;

    private Long postId;

    private Long userId;
}
