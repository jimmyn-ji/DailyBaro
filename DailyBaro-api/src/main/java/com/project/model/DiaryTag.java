package com.project.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("diary_tags")
public class DiaryTag {

    private Long diaryId;

    private Long tagId;
} 