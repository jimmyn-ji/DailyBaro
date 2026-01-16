package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tags")
public class Tag {

    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    private String tagName;
} 