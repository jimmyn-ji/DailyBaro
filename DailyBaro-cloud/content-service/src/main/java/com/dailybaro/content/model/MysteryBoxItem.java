package com.dailybaro.content.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("mystery_box_items")
public class MysteryBoxItem {

    @TableId(value = "box_item_id", type = IdType.AUTO)
    private Long boxItemId;

    private String contentType;

    private String content;
} 