package com.dailybaro.diary.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

@Data
@TableName("diary_tags")
public class DiaryTag {

    @TableId(type = IdType.INPUT)
    private Long diaryId;

    private Long tagId;
} 