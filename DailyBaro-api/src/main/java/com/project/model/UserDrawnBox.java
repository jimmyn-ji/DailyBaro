package com.project.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_drawn_boxes")
public class UserDrawnBox {

    @TableId(value = "drawn_box_id", type = IdType.AUTO)
    private Long drawnBoxId;

    private Long userId;

    private Long boxItemId;

    private Date drawTime;

    private Boolean isCompleted;
} 