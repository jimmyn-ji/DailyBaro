package com.dailybaro.emotion.model.vo;

public class EmotionShareVO {
    private String emotion;
    private Double percentage;

    public EmotionShareVO() {}
    public EmotionShareVO(String emotion, Double percentage) {
        this.emotion = emotion;
        this.percentage = percentage;
    }
    public String getEmotion() {
        return emotion;
    }
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
    public Double getPercentage() {
        return percentage;
    }
    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
} 