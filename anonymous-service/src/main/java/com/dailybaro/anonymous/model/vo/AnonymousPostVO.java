package com.dailybaro.anonymous.model.vo;

import com.dailybaro.anonymous.model.Comments;
import lombok.Data;
import java.util.List;

@Data
public class AnonymousPostVO {
    private Long postId;
    private Long userId;
    private String content;
    private String visibility;
    private Long createTime;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private List<Comments> comments;
} 