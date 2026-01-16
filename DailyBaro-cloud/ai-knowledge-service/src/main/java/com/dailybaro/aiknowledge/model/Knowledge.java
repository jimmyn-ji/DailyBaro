package com.dailybaro.aiknowledge.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge")
public class Knowledge {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;           // 标题
    private String content;         // 内容
    private String category;        // 分类：情绪管理、心理排忧、成长指南、案例分享
    private String subcategory;     // 子分类：如焦虑、抑郁、压力等
    private String tags;            // 标签（逗号分隔）
    private String vectorId;        // Milvus 向量ID
    private Integer status;         // 状态：0-草稿，1-已发布，2-已审核
    private Integer viewCount;      // 浏览次数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;          // 创建人ID（管理员）
}
