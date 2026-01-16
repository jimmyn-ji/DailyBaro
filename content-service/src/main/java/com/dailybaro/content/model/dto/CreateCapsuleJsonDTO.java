package com.dailybaro.content.model.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CreateCapsuleJsonDTO {
    private String currentEmotion;
    private String thoughts;
    private String futureGoal;
    private Date openTime;
    private String reminderType;
    private List<String> mediaUrls;
}


