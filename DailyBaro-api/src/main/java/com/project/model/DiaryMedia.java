package com.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("diary_media")
public class DiaryMedia {

    @TableId(value = "media_id", type = IdType.AUTO)
    private Long mediaId;

    private Long diaryId;

    private String mediaType;

    private String mediaUrl;

    private Date createTime;
} 