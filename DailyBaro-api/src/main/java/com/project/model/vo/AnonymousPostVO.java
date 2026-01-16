package com.project.model.vo;

import lombok.Data;
import java.util.Date;

@Data
public class AnonymousPostVO {
    private Long postId;
    private Long userId; // Can be anonymized or obfuscated on the client-side if needed
    private String content;
    private String visibility;
    private Date createTime;
    private int likeCount;
    private int commentCount;
    private java.util.List<com.project.model.Comments> comments;
    private boolean liked;
} 