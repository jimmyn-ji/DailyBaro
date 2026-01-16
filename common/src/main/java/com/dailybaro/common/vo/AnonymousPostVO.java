package com.dailybaro.common.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class AnonymousPostVO {
    private Long postId;
    private Long userId; // Can be anonymized or obfuscated on the client-side if needed
    private String content;
    private String visibility;
    private Date createTime;
    private int likeCount;
    private int commentCount;
    private List<CommentVO> comments;
    private boolean liked;
}
