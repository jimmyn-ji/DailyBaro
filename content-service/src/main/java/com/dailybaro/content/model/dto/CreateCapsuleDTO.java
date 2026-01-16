package com.dailybaro.content.model.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class CreateCapsuleDTO {
    private String currentEmotion; // 当前情绪
    private String thoughts; // 想法
    private String futureGoal; // 未来目标
    private String content; // 保留原有字段，用于兼容

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date openTime;

    private String reminderType; // "app_notification", "email", or "sms"

    private List<MultipartFile> mediaFiles;

    private Integer reminderSent;
    private Integer reminderRead;
} 