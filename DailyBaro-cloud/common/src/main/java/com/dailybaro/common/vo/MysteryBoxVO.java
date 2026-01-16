package com.dailybaro.common.vo;

import lombok.Data;
import java.util.Date;

@Data
public class MysteryBoxVO {
    private Long drawnBoxId;
    private String content;
    private String contentType; // quote, task, tip
    private Boolean isCompleted;
    private Date drawTime;
    private Integer energyReward; // 能量值奖励
} 