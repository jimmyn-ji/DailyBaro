package com.dailybaro.diary.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DiaryVO {
    private Long diaryId;
    private Long userId;
    private String userAccount; // 用户账号
    private String title;
    private String content;
    private String status;
    private Date createTime;
    private Date updateTime;
    private List<String> tags; // List of tag names
    private List<Long> tagIds; // 新增，标签id数组
    private List<MediaVO> media; // List of media objects
} 