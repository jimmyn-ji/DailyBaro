package com.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("capsule_media")
public class CapsuleMedia {

    @TableId(value = "media_id", type = IdType.AUTO)
    private Long mediaId;

    private Long capsuleId;

    private String mediaType;

    private String mediaUrl;
} 