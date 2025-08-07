package com.dailybaro.emotion.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionShareVO {
    private String emotion; // The name of the emotion, e.g., "Happy", "Sad"
    private Double percentage;
} 