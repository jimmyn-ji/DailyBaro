package com.project.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDataPointVO {
    private Date date;
    private Double value; // A numerical representation of the emotion
} 