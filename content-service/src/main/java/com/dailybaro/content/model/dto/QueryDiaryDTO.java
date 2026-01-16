package com.dailybaro.content.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QueryDiaryDTO {
    private List<Long> tagIds; // 用于前端 tag_id 查询
    private String status; // 新增，支持草稿/已发布筛选
    private String date; // 只筛选某一天
    private Long targetUserId; // 指定要查询的目标用户ID，null表示查询当前用户
}
