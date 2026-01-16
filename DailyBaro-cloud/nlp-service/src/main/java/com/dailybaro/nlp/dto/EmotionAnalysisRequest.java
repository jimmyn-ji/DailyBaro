package com.dailybaro.nlp.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 情绪分析请求DTO
 * 
 * @author DailyBaro Team
 * @version 1.0.0
 */
@Data
public class EmotionAnalysisRequest {

    /**
     * 待分析文本
     */
    @NotBlank(message = "文本内容不能为空")
    @Size(max = 1000, message = "文本长度不能超过1000字符")
    private String text;

    /**
     * 用户ID（可选）
     */
    private String userId;

    /**
     * 分析类型（可选）
     */
    private String analysisType = "emotion";
}
