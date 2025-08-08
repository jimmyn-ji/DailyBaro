package com.dailybaro.diary.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("diaries")
public class Diary {

    @TableId(value = "diary_id", type = IdType.AUTO)
    private Long diaryId;

    private Long userId;

    private String title;

    private String content;

    private String status;

    private Date createTime;

    private Date updateTime;
} 