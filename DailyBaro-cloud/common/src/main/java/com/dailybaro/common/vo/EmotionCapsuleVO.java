package com.project.model.vo;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class EmotionCapsuleVO {
    private Long capsuleId;
    private Long userId;
    private Date openTime;
    private Date createTime;
    private boolean isOpened;
    
    // These fields are only populated if isOpened is true
    private String content; // 保留原有字段，用于兼容
    private String currentEmotion; // 当前情绪
    private String thoughts; // 想法
    private String futureGoal; // 未来目标
    private List<MediaVO> media;
    private String reminderType;
    private Integer reminderSent;
    private Integer reminderRead;
} 